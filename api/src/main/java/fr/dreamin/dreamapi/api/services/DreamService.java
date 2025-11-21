package fr.dreamin.dreamapi.api.services;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Base interface for all Dreamin services.
 * Provides a consistent lifecycle for initialization, reload, and cleanup.
 */
public interface DreamService {

  /** Enum representing the lifecycle state of the service. */
  enum ServiceStatus {
    UNLOADED,    // Not yet loaded
    LOADING,     // Currently loading
    LOADED,      // Successfully loaded
    RELOADING,   // Currently reloading
    CLOSED,      // Service closed
    FAILED       // Failed to load or reload
  }

  /** Returns the human-readable name of the service. */
  default @NotNull String getName() {
    return getClass().getSimpleName();
  }

  /** Current status of the service */
  default ServiceStatus getStatus() {
    return ServiceStatus.UNLOADED;
  }

  /** Called once after the service is created and registered.*/
  default void onLoad(final @NotNull Plugin plugin) {}

  /** Called when the service is reloaded (configs, caches, etc.). */
  default void onReload() {}

  /** Optional: called to reset the service state without fully reloading. */
  default void onReset() {}

  /** Called when the plugin disables or the service is unregistered. */
  default void onClose() {}

  /** Called when the load/reload service failed */
  default void onFailed() {}

  /** Returns true if the service is fully ready to be used. */
  default boolean isReady() {
    return getStatus() == ServiceStatus.LOADED;
  }

  /** Returns true if the service can safely be reloaded. */
  default boolean canReload() {
    return isReady() || getStatus() == ServiceStatus.FAILED;
  }

}
