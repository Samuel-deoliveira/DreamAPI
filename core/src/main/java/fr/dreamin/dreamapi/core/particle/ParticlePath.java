package fr.dreamin.dreamapi.core.particle;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * ParticlePath - Defines a path (sequence of locations) for a particle animation to follow.
 * <p>
 * This class uses interpolation to generate the path frames over a given number of steps.
 */
@Getter
public class ParticlePath {

  private final Location start;
  private final Location end;
  private final InterpolationType ease;

  private ParticlePath(final Location start, final Location end, final @NotNull InterpolationType ease) {
    this.start = start;
    this.end = end;
    this.ease = ease;
  }

  /**
   * Generates the list of Location frames for the path.
   * @param steps The number of steps (frames) to generate.
   * @return A list of interpolated Locations.
   */
  public List<Location> generateFrames(final int steps) {
    if (start == null || end == null) return Collections.emptyList();
    return Interpolation.between(start, end).over(steps).ease(ease).asLocations();
  }

  /**
   * Creates a path between two locations with linear interpolation by default.
   * @param start The starting location.
   * @param end The ending location.
   * @return A new ParticlePath instance.
   */
  public static ParticlePath between(final @NotNull Location start, final @NotNull Location end) {
    return new ParticlePath(start, end, InterpolationType.LINEAR);
  }

  /**
   * Creates a path that generates no frames (static animation).
   * @return A static ParticlePath instance.
   */
  public static ParticlePath none() {
    return new ParticlePath(null, null, InterpolationType.LINEAR) {
      @Override
      public List<Location> generateFrames(final int steps) {
        return Collections.emptyList();
      }

    };
  }

  /**
   * Sets the easing function for the path interpolation.
   * @param ease The interpolation type (easing function).
   * @return A new ParticlePath instance with the specified easing.
   */
  public ParticlePath ease(final @NotNull InterpolationType ease) {
    return new ParticlePath(start, end, ease);
  }


}