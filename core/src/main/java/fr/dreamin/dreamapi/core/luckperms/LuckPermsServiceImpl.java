package fr.dreamin.dreamapi.core.luckperms;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.team.TeamService;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@DreamAutoService(value = LuckPermsService.class)
public class LuckPermsServiceImpl implements LuckPermsService, DreamService, Listener {

  private final @NotNull Plugin plugin;

  private final @NotNull TeamService teamService = DreamAPI.getAPI().getService(TeamService.class);

  private final @NotNull LuckPerms luckPerms;

  private Component msgKickError = Component.text("Une erreur interne est survenue lors de la connexion. Veuillez réessayer plus tard.",
    NamedTextColor.RED);

  private boolean enabled;

  @NotNull
  private final Map<UUID, CachedMetaData> metaData = new ConcurrentHashMap<>();

  public LuckPermsServiceImpl(final @NotNull Plugin plugin) {
    this.luckPerms = DreamAPI.getAPI().getService(LuckPerms.class);
    this.plugin = plugin;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled == this.enabled) return;
    this.enabled = enabled;

    if (this.enabled) {
      Bukkit.getPluginManager().registerEvents(this, this.plugin);

      for (var online : Bukkit.getOnlinePlayers()) {
        try {

          final var user = this.luckPerms.getUserManager().loadUser(online.getUniqueId()).get(3, TimeUnit.SECONDS);
          this.metaData.put(online.getUniqueId(), user.getCachedData().getMetaData());
          setPrefixSuffix(online);
          refreshPlayerVisuals(online);

        } catch (Exception e) {
          DreamAPI.getAPI().getLogger().warning("LuckPerms metadata load failed for " + online.getName());
        }

      }

    }
    else {
      HandlerList.unregisterAll(this);

      for (var online : Bukkit.getOnlinePlayers()) {
        online.displayName(online.name());
        online.playerListName(online.name());
        this.teamService.removeTeam(online);
      }

    }

    this.enabled = enabled;
  }

  @Override
  public Component getMsgKickError() {
    return this.msgKickError;
  }

  @Override
  public void setMsgKickError(Component msgKickError) {
    this.msgKickError = msgKickError;
  }

  @Override
  public CompletableFuture<String> getPrimaryGroup(@NotNull UUID uuid) {
    return this.luckPerms.getUserManager().loadUser(uuid)
      .thenApply(u -> u.getCachedData().getMetaData().getPrimaryGroup());
  }

  @Override
  public CompletableFuture<String> getPrefix(@NotNull UUID uuid) {
    return this.luckPerms.getUserManager().loadUser(uuid)
      .thenApply(u -> u.getCachedData().getMetaData().getPrefix());
  }

  @Override
  public CompletableFuture<String> getSuffix(@NotNull UUID uuid) {
    return this.luckPerms.getUserManager().loadUser(uuid)
      .thenApply(u -> u.getCachedData().getMetaData().getSuffix());
  }

  @Override
  public CompletableFuture<Void> setPermission(@NotNull UUID uuid, @NotNull String permission, boolean value) {
    return this.luckPerms.getUserManager().modifyUser(uuid, user -> user.data().add(Node.builder(permission).value(value).build()));
  }

  @Override
  public CompletableFuture<Void> removePermission(@NotNull UUID uuid, @NotNull String permission) {
    return this.luckPerms.getUserManager().modifyUser(uuid, user -> user.data().remove(Node.builder(permission).build()));
  }

  @Override
  public CompletableFuture<Void> applyTemporaryPermission(@NotNull UUID uuid, @NotNull String permission, boolean value, @NotNull Duration duration) {
    return this.luckPerms.getUserManager().modifyUser(uuid, user -> {
      user.data().add(Node.builder(permission)
        .expiry(duration)
        .value(value)
        .build());
    });
  }

  @Override
  public CompletableFuture<Void> schedulePermissionChange(@NotNull UUID uuid, @NotNull Runnable action, @NotNull Duration delay) {
    return CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(delay.toMillis());
        action.run();
      } catch (InterruptedException ignored) {}
    });
  }

  @Override
  public void refreshPlayerVisuals(@NotNull Player player) {
    getPrefix(player.getUniqueId()).thenCombine(getSuffix(player.getUniqueId()), (prefix, suffix) -> {
      this.teamService.updatePlayerDisplay(player, prefix, suffix);
      return null;
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void setPrefixSuffix(final Player player) {
    final var cachedMetaData = this.metaData.get(player.getUniqueId());

    if (cachedMetaData == null)
      return;

    final var prefix = cachedMetaData.getPrefix();
    final var suffix = cachedMetaData.getSuffix();

    Component prefixComponent = Component.empty(), suffixComponent = Component.empty();
    if (prefix != null)
      prefixComponent = DreamContext.LEGACY_COMPONENT_SERIALIZER.deserialize(prefix);
    if (suffix != null)
      suffixComponent = DreamContext.LEGACY_COMPONENT_SERIALIZER.deserialize(suffix);

    final var color = prefixComponent.color();

    player.displayName(
      prefixComponent.color(NamedTextColor.WHITE).append(player.name().color(color))
        .append(suffixComponent)
    );

    player.playerListName(
      prefixComponent.color(NamedTextColor.WHITE).append(player.name().color(color))
        .append(suffixComponent)
    );

  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onLogin(final @NotNull AsyncPlayerPreLoginEvent event) {
    if (!this.enabled) return;

    final UUID uniqueId = event.getUniqueId();

    try {
      final var user = this.luckPerms.getUserManager().loadUser(uniqueId).get(5, TimeUnit.SECONDS);

      this.metaData.put(uniqueId, user.getCachedData().getMetaData());
    } catch (Exception e) {
      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
      event.kickMessage(this.msgKickError);
    }
  }

  @EventHandler
  private void onJoin(final @NotNull PlayerJoinEvent event) {
    if (!this.enabled) return;

    final var player = event.getPlayer();

    setPrefixSuffix(player);
  }

  @EventHandler
  private void onChat(final @NotNull AsyncChatEvent event) {
    if (!this.enabled) return;

    event.renderer(ChatRenderer.viewerUnaware(new ChatRenderer.ViewerUnaware() {
      @Override
      public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message) {
        return sourceDisplayName.colorIfAbsent(NamedTextColor.WHITE)
          .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
          .append(message.colorIfAbsent(NamedTextColor.WHITE));
      }
    }));

  }

}
