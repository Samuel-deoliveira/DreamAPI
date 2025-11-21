package fr.dreamin.dreamapi.api.http.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.http.ApiManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ApiPool {

  private static final Map<String, ApiManager> APIS = new ConcurrentHashMap<>();

  // ###############################################################
  // ------------------------- REGISTER ----------------------------
  // ###############################################################

  /**
   * Registers an existing ApiManager instance.
   *
   * @param label The unique label identifying this API.
   * @param api   The ApiManager instance.
   * @return The same instance for chaining.
   */
  public static @NotNull ApiManager register(@NotNull String label, @NotNull ApiManager api) {
    APIS.put(label.toLowerCase(), api);
    return api;
  }

  /**
   * Registers a new ApiManager via a supplier if not already present.
   * Immediately performs a health check (ping) and logs the result.
   *
   * @param label   The unique label.
   * @param creator The supplier creating the ApiManager.
   * @return The existing or newly created ApiManager.
   */
  public static @NotNull ApiManager register(@NotNull String label, @NotNull Supplier<ApiManager> creator) {
    return APIS.computeIfAbsent(label.toLowerCase(), key -> {
      ApiManager api = creator.get();
      boolean ok = api.ping();
      DreamAPI.getAPI().getLogger().info("[" + label + "] API status: " + (ok ? "§aONLINE" : "§cOFFLINE"));
      return api;
    });
  }

  // ###############################################################
  // --------------------------- GET -------------------------------
  // ###############################################################

  /**
   * Retrieves an ApiManager by its label or throws if not found.
   */
  public static @NotNull ApiManager get(@NotNull String label) {
    ApiManager api = APIS.get(label.toLowerCase());
    if (api == null)
      throw new IllegalStateException("No API registered with label: " + label);
    return api;
  }

  /**
   * Retrieves an ApiManager if present.
   */
  public static Optional<ApiManager> find(@NotNull String label) {
    return Optional.ofNullable(APIS.get(label.toLowerCase()));
  }

  /**
   * Returns all registered APIs.
   */
  public static Collection<ApiManager> all() {
    return Collections.unmodifiableCollection(APIS.values());
  }

  /**
   * Checks if an API with the given label exists.
   */
  public static boolean exists(@NotNull String label) {
    return APIS.containsKey(label.toLowerCase());
  }

  // ###############################################################
  // ------------------------ UNREGISTER ---------------------------
  // ###############################################################

  /**
   * Unregisters and closes an API.
   */
  public static void unregister(@NotNull String label) {
    Optional.ofNullable(APIS.remove(label.toLowerCase()))
      .ifPresent(api -> {
        try {
          api.close();
          DreamAPI.getAPI().getLogger().info("[" + label + "] API connection closed.");
        } catch (Exception e) {
          DreamAPI.getAPI().getLogger().warning("Failed to close API: " + label + " - " + e.getMessage());
        }
      });
  }

  /**
   * Closes and clears all registered APIs.
   */
  public static void closeAll() {
    APIS.forEach((label, api) -> {
      try {
        api.close();
      } catch (Exception e) {
        DreamAPI.getAPI().getLogger().warning("Failed to close API: " + label + " - " + e.getMessage());
      }
    });
    APIS.clear();
    DreamAPI.getAPI().getLogger().info("§c[API] All connections closed.");
  }
}