package fr.dreamin.dreamapi.core.item.builder;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.cache.Cache;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * SkullBuilder provides an easy and efficient way to build player heads with various sources.
 * <p>
 * Supported input methods:
 * <ul>
 *     <li>Player name</li>
 *     <li>UUID</li>
 *     <li>OfflinePlayer</li>
 *     <li>Base64-encoded texture</li>
 * </ul>
 * <p>
 * Internally, it uses a {@link Cache} to store resolved {@link PlayerProfile}s for one hour to avoid redundant lookups.
 *
 * Example:
 * <pre>{@code
 * ItemStack skull = new SkullBuilder()
 *     .player("DreamArchitect")
 *     .displayName("§bArchitect's Head")
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
public final class SkullBuilder extends ItemBuilder {

  public SkullBuilder() {
    super(Material.PLAYER_HEAD);
  }

  public SkullBuilder(final @NotNull String base64) {
    super(Material.PLAYER_HEAD);
    base64(base64);
  }

  public SkullBuilder(final @NotNull OfflinePlayer player) {
    super(Material.PLAYER_HEAD);
    player(player);
  }

  public SkullBuilder(final @NotNull UUID uuid) {
    super(Material.PLAYER_HEAD);
    uuid(uuid);
  }

  public SkullBuilder(final @NotNull ItemStack itemStack) {
    super(itemStack);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Sets the skull texture from a base64-encoded texture string.
   *
   * @param base64 the base64 texture string
   * @return this builder instance
   */
  public SkullBuilder base64(final @NotNull String base64) {
    setHeadFromBase64(base64);
    return this;
  }

  /**
   * Sets the skull texture using a Mojang username.
   *
   * @param name the player's name
   * @return this builder instance
   */
  public SkullBuilder name(final @NotNull String name) {
    setHeadFromName(name);
    return this;
  }

  /**
   * Sets the skull texture from an OfflinePlayer instance.
   *
   * @param player the offline player
   * @return this builder instance
   */
  public SkullBuilder player(final @NotNull OfflinePlayer player) {
    return this.uuid(player.getUniqueId());
  }


  /**
   * Sets the skull texture using a UUID.
   *
   * @param uuid the player's UUID
   * @return this builder instance
   */
  public SkullBuilder uuid(final @NotNull UUID uuid) {
    setHeadFromUuid(uuid);
    return this;
  }

}
