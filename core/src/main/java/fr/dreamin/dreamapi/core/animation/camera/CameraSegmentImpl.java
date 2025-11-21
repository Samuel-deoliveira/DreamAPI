package fr.dreamin.dreamapi.core.animation.camera;

import fr.dreamin.dreamapi.api.animation.camera.CameraSegment;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;


public record CameraSegmentImpl(
  Location start,
  Location end,
  Duration duration,
  InterpolationType easing
) implements CameraSegment {

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void playFromFrame(@NotNull Player player, int startFrame, @Nullable BiConsumer<Player, Integer> onFrame, @NotNull Runnable onComplete) {
    final var steps = Math.max(1, (int) (duration.toMillis() / 50));
    final var frames = Interpolation.generate(start, end, steps, easing);

    new BukkitRunnable() {
      int i = Math.min(startFrame, frames.size() - 1);

      @Override
      public void run() {
        if (!player.isOnline()) {
          cancel();
          onComplete.run();
          return;
        }
        if (i >= frames.size()) {
          cancel();
          onComplete.run();
          return;
        }

        player.teleport(frames.get(i));

        if (onFrame != null)
          onFrame.accept(player, i);
        i++;
      }
    }.runTaskTimer(DreamContext.getPlugin(), 0L, 1L);
  }

  @Override
  public void play(@NotNull Player player, @NotNull Runnable onComplete) {
    playFromFrame(player, 0, null, onComplete);
  }
}