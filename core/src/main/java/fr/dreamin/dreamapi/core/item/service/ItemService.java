package fr.dreamin.dreamapi.core.item.service;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemService {

  <T, Z> boolean hasTag(@NotNull ItemStack item, @NotNull String key, @NotNull PersistentDataType<T, Z> type);

  @Nullable String getStringTag(@NotNull ItemStack item, @NotNull String key);

  boolean getBooleanTag(@NotNull ItemStack item, @NotNull String key);

  int getIntTag(@NotNull ItemStack item, @NotNull String key);

  double getDoubleTag(@NotNull ItemStack item, @NotNull String key);

}
