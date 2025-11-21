package fr.dreamin.dreamapi.core.item.builder;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.dreamin.dreamapi.core.DreamContext;
import fr.dreamin.dreamapi.core.annotations.Internal;
import fr.dreamin.dreamapi.core.item.Items;
import io.papermc.paper.datacomponent.DataComponentType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Internal implementation of the Paper ItemBuilder system.
 * <p>
 * Use {@link Items} for all public API interactions.
 * </p>
 */
@Getter
@Internal
public class ItemBuilder {

  private static final Cache<String, PlayerProfile> PROFILE_CACHE = CacheBuilder.newBuilder()
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build();

  private ItemStack is;
  private ItemMeta itemMeta;

  // ###############################################################
  // ------------------------ CONSTRUCTORS -------------------------
  // ###############################################################

  /**
   * Constructs an ItemBuilder with a specified Material and a default amount of 1.
   *
   * @param m The material for the item.
   */
  public ItemBuilder(Material m) {this(m, 1);}

  /**
   * Constructs an ItemBuilder with a specified Material and amount.
   *
   * @param m      The material for the item.
   * @param amount The amount of the item in the stack.
   */
  public ItemBuilder(Material m, int amount) {
    this.is = new ItemStack(m, amount);

    this.itemMeta = is.getItemMeta();
  }

  /**
   * Constructs an ItemBuilder from an existing ItemStack. The item meta is cloned to ensure it doesn't modify the
   * original ItemStack.
   *
   * @param is The existing ItemStack to copy.
   */
  public ItemBuilder(ItemStack is) {
    this.is = is.clone();
    final var meta = is.getItemMeta();
    if (meta != null && is.hasItemMeta()) {
      this.is.setItemMeta(meta.clone());
      this.itemMeta = this.is.getItemMeta();
    }
  }

  // ###############################################################
  // -------------------------- UTILITIES --------------------------
  // ###############################################################

  private void withMeta(Consumer<ItemMeta> consumer) {
    final var meta = this.itemMeta;
    consumer.accept(meta);
    this.itemMeta = meta;
  }

  private <T extends ItemMeta> void withMeta(Class<T> metaClass, Consumer<T> consumer) {
    is.editMeta(metaClass, consumer);
  }

  // ###############################################################
  // ------------------------- BASIC PROPS -------------------------
  // ###############################################################

  public ItemBuilder setType(Material type) {
    this.is = is.withType(type);
    return this;
  }

  public ItemBuilder setItemModel(NamespacedKey key) {
    withMeta(meta -> meta.setItemModel(key));
    return this;
  }

  public ItemBuilder setRarity(ItemRarity rarity) {
    withMeta(meta -> meta.setRarity(rarity));
    return this;
  }

  public ItemBuilder setHideToolType(boolean b) {
    withMeta(meta -> meta.setHideTooltip(b));
    return this;
  }


  public ItemBuilder addItemFlag(ItemFlag... flags) {
    withMeta(meta -> meta.addItemFlags(flags));
    return this;
  }

  public ItemBuilder setUnbreakable(boolean unbreakable) {
    withMeta(meta -> meta.setUnbreakable(unbreakable));
    return this;
  }

  // ###############################################################
  // ----------------------- AMOUNT / STACK ------------------------
  // ###############################################################

  public ItemBuilder setAmount(int amount) {
    this.is.setAmount(amount);
    return this;
  }

  public ItemBuilder setMaxStackSize(int amount) {
    withMeta(meta -> meta.setMaxStackSize(amount));
    return this;
  }

  // ###############################################################
  // ---------------------------- LORE -----------------------------
  // ###############################################################

  public ItemBuilder setLore(List<Component> lines) {
    withMeta(meta -> meta.lore(lines));
    return this;
  }

  public ItemBuilder setLore(Component... lines) {
    return setLore(Arrays.asList(lines));
  }

  public ItemBuilder setLegacyLore(String... lines) {
    withMeta(meta -> meta.setLore(Arrays.asList(lines)));
    return this;
  }

  // ###############################################################
  // ---------------------------- NAMES ----------------------------
  // ###############################################################

  public ItemBuilder setName(final @NotNull Component name) {
    withMeta(meta -> meta.displayName(name));
    return this;
  }

  public ItemBuilder setCustomName(final @NotNull Component name) {
    withMeta(meta -> meta.customName(name));
    return this;
  }

  public ItemBuilder setLegacyName(final @NotNull String name) {
    withMeta(meta -> meta.setDisplayName(name));
    return this;
  }

  // ###############################################################
  // ------------------------ LEATHER ARMOR ------------------------
  // ###############################################################

  public ItemBuilder setLeatherArmorColor(final @NotNull Color color) {
    withMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    return this;
  }

  // ###############################################################
  // ------------------------- DURABILITY --------------------------
  // ###############################################################

  public ItemBuilder setDamage(int damage) {
    withMeta(Damageable.class, meta -> meta.setDamage(damage));
    return this;
  }

  public ItemBuilder setMaxDamage(int maxDamage) {
    withMeta(Damageable.class, meta -> meta.setMaxDamage(maxDamage));
    return this;
  }

  // ###############################################################
  // ------------------------ PLAYER HEADS -------------------------
  // ###############################################################

  public ItemBuilder setHeadFromName(final @NotNull String name) {
    if (name.isBlank()) return this;

    final var key = String.format("name:%s", name.toLowerCase());
    var profile = PROFILE_CACHE.getIfPresent(key);

    if (profile == null) {
      profile = Bukkit.createProfile(name);
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> meta.setPlayerProfile(finalProfile));
    return this;
  }

  public ItemBuilder setHeadFromUuid(final @NotNull UUID uuid) {
    final var key = String.format("uuid:%s", uuid);

    var profile = PROFILE_CACHE.getIfPresent(key);
    if (profile == null) {
      profile = Bukkit.createProfile(uuid);
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> meta.setPlayerProfile(finalProfile));
    return this;
  }

  public ItemBuilder setHeadFromBase64(final @NotNull String base64) {
    final var key = String.format("bas64:%s", base64.hashCode());
    var profile = PROFILE_CACHE.getIfPresent(key);

    if (profile == null) {
      profile = Bukkit.createProfile(base64);
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> {
      finalProfile.setProperty(new ProfileProperty("textures", base64));
      meta.setPlayerProfile(finalProfile);
    });
    return this;
  }

  // ###############################################################
  // ------------------------ ENCHANTMENTS -------------------------
  // ###############################################################

  public ItemBuilder addEnchant(final @NotNull Enchantment enchant, int level) {
    withMeta(meta -> meta.addEnchant(enchant, level, true));
    return this;
  }

  public ItemBuilder addEnchants(final Map<Enchantment, Integer> enchants) {
    withMeta(meta -> enchants.forEach((e, l) -> meta.addEnchant(e, l, true)));
    return this;
  }

  public ItemBuilder removeEnchant(final @NotNull Enchantment enchant) {
    withMeta(meta -> meta.removeEnchant(enchant));
    return this;
  }

  public ItemBuilder clearEnchants() {
    withMeta(meta -> meta.getEnchants().keySet().forEach(meta::removeEnchant));
    return this;
  }

  public ItemBuilder setEnchantGlint(boolean enabled) {
    withMeta(meta -> meta.setEnchantmentGlintOverride(enabled));
    return this;
  }

  // ###############################################################
  // ------------------------- ATTRIBUTES --------------------------
  // ###############################################################

  public ItemBuilder addAttribute(Attribute attribute, double value, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
    withMeta(meta -> {
      AttributeModifier modifier = new AttributeModifier(
        NamespacedKey.fromString(attribute.getKey().getKey() + "_" + value, DreamContext.getPlugin()),
        value, operation, slot);
      meta.addAttributeModifier(attribute, modifier);
    });
    return this;
  }

  public ItemBuilder addAttribute(Attribute attribute, double value, AttributeModifier.Operation operation) {
    return addAttribute(attribute, value, operation, EquipmentSlotGroup.HAND);
  }

  public ItemBuilder addAttribute(Attribute attribute, double value) {
    return addAttribute(attribute, value, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.HAND);
  }

  public ItemBuilder addAttackSpeed(double value) {
    return addAttribute(Attribute.ATTACK_SPEED, value);
  }

  // ###############################################################
  // ----------------------- PERSISTENT DATA -----------------------
  // ###############################################################

  public <T, Z> ItemBuilder addPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    withMeta(meta -> meta.getPersistentDataContainer().set(key, type, value));
    return this;
  }

  public ItemBuilder addStringTag(String key, String value) {
    return addPersistentData(new NamespacedKey(DreamContext.getPlugin(), key), PersistentDataType.STRING, value);
  }

  public ItemBuilder addBooleanTag(String key, boolean value) {
    return addPersistentData(new NamespacedKey(DreamContext.getPlugin(), key), PersistentDataType.BOOLEAN, value);
  }

  public ItemBuilder addIntTag(String key, int value) {
    return addPersistentData(new NamespacedKey(DreamContext.getPlugin(), key), PersistentDataType.INTEGER, value);
  }

  public ItemBuilder addDoubleTag(String key, double value) {
    return addPersistentData(new NamespacedKey(DreamContext.getPlugin(), key), PersistentDataType.DOUBLE, value);
  }

  // ###############################################################
  // ------------------------- COMPONENTS --------------------------
  // ###############################################################

  public <T> ItemBuilder setDataComponent(DataComponentType.Valued<T> key, T value) {
    is.setData(key, value);
    return this;
  }

  // ###############################################################
  // --------------------------- EXPORT ----------------------------
  // ###############################################################

  public ItemStack build() {
    this.is.setItemMeta(this.itemMeta);
    return this.is.clone();
  }

  public ItemBuilder copy(Consumer<ItemBuilder> modifier) {
    ItemBuilder clone = clone();
    modifier.accept(clone);
    return clone;
  }

  @Override
  public ItemBuilder clone() {
    return new ItemBuilder(is);
  }

  // ###############################################################
  // ------------------------ META EDITING -------------------------
  // ###############################################################

  public <T extends ItemMeta> ItemBuilder editMeta(Class<T> metaClass, Consumer<T> consumer) {
    withMeta(metaClass, consumer);
    return this;
  }

}