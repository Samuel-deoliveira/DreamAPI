package fr.dreamin.dreamapi.api.animation;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface CinematicBuilder {

  CinematicBuilder camera(final @NotNull Location start, final @NotNull Location end, final @NotNull Duration duration);
  CinematicBuilder camera(final @NotNull Location start, final @NotNull Location end, final @NotNull Duration duration, @NotNull InterpolationType type);

  CinematicBuilder returnToStart(final boolean enabled);
  CinematicBuilder endAt(final @NotNull Location location);

  CinematicBuilder gameMode(final @NotNull GameMode gameMode);
  CinematicBuilder copyInventory(boolean enabled);
  CinematicBuilder reconnectBehavior(final @NotNull ReconnectBehavior behavior);


  CinematicBuilder onStart(final @NotNull Consumer<Player> callback);
  CinematicBuilder onEnd(final @NotNull Consumer<Player> callback);
  CinematicBuilder onSegmentChange(final @NotNull BiConsumer<Player, Integer> callback);

  Cinematic build();

}
