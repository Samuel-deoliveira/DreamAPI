package fr.dreamin.dreamapi.core.cuboid.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.cuboid.Cuboid;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidEnterEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidLeaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@DreamAutoService(value= CuboidService.class)
public class CuboidServiceImpl implements CuboidService, DreamService, Listener {

  private final @NotNull Set<Cuboid> cuboids = new HashSet<>();
  private final @NotNull Map<UUID, Set<Cuboid>> playerCuboids = new HashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void onClose() {
    HandlerList.unregisterAll(this);
    clear();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void register(@NotNull Cuboid cuboid) {
    this.cuboids.add(cuboid);
  }

  @Override
  public void unregister(@NotNull Cuboid cuboid) {
    this.cuboids.remove(cuboid);
  }

  @Override
  public void clear() {
    this.cuboids.clear();
    this.playerCuboids.clear();
  }

  @Override
  public @NotNull Set<Cuboid> getCuboids() {
    return Collections.unmodifiableSet(this.cuboids);
  }

  @Override
  public @NotNull Set<Cuboid> getCuboidsOf(@NotNull UUID uuid) {
    return playerCuboids.getOrDefault(uuid, Set.of());
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onPlayerMove(final @NotNull PlayerMoveEvent event) {
    var player = event.getPlayer();
    var from = event.getFrom();
    var to = event.getTo();
    if ((from.getBlockX() == to.getBlockX() &&
        from.getBlockY() == to.getBlockY() &&
        from.getBlockZ() == to.getBlockZ()))
      return;

    var currentCuboids = playerCuboids.computeIfAbsent(player.getUniqueId(), id -> new HashSet<>());

    for (Cuboid cuboid : cuboids) {
      boolean wasIn = currentCuboids.contains(cuboid);
      boolean isIn = cuboid.isLocationIn(to);

      if (!wasIn && isIn)
        enter(player, cuboid, event, currentCuboids);
      else if (wasIn && !isIn)
        leave(player, cuboid, event, currentCuboids);
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void enter(
    final @NotNull Player player,
    final @NotNull Cuboid cuboid,
    final @NotNull PlayerMoveEvent event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var enterEvent = new CuboidEnterEvent(player, cuboid);
    if (DreamAPI.getAPI().callEvent(enterEvent).isCancelled())
      event.setCancelled(true);
    else
      currentCuboids.add(cuboid);
  }

  private void leave(
    final @NotNull Player player,
    final @NotNull Cuboid cuboid,
    final @NotNull PlayerMoveEvent event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var leaveEvent = new CuboidLeaveEvent(player, cuboid);
    if (DreamAPI.getAPI().callEvent(leaveEvent).isCancelled())
      event.setCancelled(true);
    else
      currentCuboids.remove(cuboid);
  }

}
