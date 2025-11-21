package fr.dreamin.dreamapi.core.particle;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import fr.dreamin.dreamapi.core.time.TickTask;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ParticleShapeMorph - Animates the progressive transformation (morphing) of a shape from 'fromShape' to 'toShape'.
 * <p>
 * This class handles the animation of a shape morphing while moving along a ParticlePath,
 * with support for looping, reversing, and progressive drawing.
 */
@Getter
@Accessors(fluent = true)
public final class ParticleShapeMorph extends TickTask<ParticleShapeMorph> {

  private final Particle particle;
  private final ParticleShape fromShape;
  private final ParticleShape toShape;
  private final ParticlePath path;
  private final int duration;
  private final double speed;
  private final boolean loop;
  private final boolean reverse;
  private final boolean progressiveDraw;
  private final InterpolationType interpolationType;
  private final ParticleShape.ParticleOptions options;

  private List<List<Vector>> morphFrames;
  private List<Location> pathFrames;
  private int index = 0;

  private ParticleShapeMorph(Builder builder) {
    super(builder);
    this.particle = builder.particle;
    this.fromShape = builder.fromShape;
    this.toShape = builder.toShape;
    this.path = builder.path;
    this.duration = builder.duration;
    this.speed = builder.speed;
    this.loop = builder.loop;
    this.reverse = builder.reverse;
    this.progressiveDraw = builder.progressiveDraw;
    this.interpolationType = builder.interpolationType;
    this.options = builder.options;
    this.morphFrames = generateMorphFrames();
    // Le path est déjà configuré avec son propre easing.
    this.pathFrames = path.generateFrames(duration);
    // Si pas de path, on s'assure d'avoir au moins une frame à la location par défaut
    if (path == ParticlePath.none() && this.pathFrames.isEmpty()) {
      this.pathFrames = List.of(new Location(null, 0, 0, 0));
    }
  }

  // ###############################################################
  // -------------------- FRAME GENERATION -------------------------
  // ###############################################################

  private List<List<Vector>> generateMorphFrames() {
    List<Vector> fromPoints = fromShape.sample();
    List<Vector> toPoints = toShape.sample();

    int targetCount = Math.max(fromPoints.size(), toPoints.size());
    fromPoints = normalizePointCount(fromPoints, targetCount);
    toPoints = normalizePointCount(toPoints, targetCount);

    List<List<Vector>> frames = new ArrayList<>(duration + 1);

    for (int step = 0; step <= duration; step++) {
      double t = Interpolation.applyEasing((double) step / duration, interpolationType);
      List<Vector> frame = new ArrayList<>(targetCount);

      for (int i = 0; i < targetCount; i++) {
        Vector interpolated = Interpolation.lerp(fromPoints.get(i), toPoints.get(i), t);
        frame.add(interpolated);
      }

      frames.add(frame);
    }

    return frames;
  }

  private List<Vector> normalizePointCount(List<Vector> points, int targetCount) {
    if (points.isEmpty()) {
      List<Vector> normalized = new ArrayList<>(targetCount);
      for (int i = 0; i < targetCount; i++) {
        normalized.add(new Vector(0, 0, 0));
      }
      return normalized;
    }

    if (points.size() == targetCount)
      return new ArrayList<>(points);

    List<Vector> normalized = new ArrayList<>(targetCount);

    if (points.size() < targetCount) {
      for (int i = 0; i < targetCount; i++) {
        int sourceIndex = (i * points.size()) / targetCount;
        normalized.add(points.get(sourceIndex).clone());
      }
    } else {
      for (int i = 0; i < targetCount; i++) {
        int sourceIndex = (i * points.size()) / targetCount;
        normalized.add(points.get(sourceIndex).clone());
      }
    }

    return normalized;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onStart() {
    this.index = 0;
    if (morphFrames.isEmpty() || pathFrames.isEmpty()) {
      stop();
    }
  }

  @Override
  public void onTick() {
    if (index >= morphFrames.size() || index >= pathFrames.size()) {
      if (this.loop) {
        index = 0;
        if (reverse) swap();
      } else {
        stop();
        return;
      }
    }

    if (index < morphFrames.size() && index < pathFrames.size()) {
      List<Vector> currentFrame = morphFrames.get(index);
      Location baseLocation = pathFrames.get(index);

      int pointsToDraw = currentFrame.size();
      if (progressiveDraw) {

        int currentCycleIndex = index;
        if (reverse && loop && (current() / duration) % 2 != 0) {

          currentCycleIndex = morphFrames.size() - 1 - index;
        }
        double progress = (double) currentCycleIndex / (morphFrames.size() - 1);

        pointsToDraw = (int) Math.ceil((double) currentFrame.size() * progress);

        if (currentCycleIndex == morphFrames.size() - 1) {
          pointsToDraw = currentFrame.size();
        }

      }

      for (int i = 0; i < pointsToDraw; i++) {
        Vector offset = currentFrame.get(i);
        Location particleLocation = baseLocation.clone().add(offset);

        if (particle == Particle.DUST && options.color() != null) {
          Particle.DustOptions dustOptions = new Particle.DustOptions(
            org.bukkit.Color.fromRGB(
              options.color().getRed(),
              options.color().getGreen(),
              options.color().getBlue()
            ),
            options.size()
          );
          baseLocation.getWorld().spawnParticle(
            particle,
            particleLocation,
            options.count(),
            options.offset().getX(),
            options.offset().getY(),
            options.offset().getZ(),
            0,
            dustOptions
          );
        } else {
          baseLocation.getWorld().spawnParticle(
            particle,
            particleLocation,
            options.count(),
            options.offset().getX(),
            options.offset().getY(),
            options.offset().getZ(),
            0
          );
        }
      }

      index++;
    }
  }

  /**
   * Starts the morphing animation.
   * <p>
   * The animation will be visible to all players by default.
   */
  public void play() {
    this.start();
  }

  /**
   * Starts the morphing animation for a specific player.
   * @param player The player who will see the particles.
   */
  public void play(@NotNull Player player) {
    this.fromShape.viewer(player);
    this.toShape.viewer(player);
    this.start();
  }

  /**
   * Starts the morphing animation for a collection of players.
   * @param players The collection of players who will see the particles.
   */
  public void play(@NotNull Collection<Player> players) {
    players.forEach(this.fromShape::viewer);
    players.forEach(this.toShape::viewer);
    this.start();
  }

  /**
   * Reverses the order of the morph and path frames for the reverse effect.
   */
  private void swap() {
    if (morphFrames == null || morphFrames.isEmpty()) return;
    if (pathFrames == null || pathFrames.isEmpty()) return;

    Collections.reverse(morphFrames);
    Collections.reverse(pathFrames);
    index = 0;
  }

  // ###############################################################
  // --------------------------- BUILDER ---------------------------
  // ###############################################################

  public static Builder create() {
    return new Builder();
  }



  @Getter
  @Accessors(fluent = true)
  public static class Builder extends TickTask.Builder<ParticleShapeMorph, Builder> {
    private Particle particle = Particle.FLAME;
    private ParticleShape fromShape = ParticleShapes.point();
    private ParticleShape toShape = ParticleShapes.point();
    private ParticlePath path = ParticlePath.none();
    private ParticleShape.ParticleOptions options = ParticleShape.ParticleOptions.defaultOptions();
    private int duration = 40;
    private double speed = 1;
    private boolean loop = false;
    private boolean reverse = false;
    private boolean progressiveDraw = false;
    private InterpolationType interpolationType = InterpolationType.LINEAR;

    public Builder particle(Particle particle) {
      this.particle = particle;
      return this;
    }

    public Builder fromShape(ParticleShape shape) {
      this.fromShape = shape;
      return this;
    }

    public Builder toShape(ParticleShape shape) {
      this.toShape = shape;
      return this;
    }

    public Builder path(ParticlePath path) {
      this.path = path;
      return this;
    }

    public Builder options(ParticleShape.ParticleOptions options) {
      this.options = options;
      return this;
    }

    public Builder duration(int ticks) {
      this.duration = ticks;
      return this;
    }

    public Builder speed(double speed) {
      this.speed = speed;
      return this;
    }

    public Builder loop(boolean loop) {
      this.loop = loop;
      return this;
    }

    public Builder reverse(boolean reverse) {
      this.reverse = reverse;
      return this;
    }

    public Builder progressiveDraw(boolean progressiveDraw) {
      this.progressiveDraw = progressiveDraw;
      return this;
    }

    public Builder interpolation(InterpolationType type) {
      this.interpolationType = type;
      return this;
    }

    @Override
    public ParticleShapeMorph build() {
      long tickInterval = (long) (1 / this.speed);
      if (tickInterval < 1) tickInterval = 1;
      this.every(tickInterval);

      if (!this.loop) {
        this.limit(this.duration);
        this.autoStop(true);
      }

      return new ParticleShapeMorph(this);
    }
  }
}