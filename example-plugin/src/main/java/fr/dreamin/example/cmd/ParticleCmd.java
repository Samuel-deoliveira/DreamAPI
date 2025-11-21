package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.particle.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class ParticleCmd {
  
  @CommandDescription("Particle")
  @CommandMethod("particle show hearth")
  @CommandPermission("test")
  private void test(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    Location startLoc = player.getLocation().add(0, 1, 0);
    Location endLoc = startLoc.clone().add(8, 1, 0);

    var path = ParticlePath.between(startLoc, endLoc)
      .ease(InterpolationType.EASE_IN_OUT);

    var options = ParticleShape.ParticleOptions.ofColor(Color.RED)
      .count(1);

    var shape = ParticleForms.tornado(4.5, 4, 7,60);
    var cicrlce = ParticleShapes.circle(4, 60);

    ParticleShapeMorph.create()
      .particle(Particle.DUST)
      .options(options)
      .path(path)
      .fromShape(ParticleForms.heart(4.0, 60))
      .toShape(shape)
      .duration(20*6)
      .loop(true)
      .reverse(true)
      .progressiveDraw(false)
      .interpolation(InterpolationType.EASE_IN_OUT)
      .build()
      .play(player);

  }

  /**
   *
   * ParticleAnimation -> animation unique
   * ParticleShapeAnimation -> animation A -> B
   * ParticleShapeMorphModule ->
   *
   *
   * @param sender
   */
  @CommandDescription("Particle")
  @CommandMethod("particle show line")
  @CommandPermission("test")
  private void test2(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    Location startLoc = player.getLocation().add(0, 1, 0);
    Location endLoc = player.getLocation().add(0, 1, 0);

    var shape = ParticleForms.tornado(2.5, 1.5, 17,100);

    var path = ParticlePath.between(startLoc, endLoc)
      .ease(InterpolationType.EASE_IN_OUT);

    var options = ParticleShape.ParticleOptions.ofColor(Color.RED)
      .size(0.1f)
      .count(1);

    var animation = ParticleAnimation.create()
      .particle(Particle.DUST)
      .shape(shape)
      .options(options)
      .path(path)
      .duration(20*8)
      .speed(1.0)
      .progressiveDraw(true)
      .interpolation(InterpolationType.EASE_IN_OUT)
      .loop(true)
      .reverse(true)
      .build();

    animation.play(player);
  }

  @CommandDescription("Particle")
  @CommandMethod("particle show sequence")
  @CommandPermission("test")
  private void test3(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    // 1. Définir les formes
    ParticleShape circle = ParticleShapes.circle(5, 200);
    ParticleShape sphere = ParticleForms.hollowSphere(2, 1400);
    ParticleShape heart = ParticleForms.heart(3.5, 200);

    Location startLoc = player.getLocation().add(0, 1, 0);
    Location endLoc = startLoc.clone().add(0, 1, 0);

    var path = ParticlePath.between(startLoc, endLoc)
      .ease(InterpolationType.EASE_IN_OUT);

    Location startLoc1 = player.getLocation().add(9, 1, 0);
    Location endLoc1 = startLoc.clone().add(15, 1, 0);

    var path2 = ParticlePath.between(startLoc1, endLoc1)
      .ease(InterpolationType.EASE_IN_OUT);

    // 2. Créer l'animation de morphing en chaîne (Circle -> Sphere -> Heart)
    ParticleChainMorph chainMorph = ParticleChainMorph.create()
      .particle(Particle.DUST)
      .options(ParticleShape.ParticleOptions.ofColor(Color.RED).size(0.5f).count(1))
      .addShape(circle, 200)
      .addShape(sphere, 210)
      .addShape(heart, 160)
      .duration(570)
      .path(path)
      .rotationAnglePerTick(new Vector(0.01, 0.01, 0.01))
      .rotationOrigin(new Vector(2, 0, 0))
      .progressiveDraw(false)
      .turn(true)
      .every(2)
      .reverse(true) // Morphing Heart -> Sphere -> Circle au retour
      .loop(true) // Boucle le morphing A->B->C->B->A
      .build();

// 3. Créer une animation simple (par exemple, une ligne)
    ParticleAnimation lineAnimation = ParticleAnimation.create()
      .particle(Particle.DUST)
      .options(ParticleShape.ParticleOptions.ofColor(Color.GREEN).size(1.5f).count(1))
      .shape(ParticleShapes.line(3.0, 30))
      .path(path2)
      .duration(40)
      .build();

// 4. Construire la séquence (le spectacle)
    ParticleSequence spectacle = ParticleSequence.create()
      .add(chainMorph) // Joue le morphing, puis attend 20 ticks
//      .add(lineAnimation) // Joue l'animation de ligne (pas d'attente après)
//      .loop(true) // Répète le spectacle complet
      .build();

// 5. Lancer le spectacle
    spectacle.play(player);

  }
}
