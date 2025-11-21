package fr.dreamin.dreamapi.core.glowing.event.set;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class GlowingBlockSetEvent extends GlowingSetEvent {

  private final @NotNull Block target;

  public GlowingBlockSetEvent(@NotNull Player viewer, @NotNull ChatColor color, @NotNull Block target) {
    super(viewer, color);
    this.target = target;
  }
}
