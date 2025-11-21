package fr.dreamin.dreamapi.core.glowing.event.set;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class GlowingEntitySetEvent extends GlowingSetEvent {

  private final @NotNull Entity target;

  public GlowingEntitySetEvent(@NotNull Player viewer, @NotNull ChatColor color, @NotNull Entity target) {
    super(viewer, color);
    this.target = target;
  }
}
