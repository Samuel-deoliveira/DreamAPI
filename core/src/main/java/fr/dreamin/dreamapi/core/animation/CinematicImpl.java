package fr.dreamin.dreamapi.core.animation;

import fr.dreamin.dreamapi.api.animation.ReconnectBehavior;
import fr.dreamin.dreamapi.api.animation.camera.CameraSegment;
import fr.dreamin.dreamapi.api.animation.Cinematic;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.animation.camera.CameraSegmentImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CinematicImpl implements Cinematic, Listener {

  private record ResumeState(int segmentIndex, int frameIndex) {}

  private final String name;
  private final List<CameraSegment> segments;

  private final boolean returnToStart;
  private final Location endAt;
  private final GameMode forcedGameMode;
  private final boolean copyInventory;
  private final ReconnectBehavior reconnectBehavior;

  private final Consumer<Player> onStart;
  private final Consumer<Player> onEnd;
  private final BiConsumer<Player, Integer> onSegmentChange;

  private final Map<UUID, Boolean> playing = new HashMap<>();
  private final Map<UUID, Location> startLocation = new HashMap<>();
  private final Map<UUID, GameMode> originalGameMode = new HashMap<>();
  private final Map<UUID, ItemStack[]> savedInventory = new HashMap<>();
  private final Map<UUID, ResumeState> resumeStates = new HashMap<>();
  private final Set<UUID> pendingReplay = new HashSet<>();

  private ResumeState globalProgress = new ResumeState(0, 0);

  public CinematicImpl(
    final @NotNull String name,
    final @NotNull List<@NotNull CameraSegment> segments,
    final boolean returnToStart,
    final @NotNull Location endAt,
    final @NotNull GameMode forcedGameMode,
    final boolean copyInventory,
    final @NotNull ReconnectBehavior reconnectBehavior,
    final Consumer<Player> onStart,
    final Consumer<Player> onEnd,
    final BiConsumer<Player, Integer> onSegmentChange
  ) {
    this.name = name;
    this.segments = List.copyOf(segments);
    this.returnToStart = returnToStart;
    this.endAt = endAt;
    this.forcedGameMode = forcedGameMode;
    this.copyInventory = copyInventory;
    this.reconnectBehavior = reconnectBehavior;
    this.onStart = onStart;
    this.onEnd = onEnd;
    this.onSegmentChange = onSegmentChange;

    Bukkit.getPluginManager().registerEvents(this, DreamContext.getPlugin());
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public void play(@NotNull Player player) {
    playFrom(player, 0, 0);
  }

  @Override public void play(List<Player> players) { players.forEach(this::play); }

  @Override public void play() { Bukkit.getOnlinePlayers().forEach(this::play); }

  @Override
  public void stop(@NotNull Player player) {
    if (!isPlaying(player)) return;

    this.playing.remove(player.getUniqueId());
    restorePlayerState(player);
  }

  @Override
  public void stop(@NotNull List<@NotNull Player> players) {
    players.forEach(this::stop);
  }

  @Override
  public void stop() {
    Bukkit.getOnlinePlayers().forEach(this::stop);
  }

  @Override
  public boolean isPlaying(@NotNull Player player) {
    return this.playing.getOrDefault(player.getUniqueId(), false);
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  public void onQuit(final @NotNull PlayerQuitEvent event) {
    final var player = event.getPlayer();
    if (!isPlaying(player)) return;

    switch (reconnectBehavior) {
      case RESTART, RESUME_PERSONAL, RESUME_GLOBAL -> pendingReplay.add(player.getUniqueId());
      case SKIP_TO_END -> {}
    }

    playing.remove(player.getUniqueId());
  }

  @EventHandler
  private void onJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();
    final var uuid = player.getUniqueId();
    if (!this.pendingReplay.remove(uuid)) return;

    Bukkit.getScheduler().runTaskLater(DreamContext.getPlugin(), () -> {
      switch (this.reconnectBehavior) {
        case RESTART -> play(player);

        case RESUME_PERSONAL -> {
          ResumeState state = this.resumeStates.get(uuid);
          if (state != null)
            playFrom(player, state.segmentIndex(), state.frameIndex());
          else
            play(player);
        }

        case RESUME_GLOBAL -> {
          playFrom(player, this.globalProgress.segmentIndex(), this.globalProgress.frameIndex());
        }

        case SKIP_TO_END -> finish(player);
      }
    }, 10L);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void restorePlayerState(final @NotNull Player player) {
    if (this.originalGameMode.containsKey(player.getUniqueId()))
      player.setGameMode(this.originalGameMode.remove(player.getUniqueId()));

    if (this.copyInventory && this.savedInventory.containsKey(player.getUniqueId()))
      player.getInventory().setContents(this.savedInventory.remove(player.getUniqueId()));
  }

  private void playFrom(final @NotNull Player player, final int segmentStart, final int frameStart) {
    if (isPlaying(player)) return;

    this.playing.put(player.getUniqueId(), true);
    this.startLocation.putIfAbsent(player.getUniqueId(), player.getLocation().clone());
    this.originalGameMode.putIfAbsent(player.getUniqueId(), player.getGameMode());

    if (this.copyInventory && !this.savedInventory.containsKey(player.getUniqueId())) {
      this.savedInventory.put(player.getUniqueId(), player.getInventory().getContents().clone());
      player.getInventory().clear();
    }

    if (this.forcedGameMode != null)
      player.setGameMode(this.forcedGameMode);

    if (this.onStart != null && segmentStart == 0) this.onStart.accept(player);
    playSegment(player, segmentStart, frameStart);
  }

  private void playSegment(final @NotNull Player player, final int segmentIndex, final int frameStart) {
    if (!isPlaying(player)) return;

    if (segmentIndex >= segments.size()) {
      finish(player);
      return;
    }

    if (this.onSegmentChange != null && frameStart == 0)
      this.onSegmentChange.accept(player, segmentIndex);

    final var seg = segments.get(segmentIndex);

    if (seg instanceof CameraSegmentImpl segmentImpl) {
      segmentImpl.playFromFrame(player, frameStart, (p, frame) -> {
        final var state = new ResumeState(segmentIndex, frame);
        this.resumeStates.put(p.getUniqueId(), state);
        this.globalProgress = state;
      }, () -> playSegment(player, segmentIndex + 1, 0));
    } else
      seg.play(player, () -> playSegment(player, segmentIndex + 1, 0));
  }

  private void finish(final @NotNull Player player) {
    this.playing.remove(player.getUniqueId());
    this.resumeStates.remove(player.getUniqueId());

    if (this.forcedGameMode != null && this.originalGameMode.containsKey(player.getUniqueId()))
      player.setGameMode(this.originalGameMode.remove(player.getUniqueId()));

    if (this.copyInventory && this.savedInventory.containsKey(player.getUniqueId()))
      player.getInventory().setContents(this.savedInventory.remove(player.getUniqueId()));

    final var start = this.startLocation.remove(player.getUniqueId());
    if (this.endAt != null) player.teleport(this.endAt);
    else if (this.returnToStart && start != null) player.teleport(start);

    if (this.onEnd != null) this.onEnd.accept(player);
  }

}
