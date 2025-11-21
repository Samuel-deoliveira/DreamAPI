package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.animation.AnimationService;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.time.Duration;

public class TestCmd {

  @CommandDescription("Test")
  @CommandMethod("test1")
  @CommandPermission("test")
  private void test(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
//
//    final var animationService = DreamAPI.getAPI().getService(AnimationService.class);
//
//    var anim = animationService
//      .cinematic("intro")
//      .camera(
//        new Location(player.getWorld(), 100, 80, 100),
//        new Location(player.getWorld(), 110, 85, 105),
//        Duration.ofSeconds(2),
//        InterpolationType.EASE_IN_OUT
//      )
//      .camera(
//        new Location(player.getWorld(), 110, 85, 105),
//        new Location(player.getWorld(), 120, 88, 150),
//        Duration.ofSeconds(3)
//      )
//      .returnToStart(true)
//      .copyInventory(true)
//      .build();
//
//    anim.play(player);

    player.getWorld().spawn(player.getLocation(), Mannequin.class, mannequin -> {
      mannequin.setPose(Pose.SLEEPING);
    });

  }

}
