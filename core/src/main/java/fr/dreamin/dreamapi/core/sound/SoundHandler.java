package fr.dreamin.dreamapi.core.sound;

import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.annotations.Internal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Flexible sound handler for playing and managing sounds for players.
 * Supports Bukkit and custom sounds, with builder-based configuration.
 */
@Getter
@Setter
@RequiredArgsConstructor
@Internal
public final class SoundHandler {

  private static final @NotNull Map<String, Optional<Sound>> SOUND_CACHE = new ConcurrentHashMap<>();

  private final @NotNull String label;
  private float volume = 1.0f;
  private float pitch = 1.0f;
  private SoundCategory category = SoundCategory.MASTER;
  private double range = -1;
  private long delayTicks = 0;
  private int loopCount = 1;

  // ###############################################################
  // ------------------------ BUILDER API --------------------------
  // ###############################################################

  public static Builder builder(@NotNull String label) {
    return new Builder(label);
  }

  public static Builder builder(@NotNull Sound sound) {
    return new Builder(sound.name());
  }

  @Getter
  @Setter
  public static class Builder {
    private final String label;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private SoundCategory category = SoundCategory.MASTER;
    private double range = -1;
    private long delayTicks = 0;
    private int loopCount = 1;

    public Builder(@NotNull String label) {
      this.label = label;
    }

    public Builder volume(float volume) { this.volume = volume; return this; }
    public Builder pitch(float pitch) { this.pitch = pitch; return this; }
    public Builder category(SoundCategory category) { this.category = category; return this; }
    public Builder range(double range) { this.range = range; return this; }
    public Builder delay(long delayTicks) { this.delayTicks = delayTicks; return this; }
    public Builder loop(int count) { this.loopCount = count; return this; }

    public SoundHandler build() {
      SoundHandler handler = new SoundHandler(label);
      handler.volume = volume;
      handler.pitch = pitch;
      handler.category = category;
      handler.range = range;
      handler.delayTicks = delayTicks;
      handler.loopCount = loopCount;
      return handler;
    }
  }

  // ###############################################################
  // ---------------------- SOUND CONTROL --------------------------
  // ###############################################################

  public void play(final @NotNull Player player) {
    play(player, player.getLocation());
  }

  public void play(final @NotNull Player player, final @NotNull Location location) {
    if (delayTicks <= 0) playInternal(player, location);
    else Bukkit.getScheduler().runTaskLater(DreamContext.getPlugin(), () -> playInternal(player, location), delayTicks);
  }

  public void play(final @NotNull Collection<? extends Player> players, final @NotNull Location location) {
    players.forEach(p -> play(p, location));
  }

  public void broadcast(final @NotNull Location location) {
    play(Bukkit.getOnlinePlayers(), location);
  }

  public void stop(final @NotNull Player player) {
    getSound().ifPresentOrElse(
      player::stopSound,
      () -> player.stopSound(label)
    );
  }

  public void stop(final @NotNull Collection<? extends Player> players) {
    players.forEach(this::stop);
  }

  // ###############################################################
  // ----------------------- INTERNAL LOGIC ------------------------
  // ###############################################################

  private void playInternal(@NotNull Player player, @NotNull Location location) {
    if (!isWithinRange(player, location)) return;

    for (int i = 0; i < loopCount; i++) {
      getSound().ifPresentOrElse(
        sound -> player.playSound(location, sound, category, volume, pitch),
        () -> player.playSound(location, label, category, volume, pitch)
      );
    }
  }

  private boolean isWithinRange(@NotNull Player player, @NotNull Location location) {
    if (range < 0) return true;
    if (!Objects.equals(player.getWorld(), location.getWorld())) return false;
    return player.getLocation().distanceSquared(location) <= range * range;
  }

  private Optional<Sound> getSound() {
    return SOUND_CACHE.computeIfAbsent(label.toUpperCase(Locale.ROOT), key -> {
      try {
        return Optional.of(Sound.valueOf(key));
      } catch (IllegalArgumentException e) {
        return Optional.empty();
      }
    });
  }

  // ###############################################################
  // ---------------------- UTILITY HELPERS ------------------------
  // ###############################################################

  /** Returns a clone with modified pitch. */
  public SoundHandler cloneWithPitch(float newPitch) {
    SoundHandler copy = new SoundHandler(label);
    copy.volume = this.volume;
    copy.pitch = newPitch;
    copy.category = this.category;
    copy.range = this.range;
    copy.delayTicks = this.delayTicks;
    copy.loopCount = this.loopCount;
    return copy;
  }

  /** Plays the sound only if the player passes a condition. */
  public void playIf(@NotNull Player player, @NotNull Predicate<Player> condition) {
    if (condition.test(player)) play(player);
  }

  /** Creates a preconfigured sound handler for note or click effects. */
  public static SoundHandler simple(@NotNull Sound sound) {
    return builder(sound).volume(1f).pitch(1f).build();
  }

  /** Creates a handler for ambient background sounds. */
  public static SoundHandler ambient(@NotNull String soundName) {
    return builder(soundName)
      .category(SoundCategory.AMBIENT)
      .volume(0.5f)
      .pitch(1f)
      .build();
  }

}
