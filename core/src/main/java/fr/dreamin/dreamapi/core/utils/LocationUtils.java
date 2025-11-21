package fr.dreamin.dreamapi.core.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LocationUtils {

  /**
   * Calculate the opposite yaw given the current yaw.
   * @param yaw The current yaw
   * @return The opposite yaw
   */
  public static float getOppositeYaw(float yaw) {
    // Normalize the yaw to be within [0, 360)
    float normalizedYaw = ((yaw % 360) + 360) % 360;

    // Add 180 degrees to get the opposite direction, then normalize again
    return (normalizedYaw + 180) % 360;
  }

  /**
   * Checks if any location in the list is within a given radius from the center location.
   *
   * @param locList   List of locations to check.
   * @param centerLoc The center location to compare.
   * @param radius    The radius within which to check.
   * @return true if a location is within the radius, false otherwise.
   */
  public static boolean hasLocationInRayon(List<Location> locList, Location centerLoc, double radius) {
    for (var location : locList) {
      if (centerLoc.distance(location) <= radius) return true;
    }
    return false;
  }

  /**
   * Get Ground location to make sure they are standing on solid ground,
   * considering special block types like stairs and slabs.
   *
   * @param start The start location.
   * @return The adjusted location at ground level.
   */
  public static @Nullable Location findNearestGroundLocation(@NotNull Location start) {
    final int maxDepth = 400;
    final var baseY = start.getBlockY();

    for (var i = 0; i < maxDepth; i++) {
      final var testLoc = start.clone();
      testLoc.setY(baseY - i);

      final var block = testLoc.getBlock();
      final var shape = block.getCollisionShape();

      if (shape.getBoundingBoxes().isEmpty()) continue;

      final var top = CollisionUtils.getLocateTopCollisionLocation(testLoc);
      if (top != null && top.getY() <= start.getY())
        return new Location(start.getWorld(), start.getX(), top.getY(), start.getZ(), start.getYaw(), start.getPitch());
    }

    return null;
  }
}
