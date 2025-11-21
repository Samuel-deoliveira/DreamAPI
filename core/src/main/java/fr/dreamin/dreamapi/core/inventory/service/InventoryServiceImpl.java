package fr.dreamin.dreamapi.core.inventory.service;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
@DreamAutoService(value= InventoryService.class)
public class InventoryServiceImpl implements InventoryService, DreamService {

  private final @NotNull Plugin plugin;

  // ###############################################################
  // ----------------------- BASE ------------------------
  // ###############################################################

  @Override
  public void playerClear(@NotNull Player player) {
    player.setItemOnCursor(null);
    player.getInventory().clear();
  }

  // ###############################################################
  // ---------------------------- COPY -----------------------------
  // ###############################################################

  @Override
  public void copyTo(@NotNull Inventory origin, @NotNull Inventory goal, @Nullable Location outLocation) {
    for (var item : origin.getContents()) {
      if (item == null) continue;
      if (goal.firstEmpty() == -1) {
        if (outLocation != null)
          outLocation.getWorld().dropItemNaturally(outLocation, item.clone());
      } else
        goal.addItem(item.clone());
    }
  }

  @Override
  public void copyTo(@NotNull Inventory origin, @NotNull Inventory goal) {
    copyTo(origin, goal, null);
  }

  @Override
  public void dropTo(@NotNull Inventory inventory, @NotNull Location location, boolean clear) {
    for (var item : inventory.getContents()) {
      if (item != null) location.getWorld().dropItemNaturally(location, item.clone());
    }
    if (clear) inventory.clear();
  }

  // ###############################################################
  // ------------------------ MANIPULATION -------------------------
  // ###############################################################

  @Override
  public void remove(@NotNull Inventory inv, @NotNull ItemStack item, int quantity) {
    var remaining = quantity;
    for (var slot : inv.getContents()) {
      if (slot == null) continue;
      if (equals(slot, item, CompareIgnoreAttribute.AMOUNT)) {
        final var remove = Math.min(slot.getAmount(), remaining);
        slot.setAmount(slot.getAmount() - remove);
        remaining -= remove;
        if (slot.getAmount() <= 0) inv.removeItem(slot);
        if (remaining <= 0) return;
      }
    }
  }

  @Override
  public void remove(@NotNull Player player, @NotNull ItemStack item, int quantity) {
    remove(player.getInventory(), item, quantity);
  }

  @Override
  public void replace(@NotNull Inventory inv, @NotNull ItemStack newest, @NotNull ItemStack replaced, boolean addIfNotExists) {
    int slot = inv.first(replaced);
    if (slot == -1) {
      if (addIfNotExists) inv.addItem(newest.clone());
      return;
    }
    inv.setItem(slot, newest.clone());
  }

  @Override
  public void replace(@NotNull Player player, @NotNull ItemStack newest, @NotNull ItemStack replaced, boolean addIfNotExists) {
    if (player.getItemOnCursor() != null && player.getItemOnCursor().equals(replaced)) {
      player.setItemOnCursor(newest.clone());
      return;
    }
    replace(player.getInventory(), newest, replaced, addIfNotExists);
  }

  // ###############################################################
  // ------------------------ NEW METHODS --------------------------
  // ###############################################################

  @Override
  public boolean has(@NotNull Inventory inv, @NotNull ItemStack item, int amount) {
    return count(inv, item) >= amount;
  }

  @Override
  public int findSlot(@NotNull Inventory inv, @NotNull ItemStack item) {
    for (var i = 0; i < inv.getSize(); i++) {
      final var slot = inv.getItem(i);
      if (slot != null && equals(slot, item)) return i;
    }
    return -1;
  }

  @Override
  public int count(@NotNull Inventory inv, @NotNull ItemStack item) {
    var total = 0;
    for (var slot : inv.getContents()) {
      if (slot == null) continue;
      if (equals(slot, item, CompareIgnoreAttribute.AMOUNT))
        total += slot.getAmount();
    }
    return total;
  }

  @Override
  public void merge(@NotNull Inventory from, @NotNull Inventory to, @NotNull Predicate<ItemStack> filter) {
    for (var item : from.getContents()) {
      if (item == null) continue;
      if (!filter.test(item)) continue;

      if (to.firstEmpty() != -1)
        to.addItem(item.clone());
      else
        if (to.getHolder() instanceof Player p)
          p.getWorld().dropItemNaturally(p.getLocation(), item.clone());
    }

  }

  @Override
  public void giveOrDrop(@NotNull Player player, @NotNull ItemStack item) {
    final var inv = player.getInventory();
    if (inv.firstEmpty() != -1) inv.addItem(item.clone());
    else player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
  }

  @Override
  public void purge(@NotNull Inventory inv, @NotNull Predicate<ItemStack> filter) {
    for (var i = 0; i < inv.getSize(); i++) {
      final var item = inv.getItem(i);
      if (item == null) continue;
      if (filter.test(item)) inv.setItem(i, null);
    }
  }

  @Override
  public boolean equals(@NotNull ItemStack is1, @NotNull ItemStack is2, CompareIgnoreAttribute... ignoreAttributes) {
    final var ignoreSet = EnumSet.noneOf(CompareIgnoreAttribute.class);
    if (ignoreAttributes != null) ignoreSet.addAll(Arrays.asList(ignoreAttributes));

    if (!ignoreSet.contains(CompareIgnoreAttribute.TYPE) && is1.getType() != is2.getType()) return false;
    if (!ignoreSet.contains(CompareIgnoreAttribute.AMOUNT) && is1.getAmount() != is2.getAmount()) return false;

    final var meta1 = is1.getItemMeta();
    final var meta2 = is2.getItemMeta();

    if (!ignoreSet.contains(CompareIgnoreAttribute.META)) {
      if (meta1 == null || meta2 == null) return meta1 == meta2;
      if (!meta1.equals(meta2)) return false;
    }

    if (!ignoreSet.contains(CompareIgnoreAttribute.ENCHANTMENT)
      && !is1.getEnchantments().equals(is2.getEnchantments()))
      return false;

    if (!ignoreSet.contains(CompareIgnoreAttribute.LABEL)
      && meta1 != null && meta2 != null
      && !meta1.itemName().equals(meta2.itemName()))
      return false;

    if (!ignoreSet.contains(CompareIgnoreAttribute.DATACONTAINER)) {
      if (meta1 == null || meta2 == null) return meta1 == meta2;
      if (!meta1.getPersistentDataContainer().equals(meta2.getPersistentDataContainer()))
        return false;
    }

    return true;
  }

}
