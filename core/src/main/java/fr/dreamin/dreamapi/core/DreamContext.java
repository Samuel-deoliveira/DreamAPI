package fr.dreamin.dreamapi.core;

import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the current plugin context for DreamAPI runtime.
 * <p>
 * This allows non-plugin classes (like ItemBuilder) to access
 * the active {@link JavaPlugin} instance for features such as NamespacedKeys.
 * </p>
 */
public class DreamContext {

  @NotNull
  public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

  private static JavaPlugin plugin;

  /**
   * Sets the current plugin context (automatically done by DreamPlugin).
   *
   * @param pluginInstance The main plugin instance.
   */
  public static void setPlugin(@NotNull JavaPlugin pluginInstance) {
    plugin = pluginInstance;
  }

  /**
   * @return The current plugin instance.
   * @throws IllegalStateException if not yet initialized.
   */
  public static @NotNull JavaPlugin getPlugin() {
    if (plugin == null)
      throw new IllegalStateException("DreamContext plugin instance not set.");
    return plugin;
  }

}
