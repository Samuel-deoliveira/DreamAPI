package fr.dreamin.dreamapi.core.item.builder;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public final class FireworkBuilder extends ItemBuilder {

  private int power = -1;
  private List<FireworkEffect> effects = new ArrayList<>();

  public FireworkBuilder() {
    super(Material.FIREWORK_ROCKET);
  }

  public FireworkBuilder(final int amount) {
    super(Material.FIREWORK_ROCKET, amount);
  }

  public FireworkBuilder(final @NotNull ItemStack stack) {
    super(stack);

    final var meta = (FireworkMeta) getItemMeta();

    this.power = meta.getPower();
    this.effects = meta.getEffects();
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull FireworkBuilder setPower(@Range(from = 0, to = 127) int power) {
    this.power = power;
    return this;
  }

  public @NotNull FireworkBuilder addFireworkEffect(final @NotNull FireworkEffect.Builder builder) {
    effects.add(builder.build());
    return this;
  }

  public @NotNull FireworkBuilder setFireworkEffects(final @NotNull List<FireworkEffect> effects) {
    this.effects = effects;
    return this;
  }

  public @NotNull FireworkBuilder clearFireworkEffects() {
    this.effects.clear();
    return this;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public ItemStack build() {
    final var meta = (FireworkMeta) getItemMeta();

    if (this.power != 1) meta.setPower(power);
    meta.clearEffects();;
    meta.addEffects(effects);

    return super.build();
  }

}
