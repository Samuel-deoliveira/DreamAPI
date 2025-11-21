package fr.dreamin.dreamapi.core.item.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum PotionType {
    SELF(Material.POTION),
    SPLASH(Material.SPLASH_POTION),
    LINGERING(Material.LINGERING_POTION);

    private final Material material;
  }
