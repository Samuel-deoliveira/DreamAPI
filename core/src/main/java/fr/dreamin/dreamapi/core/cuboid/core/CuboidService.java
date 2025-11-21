package fr.dreamin.dreamapi.core.cuboid.core;

import fr.dreamin.dreamapi.core.cuboid.Cuboid;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface CuboidService {

  void register(final @NotNull Cuboid cuboid);
  void unregister(final @NotNull Cuboid cuboid);
  void clear();

  @NotNull Set<Cuboid> getCuboids();

  @NotNull Set<Cuboid> getCuboidsOf(final @NotNull UUID uuid);

}
