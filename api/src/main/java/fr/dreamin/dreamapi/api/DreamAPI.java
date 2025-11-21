package fr.dreamin.dreamapi.api;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * <p>
 * Entry point of the DreamAPI library.
 * </p>
 *
 * <p>
 * Provides a singleton-style access to the current {@link IApiProvider}, which
 * exposes all available managers (data, structures, NPC, messaging...).
 * </p>
 *
 * <p>
 * The API must be initialized by the Core module before use
 * (via {@link #setProvider(IApiProvider)}).
 * </p>
 *
 * <pre>
 * Example usage:
 * {@code
 * DreamAPI.getAPI().getDataManager().registerEntity(MyEntity.class);
 * }
 * </pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public final class DreamAPI {

  private static IApiProvider provider;

  /**
   * Returns the currently active API provider.
   *
   * @return The initialized {@link IApiProvider}.
   * @throws IllegalStateException if DreamAPI has not been initialized yet.
   */
  public static IApiProvider getAPI() {
    if (provider == null)
      throw new IllegalStateException("DreamAPI is not initialized");

    return provider;
  }

  /**
   * Registers the given API provider. This method should only be called once,
   * typically by the Core implementation during plugin startup.
   *
   * @param apiProvider The provider instance to register.
   */
  public static void setProvider(final @NotNull IApiProvider apiProvider) {
    if (provider != null) return;
    provider = apiProvider;
  }


  /**
   * Checks if the DreamAPI provider has already been initialized.
   *
   * @return {@code true} if the API provider is available, {@code false} otherwise.
   */
  public static boolean isInitialized() {
    return provider != null;
  }


  /**
   * Represents the root interface of the DreamAPI provider.
   * Each manager (data, structure, NPC, etc.) should be accessible through this interface.
   */
  public interface IApiProvider {
    /**
     * Returns a registered Bukkit service by its interface class.
     *
     * @param serviceClass The service class
     * @param <T>          The service type
     * @return The registered service instance, or null if not found
     */
    <T> T getService(@NotNull Class<T> serviceClass);

    /**
     * Calls a Bukkit event synchronously.
     *
     * @param event The event instance
     * @param <T>   The event type
     * @return The same event instance, after being fired
     */
    <T extends Event> T callEvent(@NotNull T event);;

    @NotNull Plugin plugin();

    @NotNull Logger getLogger();

  }

}
