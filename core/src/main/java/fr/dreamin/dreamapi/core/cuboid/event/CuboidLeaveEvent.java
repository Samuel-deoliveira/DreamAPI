package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.core.cuboid.Cuboid;
import fr.dreamin.dreamapi.core.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString
@Getter
@RequiredArgsConstructor
public final class CuboidLeaveEvent extends ToolsCancelEvent {

  private final @NotNull Player player;
  private final @NotNull Cuboid cuboid;

}
