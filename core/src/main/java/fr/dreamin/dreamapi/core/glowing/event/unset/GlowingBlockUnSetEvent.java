package fr.dreamin.dreamapi.core.glowing.event.unset;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class GlowingBlockUnSetEvent extends GlowingUnSetEvent {

  private final @NotNull Block target;

  public GlowingBlockUnSetEvent(@NotNull Player viewer, @NotNull Block target) {
    super(viewer);
    this.target = target;
  }
}
