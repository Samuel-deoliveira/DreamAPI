package fr.dreamin.dreamapi.core.item.listener;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.core.item.event.PlayerItemUseEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class PlayerItemListener implements Listener {

  // ################################################################
  // ---------------------- REGISTER METHODS ------------------------
  // ################################################################

  private static final double REACH_EXTRA = 0.5;
  private static final double RAY_SIZE = 0.3;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @EventHandler
  public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) return;
    final var player = event.getPlayer();
    final var action = toActionType(event.getAction(), player.isSneaking());
    process(player, action, null, event.getClickedBlock(), event.getBlockFace(), event);
  }

  @EventHandler
  public void onPlayerInteractAtEntity(final @NotNull PlayerInteractAtEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) return;
    final var player = event.getPlayer();
    PlayerItemUseEvent.ActionType action = player.isSneaking()
      ? PlayerItemUseEvent.ActionType.SHIFT_RIGHT
      : PlayerItemUseEvent.ActionType.RIGHT;
    process(player, action, event.getRightClicked(), null, null, event);
  }

  @EventHandler
  public void onEntityDamageByEntity(final @NotNull EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) return;
    PlayerItemUseEvent.ActionType action = player.isSneaking()
      ? PlayerItemUseEvent.ActionType.SHIFT_LEFT
      : PlayerItemUseEvent.ActionType.LEFT;
    process(player, action, event.getEntity(), null, null, event);
  }

  @EventHandler
  public void onPlayerSwap(final @NotNull PlayerSwapHandItemsEvent event) {
    if (event.getOffHandItem().isEmpty()) return;
    final var player = event.getPlayer();
    process(player, player.isSneaking() ? PlayerItemUseEvent.ActionType.SHIFT_SWAP : PlayerItemUseEvent.ActionType.SWAP, null, null, null, event);
    event.setCancelled(true);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void process(final @NotNull Player player, final @NotNull PlayerItemUseEvent.ActionType action, final @Nullable Entity hitEntity, final @Nullable Block hitBlock, final @Nullable BlockFace hitFace, final @NotNull Event rawEvent) {
    final var item = player.getInventory().getItemInMainHand();
    if (item.isEmpty()) return;

    final var isRight = action == PlayerItemUseEvent.ActionType.RIGHT || action == PlayerItemUseEvent.ActionType.SHIFT_RIGHT;
    if (rawEvent instanceof PlayerInteractEvent && isRight) {
      final var reach = Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).getValue() + REACH_EXTRA;
      final var rtr = player.getWorld().rayTraceEntities(
        player.getEyeLocation(),
        player.getEyeLocation().getDirection(),
        reach,
        RAY_SIZE,
        e -> !e.equals(player)
      );
      if (rtr != null && rtr.getHitEntity() != null) return;
    }

    DreamAPI.getAPI().callEvent(
      new PlayerItemUseEvent(player, item, action, hitEntity, hitBlock, hitFace)
    );
  }

  private static PlayerItemUseEvent.ActionType toActionType(final @NotNull Action action, final boolean sneaking) {
    final var left = action.isLeftClick();
    if (sneaking)
      return left ? PlayerItemUseEvent.ActionType.SHIFT_LEFT : PlayerItemUseEvent.ActionType.SHIFT_RIGHT;

    return left ? PlayerItemUseEvent.ActionType.LEFT : PlayerItemUseEvent.ActionType.RIGHT;
  }

}
