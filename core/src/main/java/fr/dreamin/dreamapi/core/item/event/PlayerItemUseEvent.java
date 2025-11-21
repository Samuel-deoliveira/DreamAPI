package fr.dreamin.dreamapi.core.item.event;

import fr.dreamin.dreamapi.core.event.ToolsEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@RequiredArgsConstructor
public final class PlayerItemUseEvent extends ToolsEvent {

  public enum ActionType {LEFT, RIGHT, SWAP, SHIFT_LEFT, SHIFT_RIGHT, SHIFT_SWAP}

  private final Player player;
  private final ItemStack is;
  private final ActionType action;
  @Nullable private final Entity clickedEntity;
  @Nullable private final Block clickedBlock;
  @Nullable private final BlockFace clickedFace;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public boolean isLeftClick()  { return action == ActionType.LEFT  || action == ActionType.SHIFT_LEFT; }

  public boolean isRightClick() { return action == ActionType.RIGHT || action == ActionType.SHIFT_RIGHT; }

  public boolean isShiftClick() { return action == ActionType.SHIFT_LEFT || action == ActionType.SHIFT_RIGHT; }

  public boolean isSwap() { return action == ActionType.SWAP; }

  public boolean hasBlock() { return clickedBlock != null; }

  public boolean hasEntity() { return clickedEntity != null; }

}
