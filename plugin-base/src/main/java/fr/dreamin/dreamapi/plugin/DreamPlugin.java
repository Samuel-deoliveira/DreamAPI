package fr.dreamin.dreamapi.plugin;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.ApiProviderImpl;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.animation.AnimationServiceImpl;
import fr.dreamin.dreamapi.core.cuboid.core.CuboidServiceImpl;
import fr.dreamin.dreamapi.core.glowing.GlowingServiceImpl;
import fr.dreamin.dreamapi.core.inventory.service.InventoryServiceImpl;
import fr.dreamin.dreamapi.core.item.service.ItemServiceImpl;
import fr.dreamin.dreamapi.core.logger.DebugServiceImpl;
import fr.dreamin.dreamapi.core.luckperms.LuckPermsServiceImpl;
import fr.dreamin.dreamapi.core.packUtils.GlobalTexturesServiceImpl;
import fr.dreamin.dreamapi.core.service.DreamServiceManager;
import fr.dreamin.dreamapi.core.team.TeamServiceImpl;
import fr.dreamin.dreamapi.core.time.day.impl.DayCycleServiceImpl;
import fr.dreamin.dreamapi.core.world.impl.WorldServiceImpl;
import fr.dreamin.dreamapi.plugin.cmd.admin.broadcast.AdminBroadcastCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.broadcast.BroadcastContext;
import fr.dreamin.dreamapi.plugin.cmd.admin.debug.DebugCmd;
import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * <p>
 * Abstract base class for all Paper plugins built on top of DreamAPI.
 * </p>
 *
 * <p>
 * Automatically initializes the {@link DreamAPI} core if not already initialized
 * and provides lifecycle hooks for plugin developers:
 * {@link #onDreamEnable()} and {@link #onDreamDisable()}.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 *
 * <pre>
 * public final class MyPlugin extends DreamPlugin {
 *     &#64;Override
 *     public void onDreamEnable() {
 *         getLogger().info("DreamAPI is ready!");
 *     }
 *
 *     &#64;Override
 *     public void onDreamDisable() {
 *         getLogger().info("Plugin stopped.");
 *     }
 * }
 * </pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */

public abstract class DreamPlugin extends JavaPlugin {

  private PaperCommandManager<CommandSender> manager;
  private AnnotationParser<CommandSender> annotationParser;;

  /**
   * Reference to the active {@link DreamAPI.IApiProvider} instance.
   */
  @Getter
  protected DreamAPI.IApiProvider dreamAPI;

  @Getter
  protected @NotNull DreamServiceManager serviceManager;

  @Getter
  protected @NotNull BroadcastContext broadcastContext;

  @Getter @Setter
  protected boolean broadcastCmd = false;

  // ##############################################################
  // -------------------- JAVAPLUGIN METHODS ----------------------
  // ##############################################################

  /**
   * Called by Bukkit when the plugin is enabled.
   * <p>
   * This method ensures DreamAPI is initialized before invoking
   * the developer-defined {@link #onDreamEnable()} method.
   * </p>
   */
  @Override
  public void onEnable() {
    if (!DreamAPI.isInitialized()) {
      getLogger().info("DreamAPI provider not found. Initializing core implementation...");
      new ApiProviderImpl(this);
    }

    this.broadcastContext = BroadcastContext.builder().build();

    DreamContext.setPlugin(this);
    this.dreamAPI = DreamAPI.getAPI();

    this.serviceManager = new DreamServiceManager(this);
    loadServices();
    loadCommands();


    getLogger().info(String.format("DreamAPI initialized successfully with provider: %s",
      this.dreamAPI.getClass().getSimpleName()));

    onDreamEnable();
  }

  /**
   * Called by Bukkit when the plugin is disabled.
   * <p>
   * Invokes the developer-defined {@link #onDreamDisable()} method.
   * </p>
   */
  @Override
  public void onDisable() {

    onDreamDisable();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  /**
   * Called once DreamAPI is fully initialized and ready.
   * <p>
   * Equivalent to {@code onEnable()} for standard Bukkit plugins.
   * </p>
   */
  public abstract void onDreamEnable();

  /**
   * Called before plugin shutdown.
   * <p>
   * Use this method to release resources, save data, etc.
   * </p>
   */
  public abstract void onDreamDisable();

  // ###############################################################
  // ---------------------- INTERNAL METHODS -----------------------
  // ###############################################################

  /**
   * Get a service from the ServicesManager
   *
   * @param serviceClass the class of the service to get
   * @param <T>          the type of the service
   * @return the service instance
   * @throws IllegalStateException if the service is not loaded
   */
  public static <T> T getService(Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    T service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  /**
   * Call an event
   * @param event the event to call
   */
  public static void callEvent(final @NotNull Event event) {
    Bukkit.getPluginManager().callEvent(event);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull <T extends DreamService> Optional<T> findDreamService(@NotNull Class<T> clazz) {
    return Optional.ofNullable(this.serviceManager.getDreamService(clazz));
  }

  /**
   * Returns an internal DreamService instance (implementation class).
   *
   * @param clazz The class implementing DreamService
   * @param <T>   The concrete type extending DreamService
   * @return The loaded DreamService, or null if not found
   */
  public <T extends DreamService> @NotNull T getDreamService(@NotNull Class<T> clazz) {
    T service = this.serviceManager.getDreamService(clazz);
    if (service == null) {
      throw new IllegalStateException(String.format(
        "DreamService '%s' is not loaded or not registered.",
        clazz.getSimpleName()
      ));
    }
    return service;
  }

  public void registerCommand(Object commandHandler) {
    annotationParser.parse(commandHandler);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void loadServices() {
    this.serviceManager.loadAllServices();

    this.serviceManager.loadServiceFromClass(DebugServiceImpl.class);
    this.serviceManager.loadServiceFromClass(AnimationServiceImpl.class);
    this.serviceManager.loadServiceFromClass(WorldServiceImpl.class);
    this.serviceManager.loadServiceFromClass(DayCycleServiceImpl.class);
    this.serviceManager.loadServiceFromClass(GlowingServiceImpl.class);
    this.serviceManager.loadServiceFromClass(GlobalTexturesServiceImpl.class);
    this.serviceManager.loadServiceFromClass(InventoryServiceImpl.class);
    this.serviceManager.loadServiceFromClass(CuboidServiceImpl.class);
    this.serviceManager.loadServiceFromClass(TeamServiceImpl.class);
    this.serviceManager.loadServiceFromClass(ItemServiceImpl.class);

    if (isLuckPermsAvailable())
      this.serviceManager.loadServiceFromClass(LuckPermsServiceImpl.class);

  }

  private boolean isLuckPermsAvailable() {
    if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null)
      return false;

    var service = Bukkit.getServicesManager().load(LuckPerms.class);
    return service != null;
  }

  private void loadCommands() {
    try {
      this.manager = new PaperCommandManager<>(
        this,
        CommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(),
        Function.identity()
      );

      if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        manager.registerAsynchronousCompletions();

      this.annotationParser = new AnnotationParser<>(manager, CommandSender.class, p -> SimpleCommandMeta.empty());
    } catch (Exception e) {
      this.getLogger().log(Level.SEVERE, "Unable to register commands", e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    if (this.broadcastCmd)
      this.annotationParser.parse(new AdminBroadcastCmd(this));

    this.annotationParser.parse(new DebugCmd());

  }

}
