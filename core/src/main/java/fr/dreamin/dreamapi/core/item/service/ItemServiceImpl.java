package fr.dreamin.dreamapi.core.item.service;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DreamAutoService(value= ItemService.class)
public class ItemServiceImpl implements ItemService, DreamService {

  private final @NotNull Plugin plugin;

  public ItemServiceImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public <T, Z> boolean hasTag(@NotNull ItemStack item, @NotNull String key, @NotNull PersistentDataType<T, Z> type) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    final var namespacedKey = new NamespacedKey(plugin, key);
    return meta.getPersistentDataContainer().has(namespacedKey, type);
  }

  @Override
  public @Nullable String getStringTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return null;
    final var meta = item.getItemMeta();
    if (meta == null) return null;
    final var namespacedKey = new NamespacedKey(plugin, key);
    return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
  }

  @Override
  public boolean getBooleanTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    final var namespacedKey = new NamespacedKey(plugin, key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN);
    return result != null && result;
  }

  @Override
  public int getIntTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return -1;
    final var meta = item.getItemMeta();
    if (meta == null) return -1;
    var namespacedKey = new NamespacedKey(plugin, key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
    return result != null ? result : -1;
  }

  @Override
  public double getDoubleTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return -1;
    final var meta = item.getItemMeta();
    if (meta == null) return -1;
    final var namespacedKey = new NamespacedKey(plugin, key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.DOUBLE);
    return result != null ? result : -1;
  }
}
