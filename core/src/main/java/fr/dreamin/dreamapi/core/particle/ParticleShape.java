package fr.dreamin.dreamapi.core.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * ParticleShape - Interface for defining a particle shape and its rendering logic.
 * <p>
 * Implementations should define how particles are spawned to form a specific shape
 * and provide a {@link #sample()} method for morphing and progressive drawing.
 */


/**
 * ParticleShape - Interface for defining a particle shape and its rendering logic.
 * <p>
 * Implementations should define how particles are spawned to form a specific shape
 * and provide a {@link #sample()} method for morphing and progressive drawing.
 */
public interface ParticleShape {

  /**
   * Renders the shape using the provided particle type and base location.
   * <p>
   * This method is typically used for particles that do not support custom options (like color/size).
   *
   * @param particle The particle type to spawn.
   * @param base The center/base location for the shape.
   */
  void render(final @NotNull Particle particle, final @NotNull Location base);

  /**
   * Render the shape using the provided particle type and base location.
   *
   * @param base     Center/base location
   * @param options  Optional shape options (color, size, offset, etc.)
   */
  void render(@NotNull Location base, @NotNull ParticleOptions options);

  /**
   * Sets an optional viewer for per-player rendering.
   * <p>
   * This is useful for effects visible to specific players only.
   *
   * @param player The player who should see the shape.
   * @return The ParticleShape instance (for chaining).
   */
  default ParticleShape viewer(final @NotNull Player player) {
    return this;
  }

  /**
   * Returns a list of points (Vector) representing the shape's geometry.
   * <p>
   * This method is crucial for interpolations (morphing) and progressive drawing.
   * The returned list should be deterministic (same points for the same shape parameters).
   *
   * @return A list of Vectors representing the shape's points relative to the origin (0, 0, 0).
   */
  default @NotNull List<Vector> sample() {
    return List.of(new Vector(0, 0, 0));
  }

  /**
   * Configuration for custom particle rendering (color, size, offset, etc.)
   */
  record ParticleOptions(@Nullable Color color, float size, @NotNull Vector offset, int count) {
    public static ParticleOptions defaultOptions() {
      return new ParticleOptions(null, 1f, new Vector(0, 0, 0), 1);
    }

    public static ParticleOptions ofColor(Color color) {
      return new ParticleOptions(color, 1f, new Vector(0, 0, 0), 1);
    }

    public ParticleOptions size(float s) {
      return new ParticleOptions(color, s, offset, count);
    }

    public ParticleOptions offset(double x, double y, double z) {
      return new ParticleOptions(color, size, new Vector(x, y, z), count);
    }

    public ParticleOptions count(int count) {
      return new ParticleOptions(color, size, offset, count);
    }
  }
}