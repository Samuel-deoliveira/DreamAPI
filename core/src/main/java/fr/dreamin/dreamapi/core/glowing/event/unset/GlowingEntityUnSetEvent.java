package fr.dreamin.dreamapi.core.glowing.event.unset;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class GlowingEntityUnSetEvent extends GlowingUnSetEvent {

  private final @NotNull Entity target;

  public GlowingEntityUnSetEvent(@NotNull Player viewer, @NotNull Entity target) {
    super(viewer);
    this.target = target;
  }
}
