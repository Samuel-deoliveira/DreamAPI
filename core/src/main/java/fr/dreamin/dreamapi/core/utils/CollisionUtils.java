package fr.dreamin.dreamapi.core.utils;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CollisionUtils {

  /**
   * Get new adjusted location base on top of collision of a block.
   *
   * @param location The current location to adjust.
   * @return new location adjusted to bounding box or null if no bounding box.
   */
  public static @Nullable Location getTopCollisionLocation(@NotNull Location location) {
    final var block = location.getBlock();
    final var shape = block.getCollisionShape();
    BoundingBox topBox = null;

    var maxY = Double.NEGATIVE_INFINITY;
    for (var box : shape.getBoundingBoxes()) {
      if (box.getMaxY() > maxY) {
        maxY = box.getMaxY();
        topBox = box;
      }
    }

    if (topBox == null || maxY == Double.NEGATIVE_INFINITY) return null;

    final var finalY = location.getBlockY() + maxY;
    return new Location(location.getWorld(), location.getX(), finalY, location.getZ(), location.getYaw(), location.getPitch());
  }

  /**
   * Get new adjusted location base on top of collision of a block with a local position on bounding box.
   *
   * @param loc The current location to adjust.
   * @return new location adjusted to bounding box or null if not valid bounding box.
   */
  public static @Nullable Location getLocateTopCollisionLocation(@NotNull Location loc) {
    final var block = loc.getBlock();
    final var shape = block.getCollisionShape();

    final var blockX = block.getX();
    final var blockZ = block.getZ();
    final var localX = loc.getX() - blockX;
    final var localZ = loc.getZ() - blockZ;

    var maxY = Double.NEGATIVE_INFINITY;

    for (var box : shape.getBoundingBoxes()) {
      if (box.contains(localX, box.getMinY(), localZ))
        maxY = Math.max(maxY, box.getMaxY());
    }

    if (maxY == Double.NEGATIVE_INFINITY) return null;

    final var finalY = block.getY() + maxY;
    return new Location(loc.getWorld(), loc.getX(), finalY, loc.getZ(), loc.getYaw(), loc.getPitch());
  }

}
