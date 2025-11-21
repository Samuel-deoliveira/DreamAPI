package fr.dreamin.dreamapi.core.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ParticleShapes {

  // ###############################################################
  // ---------------------- BASIC SHAPES ----------------------------
  // ###############################################################

  /** Simple point (single particle). */
  public static ParticleShape point() {
    return new ParticleShape() {
      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawn(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @Nullable ParticleShape.ParticleOptions options) {
        spawn(Particle.DUST, base, options); // default redstone for color support
      }
    };
  }

  /** Circle in XZ plane. */
  public static ParticleShape circle(final double radius, final int points) {
    return new ParticleShape() {
      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        for (int i = 0; i < points; i++) {
          final var angle = 2 * Math.PI * i / points;
          final var x = Math.cos(angle) * radius;
          final var z = Math.sin(angle) * radius;
          spawn(particle, base.clone().add(x, 0, z), ParticleShape.ParticleOptions.defaultOptions());
        }
      }

      @Override
      public void render(final @NotNull Location base, final @Nullable ParticleShape.ParticleOptions options) {
        final var opts = options != null ? options : ParticleShape.ParticleOptions.defaultOptions();
        for (var i = 0; i < points; i++) {
          final var angle = 2 * Math.PI * i / points;
          final var x = Math.cos(angle) * radius;
          final var z = Math.sin(angle) * radius;
          spawn(Particle.DUST, base.clone().add(x, 0, z), opts);
        }
      }

      @Override
      public @NotNull List<Vector> sample() {
        List<Vector> pointsList = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
          double angle = 2 * Math.PI * i / points;
          double x = Math.cos(angle) * radius;
          double z = Math.sin(angle) * radius;
          pointsList.add(new Vector(x, 0, z));
        }
        return pointsList;
      }

    };
  }

  /** Spiral shape going upwards. */
  public static ParticleShape spiral(final double radius, final int turns, final int pointsPerTurn) {
    return new ParticleShape() {
      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        final var total = turns * pointsPerTurn;
        for (var i = 0; i < total; i++) {
          final var angle = 2 * Math.PI * i / pointsPerTurn;
          final var y = (i / (double) total) * radius * 2;
          final var x = Math.cos(angle) * radius;
          final var z = Math.sin(angle) * radius;
          spawn(particle, base.clone().add(x, y, z), ParticleShape.ParticleOptions.defaultOptions());
        }
      }

      @Override
      public void render(final @NotNull Location base, final @Nullable ParticleShape.ParticleOptions options) {
        ParticleShape.ParticleOptions opts = options != null ? options : ParticleShape.ParticleOptions.defaultOptions();
        final var total = turns * pointsPerTurn;
        for (var i = 0; i < total; i++) {
          final var angle = 2 * Math.PI * i / pointsPerTurn;
          final var y = (i / (double) total) * radius * 2;
          final var x = Math.cos(angle) * radius;
          final var z = Math.sin(angle) * radius;
          spawn(Particle.DUST, base.clone().add(x, y, z), opts);
        }
      }

      @Override
      public @NotNull List<Vector> sample() {
        List<Vector> pointsList = new ArrayList<>(turns * pointsPerTurn);
        int total = turns * pointsPerTurn;
        for (int i = 0; i < total; i++) {
          double angle = 2 * Math.PI * i / pointsPerTurn;
          double y = (i / (double) total) * radius * 2;
          double x = Math.cos(angle) * radius;
          double z = Math.sin(angle) * radius;
          pointsList.add(new Vector(x, y, z));
        }
        return pointsList;
      }

    };
  }

  /** Line shape (from -radius to +radius). */
  public static ParticleShape line(final double length, final int points) {
    return new ParticleShape() {
      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        for (var i = 0; i < points; i++) {
          final var t = (double) i / (points - 1);
          final var offset = (t - 0.5) * length;
          spawn(particle, base.clone().add(0, offset, 0), ParticleShape.ParticleOptions.defaultOptions());
        }
      }

      @Override
      public void render(final @NotNull Location base, final @Nullable ParticleShape.ParticleOptions options) {
        ParticleShape.ParticleOptions opts = options != null ? options : ParticleShape.ParticleOptions.defaultOptions();
        for (var i = 0; i < points; i++) {
          final var t = (double) i / (points - 1);
          final var offset = (t - 0.5) * length;
          spawn(Particle.DUST, base.clone().add(0, offset, 0), opts);
        }
      }

      @Override
      public @NotNull List<Vector> sample() {
        List<Vector> pointsList = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
          double t = (double) i / (points - 1);
          double offset = (t - 0.5) * length;
          pointsList.add(new Vector(0, offset, 0));
        }
        return pointsList;
      }

    };
  }

  // ###############################################################
  // --------------------- UTILITY METHOD ---------------------------
  // ###############################################################

  private static void spawn(final @NotNull Particle particle, final @NotNull Location loc, final @NotNull ParticleShape.ParticleOptions opts) {
    if (particle == Particle.DUST && opts.color() != null) {
      var dust = new Particle.DustOptions(opts.color(), opts.size());
      loc.getWorld().spawnParticle(
        particle,
        loc,
        opts.count(),
        opts.offset().getX(),
        opts.offset().getY(),
        opts.offset().getZ(),
        0,
        dust
      );
    } else {
      loc.getWorld().spawnParticle(
        particle,
        loc,
        opts.count(),
        opts.offset().getX(),
        opts.offset().getY(),
        opts.offset().getZ(),
        0
      );
    }
  }
}