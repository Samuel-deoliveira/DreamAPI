package fr.dreamin.dreamapi.core.item.builder;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BannerBuilder extends ItemBuilder {

  private List<Pattern> patterns = new ArrayList<>();


  public BannerBuilder(final @NotNull BannerType type) {
    this(type, 1);
  }

  public BannerBuilder(final @NotNull BannerType type, int amount) {
    super(type.getType(), amount);
  }

  public BannerBuilder(final @NotNull ItemStack base) {
    super(base);

    final var meta = (BannerMeta) getItemMeta();
    patterns.addAll(meta.getPatterns());
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull BannerBuilder addPattern(final @NotNull Pattern pattern) {
    this.patterns.add(pattern);
    return this;
  }

  public @NotNull BannerBuilder addPattern(final @NotNull DyeColor color, final @NotNull PatternType type) {
    this.patterns.add(new Pattern(color, type));
    return this;
  }

  public @NotNull BannerBuilder setPatterns(@NotNull List<@NotNull Pattern> patterns) {
    this.patterns = patterns;
    return this;
  }

  public @NotNull BannerBuilder clearPatterns() {
    this.patterns.clear();
    return this;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################


  @Override
  public ItemStack build() {
    final var meta = (BannerMeta) getItemMeta();

    meta.setPatterns(patterns);

    return super.build();
  }
}
