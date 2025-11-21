package fr.dreamin.dreamapi.core.item;

import fr.dreamin.dreamapi.core.item.builder.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Static facade for quick access to DreamAPI's ItemBuilder system.
 * <p>
 * Provides shorthand methods for creating {@link ItemBuilder} instances.
 * </p>
 *
 * Example usage:
 * <pre>{@code
 * ItemStack sword = Items.of(Material.DIAMOND_SWORD)
 *      .setName(Component.text("§bLegendary Sword"))
 *      .addEnchant(Enchantment.DAMAGE_ALL, 5)
 *      .setUnbreakable(true)
 *      .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
public final class Items {

  // ###############################################################
  // ------------------------- GLOBAL ITEM -------------------------
  // ###############################################################

  public static ItemBuilder of(final @NotNull Material material) {
    return new ItemBuilder(material);
  }

  public static ItemBuilder of(final @NotNull Material material, final int amount) {
    return new ItemBuilder(material, amount);
  }

  public static ItemBuilder from(final @NotNull ItemStack stack) {
    if (isPotion(stack.getType()))
      return new PotionBuilder(stack);
    else if (isSkull(stack.getType()))
      return new SkullBuilder(stack);
    else if (isBanner(stack.getType()))
      return new BannerBuilder(stack);
    else if (isFirework(stack.getType()))
      return new FireworkBuilder(stack);

    return new ItemBuilder(stack);
  }

  // ###############################################################
  // ------------------------- POTION ITEM -------------------------
  // ###############################################################

  public static PotionBuilder ofPotion(final @NotNull PotionType tye) {
    return new PotionBuilder(tye);
  }

  public static PotionBuilder ofPotion(final @NotNull PotionType type, final int amount) {
    return new PotionBuilder(type, amount);
  }

  public static PotionBuilder fromPotion(final @NotNull ItemStack stack) {
    return new PotionBuilder(stack);
  }

  // ###############################################################
  // ------------------------- SKULL ITEM --------------------------
  // ###############################################################

  public static SkullBuilder ofHead(final @NotNull String base64) {
    return new SkullBuilder(base64);
  }

  public static SkullBuilder ofHead(final @NotNull OfflinePlayer player) {
    return new SkullBuilder(player);
  }

  public static SkullBuilder ofHead(final @NotNull UUID uuid) {
    return new SkullBuilder(uuid);
  }

  public static SkullBuilder fromHead(final @NotNull ItemStack stack) {
    return new SkullBuilder(stack);
  }

  // ###############################################################
  // ------------------------- BANNER ITEM -------------------------
  // ###############################################################

  public static BannerBuilder ofBanner(final @NotNull BannerType type) {
    return new BannerBuilder(type);
  }

  public static BannerBuilder ofBanner(final @NotNull BannerType type, final int amount) {
    return new BannerBuilder(type, amount);
  }

  public static BannerBuilder fromBanner(final @NotNull ItemStack stack) {
    return new BannerBuilder(stack);
  }

  // ###############################################################
  // ------------------------ FIREWORK ITEM ------------------------
  // ###############################################################

  public static FireworkBuilder ofFirework() {
    return new FireworkBuilder();
  }

  public static FireworkBuilder ofFirework(final int amount) {
    return new FireworkBuilder(amount);
  }

  public static FireworkBuilder fromFirework(final @NotNull ItemStack stack) {
    return new FireworkBuilder(stack);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static boolean isSkull(final @NotNull Material material) {
    return material.equals(Material.PLAYER_HEAD);
  }

  private static boolean isFirework(final @NotNull Material material) {
    return material.equals(Material.FIREWORK_ROCKET);
  }

  private static boolean isBanner(@NotNull Material material) {
    final var name = material.name();
    return name.endsWith("_BANNER");
  }

  private static boolean isPotion(@NotNull Material material) {
    final var name = material.name();
    return name.endsWith("_POTION");
  }

}
