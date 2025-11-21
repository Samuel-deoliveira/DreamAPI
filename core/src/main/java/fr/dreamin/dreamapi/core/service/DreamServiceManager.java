package fr.dreamin.dreamapi.core.service;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatically loads and registers all Bukkit services annotated with {@link DreamAutoService}.
 * <p>
 * This loader supports:
 * <ul>
 *     <li>Topological loading of services according to their dependencies.</li>
 *     <li>Lifecycle hooks via {@link DreamService}.</li>
 *     <li>Service priority ordering.</li>
 *     <li>Proper status tracking (LOADING, LOADED, FAILED, etc.).</li>
 * </ul>
 */
public final class DreamServiceManager implements Listener {

  private final @NotNull Plugin plugin;

  // Storage for loaded services
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
    final long startTime = System.currentTimeMillis();

    final var basePackage = this.plugin.getClass().getPackageName();
    log.info("[DreaminService] Starting service scan in: " + basePackage);

    // Step 1: Retrieve all annotated classes
    Set<Class<?>> annotatedClasses;
    try {
      annotatedClasses = ClassScanner.getClasses(this.plugin, basePackage, true).stream()
      .filter(clazz -> clazz.isAnnotationPresent(DreamAutoService.class))
      .collect(Collectors.toSet());
    } catch (IOException | ClassNotFoundException e) {
      log.severe("[DreaminService] Failed to scan classes: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    // Step 2: Build dependency graph
    Map<Class<?>, List<Class<?>>> dependencyGraph = new HashMap<>();
    for (Class<?> clazz : annotatedClasses) {
      DreamAutoService annotation = clazz.getAnnotation(DreamAutoService.class);
      dependencyGraph.put(clazz, Arrays.asList(annotation.dependencies()));
    }

    // Step 3: Compute load order via topological sort
    List<Class<?>> loadOrder;
    try {
      loadOrder = topologicalSort(dependencyGraph);
    } catch (IllegalStateException e) {
      log.severe("[DreaminService] Circular dependency detected: " + e.getMessage());
      return;
    }

    int successCount = 0;
    int failCount = 0;

    // Step 4: Instantiate and register services in the correct order
    for (Class<?> clazz : loadOrder) {
      DreamAutoService annotation = clazz.getAnnotation(DreamAutoService.class);
      Class<?> serviceInterface = annotation.value();
      var priority = annotation.priority();

      try {
        @SuppressWarnings("unchecked")
        final var iface = (Class<Object>) serviceInterface;

        final var constructor = Arrays.stream(clazz.getDeclaredConstructors())
          .filter(c -> c.getParameterCount() == 1 && Plugin.class.isAssignableFrom(c.getParameterTypes()[0]))
          .findFirst()
          .orElse(clazz.getDeclaredConstructors()[0]);

        final var serviceImpl =
          constructor.getParameterCount() == 1
            ? constructor.newInstance(this.plugin)
            : constructor.newInstance();

        if (!(serviceImpl instanceof DreamService ds)) {
          log.warning(String.format("[DreamService] %s does not implement DreamService", clazz.getSimpleName()));
          continue;
        }

        // Mark as loading
        setStatus(ds, DreamService.ServiceStatus.LOADING);
        ds.onLoad(this.plugin);
        setStatus(ds, DreamService.ServiceStatus.LOADED);

        // Register Bukkit service
        Bukkit.getServicesManager().register(iface, serviceImpl, this.plugin, priority);

        // Store DreamService in local registry
        loadedServices.put(clazz, ds);

        successCount++;
//        log.info(String.format("✅ Registered service: %s → %s (priority: %s)", clazz.getSimpleName(), serviceInterface.getSimpleName(), priority));
      } catch (Exception e) {
        failCount++;
        log.severe(String.format("❌ Failed to register service %s: %s", clazz.getSimpleName(), e.getMessage()));
        e.printStackTrace();
      }
    }

    final var duration = System.currentTimeMillis() - startTime;
    log.info(String.format("[DreaminService] Scan completed: %d services loaded, %d failed (%.2fs)", successCount, failCount, duration / 1000.0));
  }

  /**
   * Reloads all currently registered services implementing {@link DreamService}.
   */
  public void reloadAllServices() {
    for (Class<?> clazz : Bukkit.getServicesManager().getKnownServices()) {
      for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(clazz)) {
        final var service = provider.getProvider();
        if (service instanceof DreamService ds) {
          try {
            setStatus(ds, DreamService.ServiceStatus.RELOADING);
            ds.onReload();
            setStatus(ds, DreamService.ServiceStatus.LOADED);
          } catch (Exception e) {
            setStatus(ds, DreamService.ServiceStatus.FAILED);
            ds.onFailed();
            this.plugin.getLogger().severe(String.format("[DreaminService] Failed to reload %s: %s", service.getClass().getSimpleName(), e.getMessage()));
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Closes all currently registered services implementing {@link DreamService}.
   */
  public void closeAllServices() {
    for (Class<?> clazz : Bukkit.getServicesManager().getKnownServices()) {
      for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(clazz)) {
        final var service = provider.getProvider();
        if (service instanceof DreamService ds) {
          try {
            setStatus(ds, DreamService.ServiceStatus.CLOSED);
            ds.onClose();
          } catch (Exception e) {
            this.plugin.getLogger().severe(String.format("[DreaminService] Failed to close %s: %s", service.getClass().getSimpleName(), e.getMessage()));
            e.printStackTrace();
          }
        }
      }
    }
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
