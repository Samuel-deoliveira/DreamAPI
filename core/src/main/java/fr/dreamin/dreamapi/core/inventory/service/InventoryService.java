package fr.dreamin.dreamapi.core.inventory.service;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface InventoryService {

  // --- Gestion de base ---
//  void clear(@NotNull Inventory inv);
  void playerClear(@NotNull Player player);

  // --- Transfert & copie ---
  void copyTo(@NotNull Inventory origin, @NotNull Inventory goal, @Nullable Location outLocation);
  void copyTo(@NotNull Inventory origin, @NotNull Inventory goal);
  void dropTo(@NotNull Inventory inventory, @NotNull Location location, boolean clear);

  // --- Manipulation ---
  void remove(@NotNull Inventory inv, @NotNull ItemStack item, int quantity);
  void remove(@NotNull Player player, @NotNull ItemStack item, int quantity);
  void replace(@NotNull Inventory inv, @NotNull ItemStack newest, @NotNull ItemStack replaced, boolean addIfNotExists);
  void replace(@NotNull Player player, @NotNull ItemStack newest, @NotNull ItemStack replaced, boolean addIfNotExists);

  // --- Nouvelles fonctions DX ---
  boolean has(@NotNull Inventory inv, @NotNull ItemStack item, int amount);
  int findSlot(@NotNull Inventory inv, @NotNull ItemStack item);
  int count(@NotNull Inventory inv, @NotNull ItemStack item);
  void merge(@NotNull Inventory from, @NotNull Inventory to, @NotNull Predicate<ItemStack> filter);
  void giveOrDrop(@NotNull Player player, @NotNull ItemStack item);
  void purge(@NotNull Inventory inv, @NotNull Predicate<ItemStack> filter);

  // --- Comparaison avancée ---
  boolean equals(@NotNull ItemStack is1, @NotNull ItemStack is2, CompareIgnoreAttribute... ignoreAttributes);

  enum CompareIgnoreAttribute { META, AMOUNT, TYPE, ENCHANTMENT, LABEL, DATACONTAINER }

}
