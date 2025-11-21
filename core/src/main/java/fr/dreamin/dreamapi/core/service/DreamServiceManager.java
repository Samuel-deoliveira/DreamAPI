package fr.dreamin.dreamapi.core.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.services.Inject;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.logger.DreamLoggerImpl;
import logger.DebugService;
import logger.DreamLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatically loads and registers all Bukkit services annotated with {@link DreamAutoService}.
 * <p>
 * This loader supports:
 * <ul>
 *   <li>Topological sorting of service load order (dependencies).</li>
 *   <li>Constructor injection via {@link Inject}.</li>
 *   <li>Fallback constructor logic.</li>
 *   <li>Lifecycle management (onLoad / onReload / onClose).</li>
 * </ul>
 */
public final class DreamServiceManager implements Listener {

  private final @NotNull Plugin plugin;

  /** Stores loaded DreamService instances by implementation class */
  private final Map<Class<?>, DreamService> loadedServices = new HashMap<>();

  public DreamServiceManager(final @NotNull Plugin plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  // ====== LOADER ======

  /**
   * Loads all services annotated with {@link DreamAutoService} in the correct dependency order.
   */
  public void loadAllServices() {
    final var log = this.plugin.getLogger();
    final long start = System.currentTimeMillis();

    final var basePackage = this.plugin.getClass().getPackageName();
    log.info(String.format("[DreamService] Scanning package: %s", basePackage));

    // Step 1: Retrieve all annotated classes
    Set<Class<?>> annotated;
    try {
      annotated = ClassScanner.getClasses(this.plugin, basePackage, true).stream()
      .filter(c -> c.isAnnotationPresent(DreamAutoService.class))
      .collect(Collectors.toSet());
    } catch (IOException | ClassNotFoundException e) {
      log.severe(String.format("[DreamService] Failed to scan classes: %s", e.getMessage()));
      return;
    }

    // Step 2: Build dependency graph
    Map<Class<?>, List<Class<?>>> graph = new HashMap<>();
    for (Class<?> clazz : annotated) {
      DreamAutoService annotation = clazz.getAnnotation(DreamAutoService.class);
      graph.put(clazz, Arrays.asList(annotation.dependencies()));
    }

    // Step 3: Compute load order via topological sort
    List<Class<?>> loadOrder;
    try {
      loadOrder = topologicalSort(graph);
    } catch (IllegalStateException e) {
      log.severe(String.format("[DreamService] Circular dependency detected: %s", e.getMessage()));
      return;
    }

    int ok = 0;
    int fail = 0;

    // Step 4: Instantiate and register services in the correct order
    for(Class<?> implClass : loadOrder) {
      try {
        final var auto = implClass.getAnnotation(DreamAutoService.class);
        final var iface = (Class<Object>) auto.value();
        final var priority = auto.priority();

        final var ctor = resolveConstructor(implClass);
        final var args = resolveConstructorArgs(ctor);

        final var instance = ctor.newInstance(args);

        if (!(instance instanceof DreamService ds)) {
          log.warning(String.format("[DreamService] %s does not implement DreamService", implClass.getSimpleName()));
          continue;
        }

        setStatus(ds, DreamService.ServiceStatus.LOADING);
        ds.onLoad(this.plugin);
        setStatus(ds, DreamService.ServiceStatus.LOADED);

        Bukkit.getServicesManager().register(iface, ds, this.plugin, priority);

        this.loadedServices.put(implClass, ds);
        ok++;
      } catch (Exception e) {
        log.severe(String.format("Failed to load service %s: %s", implClass.getSimpleName(), e.getMessage()));
        fail++;
      }
    }

    final var end = System.currentTimeMillis();
    log.info(String.format(
      "[DreamService] Loaded %d services (%d failed) in %.2fs",
      ok, fail, (end - start) / 1000.0
    ));
  }

  /**
   * Reloads all currently registered services implementing {@link DreamService}.
   */
  public void reloadAllServices() {
    this.loadedServices.values().forEach(this::reloadService);
  }

  /**
   * Closes all currently registered services implementing {@link DreamService}.
   */
  public void closeAllServices() {
    this.loadedServices.values().forEach(this::closeService);
    this.loadedServices.clear();
  }

  /**
   * Loads a single service from its implementation class.
   * <p>
   * This method:
   * <ul>
   *     <li>Checks for the {@link DreamAutoService} annotation.</li>
   *     <li>Respects lifecycle hooks (onLoad) and updates {@link DreamService.ServiceStatus}.</li>
   *     <li>Registers the service in Bukkit's {@link org.bukkit.plugin.ServicesManager}.</li>
   * </ul>
   *
   * @param serviceClass The implementation class of the service
   */
  public void loadServiceFromClass(final @NotNull Class<?> serviceClass) {
    final var log = this.plugin.getLogger();

    if (!serviceClass.isAnnotationPresent(DreamAutoService.class)) {
      log.warning(String.format("[DreaminService] Class %s is not annotated with @DreaminAutoService", serviceClass.getSimpleName()));
      return;
    }

    final var annotation = serviceClass.getAnnotation(DreamAutoService.class);
    final var serviceInterface = annotation.value();
    final var priority = annotation.priority();

    try {
      @SuppressWarnings("unchecked")
      final var iface = (Class<Object>) serviceInterface;

      // Instantiate service (try Plugin constructor first)
      final var constructor = Arrays.stream(serviceClass.getDeclaredConstructors())
        .filter(c -> c.getParameterCount() == 1 && Plugin.class.isAssignableFrom(c.getParameterTypes()[0]))
        .findFirst()
        .orElse(serviceClass.getDeclaredConstructors()[0]);

      final var serviceImpl =
        constructor.getParameterCount() == 1
          ? constructor.newInstance(this.plugin)
          : constructor.newInstance();

      if (!(serviceImpl instanceof DreamService ds)) return;

      // Set status to LOADING
      setStatus(ds, DreamService.ServiceStatus.LOADING);

      // Call lifecycle hook
      ds.onLoad(this.plugin);

      // Set status to LOADED
      setStatus(ds, DreamService.ServiceStatus.LOADED);

      // Register with Bukkit ServicesManager
      Bukkit.getServicesManager().register(iface, serviceImpl, this.plugin, priority);

      this.loadedServices.put(serviceClass, ds);

    } catch (Exception e) {
      log.severe(String.format("❌ Failed to load service %s: %s",
        serviceClass.getSimpleName(), e.getMessage()));
      e.printStackTrace();
    }
  }

  /** Calls onLoad() on a specific service and updates its status. */
  public void loadService(final @NotNull DreamService service) {
    try {
      setStatus(service, DreamService.ServiceStatus.LOADING);
      service.onLoad(this.plugin);
      setStatus(service, DreamService.ServiceStatus.LOADED);
      plugin.getLogger().info(String.format("✅ Loaded service %s", service.getName()));
    } catch (Exception e) {
      setStatus(service, DreamService.ServiceStatus.FAILED);
      service.onFailed();
      plugin.getLogger().severe(String.format("❌ Failed to load service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
    this.loadedServices.put(service.getClass(), service);
  }

  /** Calls onReload() on a specific service if it can be reloaded. */
  public void reloadService(final @NotNull DreamService service) {
    if (!service.canReload()) {
      this.plugin.getLogger().warning(String.format("[%s] Service cannot be reloaded in current status: %s", service.getName(), service.getStatus()));
      return;
    }
    try {
      setStatus(service, DreamService.ServiceStatus.RELOADING);
      service.onReload();
      setStatus(service, DreamService.ServiceStatus.LOADED);
      this.plugin.getLogger().info(String.format("🔄 Reloaded service %s", service.getName()));
    } catch (Exception e) {
      setStatus(service, DreamService.ServiceStatus.FAILED);
      service.onFailed();
      this.plugin.getLogger().severe(String.format("❌ Failed to reload service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
  }

  /** Calls onReset() on a specific service. */
  public void resetService(final @NotNull DreamService service) {
    try {
      service.onReset();
    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("❌ Failed to reset service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
  }

  /** Calls onClose() on a specific service and updates its status. */
  public void closeService(final @NotNull DreamService service) {
    try {
      setStatus(service, DreamService.ServiceStatus.CLOSED);
      service.onClose();
      this.plugin.getLogger().info(String.format("❌ Closed service %s", service.getName()));
    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("❌ Failed to close service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
    this.loadedServices.remove(service.getClass());
  }

  /**
   * Returns a DreamService instance by its implementation class.
   *
   * @param clazz The class implementing DreamService
   * @param <T>   The concrete service type
   * @return The loaded service instance, or null if not found
   */
  public <T extends DreamService> T getDreamService(@NotNull Class<T> clazz) {
    return clazz.cast(this.loadedServices.get(clazz));
  }

  /**
   * Returns an unmodifiable view of all loaded services.
   */
  public Map<Class<?>, DreamService> getAllLoadedServices() {
    return Collections.unmodifiableMap(this.loadedServices);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Constructor<?> resolveConstructor(final @NotNull Class<?> clazz) {
    final var constructors = clazz.getDeclaredConstructors();

    // 1) If a constructor has @Inject → use it
    for (var c : constructors) {
      if (c.isAnnotationPresent(Inject.class)) {
        c.setAccessible(true);
        return c;
      }
    }

    // 2) If the class has @Inject → use the best constructor
    if (clazz.isAnnotationPresent(Inject.class)) {

      // Choose the constructor with the most parameters
      // (ideal for Lombok's @RequiredArgsConstructor)
      var best = constructors[0];
      for (var c : constructors) {
        if (c.getParameterCount() > best.getParameterCount())
          best = c;
      }

      best.setAccessible(true);
      return best;
    }

    // 3) Fallback: constructor with only allowed params (Plugin, DreamService, DreamLogger)
    for (final var c : constructors) {
      var compatible = true;

      for (final var param : c.getParameterTypes()) {
        if (!Plugin.class.isAssignableFrom(param) &&
          !param.equals(DreamLogger.class) &&
          !DreamService.class.isAssignableFrom(param)) {
          compatible = false;
          break;
        }
      }

      if (compatible) {
        c.setAccessible(true);
        return c;
      }
    }

    // 4) Fallback: no-arg constructor
    try {
      Constructor<?> c = clazz.getDeclaredConstructor();
      c.setAccessible(true);
      return c;
    } catch (Exception e) {
      throw new RuntimeException("No suitable constructor found for " + clazz.getName());
    }
  }

  private Object[] resolveConstructorArgs(final @NotNull Constructor<?> constructor) {
    final var params = constructor.getParameterTypes();
    final var args = new Object[params.length];

    for (var i = 0; i < params.length; i++) {
      final var param = params[i];

      // Inject Plugin
      if (Plugin.class.isAssignableFrom(param)) {
        args[i] = this.plugin;
        continue;
      }

      if (param == DreamLogger.class) {
        args[i] = createLogger(constructor.getDeclaringClass());
        continue;
      }

      // Inject DreamService
      var resolved = false;
      for (DreamService service : loadedServices.values()) {
        if (param.isAssignableFrom(service.getClass())) {
          args[i] = service;
          resolved = true;
          break;
        }
      }

      if (!resolved) {
        throw new RuntimeException(
          String.format("Unable to resolve dependency: %s for constructor %s", param.getName(), constructor)
        );
      }
    }

    return args;
  }

  private DreamLogger createLogger(final @NotNull Class<?> serviceClass) {
    final var debugService = DreamAPI.getAPI().getService(DebugService.class);
    final var category = serviceClass.getSimpleName().replace("Service", "");
    return new DreamLoggerImpl(this.plugin, category, debugService);
  }

  /**
   * Topological sort to determine proper service load order based on dependencies.
   *
   * @param graph Dependency graph of services
   * @return List of classes sorted in load order
   */
  private List<Class<?>> topologicalSort(Map<Class<?>, List<Class<?>>> graph) {
    List<Class<?>> order = new ArrayList<>();
    Set<Class<?>> visited = new HashSet<>();
    Set<Class<?>> visiting = new HashSet<>();

    for (Class<?> node : graph.keySet()) {
      visit(node, graph, visited, visiting, order);
    }
    return order;
  }

  private void visit(Class<?> node, Map<Class<?>, List<Class<?>>> graph, Set<Class<?>> visited, Set<Class<?>> visiting, List<Class<?>> order) {
    if (visited.contains(node)) return;
    if (visiting.contains(node)) throw new IllegalStateException(node.getSimpleName());

    visiting.add(node);
    for (Class<?> dep : graph.getOrDefault(node, Collections.emptyList())) {
      visit(dep, graph, visited, visiting, order);
    }
    visiting.remove(node);
    visited.add(node);
    order.add(node);
  }

  /**
   * Utility method to set the status of a DreaminService via reflection.
   * <p>
   * This is necessary because the interface has a default getStatus() method.
   */
  private void setStatus(DreamService service, DreamService.ServiceStatus status) {
    try {
      final var field = service.getClass().getDeclaredField("status");
      field.setAccessible(true);
      field.set(service, status);
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
      // The service may choose not to store the status internally
    }
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin() == this.plugin) closeAllServices();
  }

}
