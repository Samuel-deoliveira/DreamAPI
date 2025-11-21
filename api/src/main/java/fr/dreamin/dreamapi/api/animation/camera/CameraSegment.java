package fr.dreamin.dreamapi.api.animation.camera;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;

public interface CameraSegment {
  Location start();
  Location end();
  Duration duration();
  InterpolationType easing();

  void playFromFrame(final @NotNull Player player, final int startFrame, final @Nullable BiConsumer<Player, Integer> onFrame, final @NotNull Runnable onComplete);

  void play(final @NotNull Player player, final @NotNull Runnable onComplete);
}
