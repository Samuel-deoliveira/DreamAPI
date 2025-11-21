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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ParticleAnimation - A task to render a single ParticleShape along a ParticlePath over time.
 * <p>
 * This class handles the animation of a fixed shape moving along a path, with support for
 * looping, reversing, and progressive drawing of the shape.
 */
@Getter
@Accessors(fluent = true)
public final class ParticleAnimation extends TickTask<ParticleAnimation> {

  private final Particle particle;
  private final ParticleShape shape;
  private final ParticlePath path;
  private final int duration;
  private final double speed;
  private final boolean loop;
  private final boolean reverse;
  private final boolean progressiveDraw;
  private final InterpolationType interpolationType;

  private final ParticleShape.ParticleOptions options;

  private List<Location> frames;
  private int index = 0;

  private ParticleAnimation(Builder builder) {
    super(builder);
    this.particle = builder.particle;
    this.shape = builder.shape;
    this.path = builder.path;
    this.duration = builder.duration;
    this.speed = builder.speed;
    this.loop = builder.loop;
    this.reverse = builder.reverse;
    this.progressiveDraw = builder.progressiveDraw;
    this.interpolationType = builder.interpolationType;
    this.options = builder.options;

    // L'interpolationType est appliqué au path uniquement si le path n'est pas ParticlePath.none()
    // Le path est déjà configuré avec son propre easing.
    this.frames = path.generateFrames(duration);
    // Si pas de path, on s'assure d'avoir au moins une frame à la location par défaut
    if (path == ParticlePath.none() && this.frames.isEmpty()) {
      this.frames = List.of(new Location(null, 0, 0, 0));
    }
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onStart() {
    this.index = 0;
    if (frames.isEmpty()) {
      stop();
    }
  }

  @Override
  public void onTick() {
    if (index >= frames.size()) {
      if (this.loop) {
        index = 0;
        if (reverse) swap();
      } else {
        stop();
        return;
      }
    }

    if (index < frames.size()) {
      Location base = frames.get(index);

      if (progressiveDraw) {
        List<Vector> shapePoints = shape.sample();
        int pointsToDraw = shapePoints.size();

        int currentCycleIndex = index;
        if (reverse && loop && (current() / duration) % 2 != 0)
          currentCycleIndex = frames.size() - 1 - index;

        double progress = (double) currentCycleIndex / (frames.size() - 1);

        if (progress > 1.0) progress = 1.0;

        pointsToDraw = (int) Math.ceil((double) shapePoints.size() * progress);

        if (currentCycleIndex == frames.size() - 1)
          pointsToDraw = shapePoints.size();

        for (int i = 0; i < pointsToDraw; i++) {
          Vector offset = shapePoints.get(i);
          Location particleLocation = base.clone().add(offset);

          if (particle == Particle.DUST && options.color() != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(
              org.bukkit.Color.fromRGB(
                options.color().getRed(),
                options.color().getGreen(),
                options.color().getBlue()
              ),
              options.size()
            );
            base.getWorld().spawnParticle(
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
            base.getWorld().spawnParticle(
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

      } else {
        if (particle == Particle.DUST && options.color() != null)
          shape.render(base, options);
        else
          shape.render(particle, base);
      }
      index++;
    }
  }


  /**
   * Starts the animation at the current location of the path.
   * If the path is ParticlePath.none(), the animation will be static.
   */
  /**
   * Starts the animation.
   * <p>
   * The animation will be visible to all players by default.
   */
  public void play() {
    this.start();
  }

  /**
   * Starts the animation for a specific player.
   * @param player The player who will see the particles.
   */
  public void play(@NotNull Player player) {
    this.shape.viewer(player);
    this.start();
  }

  /**
   * Starts the animation for a collection of players.
   * @param players The collection of players who will see the particles.
   */
  public void play(@NotNull Collection<Player> players) {
    players.forEach(this.shape::viewer);
    this.start();
  }

  /**
   * Reverses the order of the path frames for the reverse effect.
   */
  private void swap() {
    if (frames == null || frames.isEmpty()) return;

    Collections.reverse(frames);

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
  public static class Builder extends TickTask.Builder<ParticleAnimation, Builder> {
    private Particle particle = Particle.FLAME;
    private ParticleShape shape = ParticleShapes.point();
    private ParticleShape.ParticleOptions options = ParticleShape.ParticleOptions.defaultOptions();
    private ParticlePath path = ParticlePath.none();
    private int duration = 40;
    private double speed = 1;
    private boolean loop = false;
    private boolean reverse = false;
    private boolean progressiveDraw = false;
    private InterpolationType interpolationType = InterpolationType.LINEAR;


    public Builder particle(Particle particle) { this.particle = particle; return this; }
    public Builder shape(ParticleShape shape) { this.shape = shape; return this; }
    public Builder path(ParticlePath path) { this.path = path; return this; }
    public Builder options(ParticleShape.ParticleOptions options) { this.options = options; return this; }
    public Builder duration(int ticks) { this.duration = ticks; return this; }
    public Builder speed(double speed) { this.speed = speed; return this; }
    public Builder loop(boolean loop) { this.loop = loop; return this; }
    public Builder reverse(boolean reverse) { this.reverse = reverse; return this; }
    public Builder progressiveDraw(boolean progressiveDraw) { this.progressiveDraw = progressiveDraw; return this; }
    public Builder interpolation(InterpolationType type) { this.interpolationType = type; return this; }


    @Override
    public ParticleAnimation build() {
      // Configure TickTask properties
      long tickInterval = (long) (1 / this.speed);
      if (tickInterval < 1) tickInterval = 1;
      this.every(tickInterval);

      if (!this.loop) {
        this.limit(this.duration);
        this.autoStop(true);
      }

      return new ParticleAnimation(this);
    }
  }
}