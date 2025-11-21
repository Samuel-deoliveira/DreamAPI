package fr.dreamin.dreamapi.core.glowing.event.unset;

import fr.dreamin.dreamapi.core.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class GlowingUnSetEvent extends ToolsCancelEvent {

  private final @NotNull Player viewer;

}
