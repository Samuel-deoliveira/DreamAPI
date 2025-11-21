package fr.dreamin.example;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.dreamapi.plugin.DreamPlugin;
import fr.dreamin.example.cmd.GUICmd;
import fr.dreamin.example.cmd.ParticleCmd;
import fr.dreamin.example.cmd.TestCmd;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.logging.Level;

@Getter
public final class ExamplePlugin extends DreamPlugin implements Listener {

  @Getter
  private static @NotNull DreamPlugin instance;

  @Override
  public void onDreamEnable() {
    getLogger().info("DreamAPI good");

    instance = this;

    loadCloudCommands();

    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDreamDisable() {
    getLogger().info("DreamAPI dead");
  }

  @EventHandler
  private void onJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void loadCloudCommands() {
    PaperCommandManager<CommandSender> manager;
    AnnotationParser<CommandSender> annotationParser;
    try {
      manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(), Function.identity());

      if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        manager.registerAsynchronousCompletions();

      annotationParser = new AnnotationParser<>(manager, CommandSender.class, p -> SimpleCommandMeta.empty());
    } catch (Exception e) {
      this.getLogger().log(Level.SEVERE, "Unable to register commands", e);
      this.getServer().getPluginManager().disablePlugin(this);
      return;
    }

    annotationParser.parse(new ParticleCmd());
    annotationParser.parse(new TestCmd());
    annotationParser.parse(new GUICmd());
  }

}
