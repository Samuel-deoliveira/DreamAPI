package fr.dreamin.dreamapi.core.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Utility class for various mathematical operations and utilities used in the game.
 */
public class MathUtils {

  /**
   * Shuffles the elements of an array using the Fisher-Yates shuffle algorithm.
   *
   * @param array The array to shuffle.
   */
  public static void randomizeArray(final @NotNull Object[] array) {
    final var random = new Random();
    for (var i = array.length - 1; i > 0; i--) {
      final var j = random.nextInt(i + 1);
      final var temp = array[i];
      array[i] = array[j];
      array[j] = temp;
    }
  }

  /**
   * Shuffles the elements of a list using Collections.shuffle.
   *
   * @param list The list to shuffle.
   * @return The shuffled list.
   */
  public static List<?> randomizeList(final @NotNull List<?> list) {
    Collections.shuffle(list); // Utilisation directe de Collections.shuffle
    return list;
  }

  /**
   * Calculates the ratio between two numbers and rounds it to three decimal places.
   *
   * @param parameter The numerator.
   * @param nbrDivise The denominator.
   * @return The rounded ratio.
   */
  public static double calculateRatio(final int parameter, final int nbrDivise) {
    return Math.round((double) parameter / (double) nbrDivise * 1000.0) / 1000.0;
  }

  /**
   * Determines the directional arrow symbol relative to a player's yaw to point towards a target location.
   *
   * @param player The player whose yaw is used.
   * @param loc The target location.
   * @return The directional arrow symbol.
   */
  public static String getDirectionalArrow(final @NotNull Player player, final @NotNull Location loc) {
    final var dx = loc.getX() - player.getLocation().getX();
    final var dz = loc.getZ() - player.getLocation().getZ();
    final var angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
    final var rotation = (player.getLocation().getYaw() - angle + 360) % 360;

    if (rotation >= 337.5 || rotation < 22.5) return "⬆";
    if (rotation < 67.5) return "⬉";
    if (rotation < 112.5) return "⬅";
    if (rotation < 157.5) return "⬋";
    if (rotation < 202.5) return "⬇";
    if (rotation < 247.5) return "⬊";
    if (rotation < 292.5) return "➡";
    return "⬈";
  }

  /**
   * Generates a random number between the specified minimum and maximum values (inclusive).
   *
   * @param min The minimum value.
   * @param max The maximum value.
   * @return A random number between min and max.
   */
  public static int getRandomNumber(final int min, final int max) {
    return new Random().nextInt((max - min) + 1) + min;
  }

  /**
   * Chooses an element from a list of names based on weighted random values.
   *
   * @param names The array of names.
   * @param values The corresponding weight values.
   * @return The chosen name.
   */
  public static String chooseElement(final @NotNull String[] names, final @NotNull Double[] values) {
    final var total = Arrays.stream(values).mapToDouble(Double::doubleValue).sum();
    var randomValue = new Random().nextDouble() * total;

    for (var i = 0; i < values.length; i++) {
      randomValue -= values[i];
      if (randomValue <= 0) return names[i];
    }
    return names[names.length - 1];
  }

  /**
   * Reverses a list starting from a given index.
   *
   * @param array The list to reverse.
   * @param id The starting index.
   * @return The reversed list.
   */
  public static List<String> reverseArray(final @NotNull List<String> array, int id) {
    final var result = new ArrayList<String>(array.size());
    for (var i = 0; i < array.size(); i++) {
      result.add(array.get((id + i) % array.size()));
    }
    return result;
  }

  /**
   * Calculates the progress as a percentage between a minimum and maximum value.
   *
   * @param min The current value.
   * @param max The maximum value.
   * @return The progress as a percentage.
   */
  public static float calculateProgress(final int min, final int max) {
    return (float) min / (float) max;
  }

  /**
   * Calculates the percentage of min compared to max.
   *
   * @param min The current value.
   * @param max The maximum value.
   * @return The percentage.
   */
  public static double calculatePercentage(final double min, final double max) {
    return min * 100 / max;
  }

  /**
   * Reorders a list of ItemStacks alphabetically, starting from a given index.
   *
   * @param items The list of ItemStacks to reorder.
   * @param id The starting index for the reordering.
   * @return The reordered list.
   */
  public static List<ItemStack> reorderItemsAZ(final @NotNull List<ItemStack> items, int id) {
    if (items.isEmpty()) throw new IllegalArgumentException("Invalid list of items");

    id = (id % items.size() + items.size()) % items.size();
    final var reorderedItems = new ArrayList<ItemStack>(items.size());
    reorderedItems.addAll(items.subList(id, items.size()));
    reorderedItems.addAll(items.subList(0, id));

    return reorderedItems;
  }

  /**
   * Rounds the coordinates of a location to the nearest block and adjusts the yaw to the closest cardinal direction.
   *
   * @param location The location to round.
   * @return The rounded location.
   */
  public static Location roundLocation(final @NotNull Location location) {
    final var x = location.getBlockX() + 0.5;
    final var y = Math.round(location.getY());
    final var z = location.getBlockZ() + 0.5;
    final var yaw = location.getYaw();

    float[] yaws = {0, 45, 90, 135, 180, -45, -90, -135, -180};
    var closestYaw = yaws[0];
    for (var yVal : yaws) {
      if (Math.abs(yVal - yaw) < Math.abs(closestYaw - yaw)) closestYaw = yVal;
    }

    return new Location(location.getWorld(), x, y, z, closestYaw, 0);
  }

  /**
   * Calculates a normalized direction vector from one location to another.
   *
   * @param start The starting location.
   * @param destination The destination location.
   * @return The normalized direction vector.
   */
  public static Vector getDirectionVector(final @NotNull Location start, final @NotNull Location destination) {
    final var directionX = destination.getX() - start.getX();
    final var directionY = destination.getY() - start.getY();
    final var directionZ = destination.getZ() - start.getZ();

    return new Vector(directionX, directionY, directionZ).normalize();
  }

  /**
   * Rounds a yaw angle to the nearest cardinal direction (0, 90, 180, 270 degrees).
   *
   * @param yaw The yaw angle to round.
   * @return The rounded yaw angle.
   */
  public static float roundYawToCardinalDirection(float yaw) {
    yaw = (yaw % 360 + 360) % 360;
    if (yaw < 45 || yaw >= 315) return 0;
    if (yaw < 135) return 90;
    if (yaw < 225) return 180;
    return 270;
  }

  /**
   * Gets a location in front of an entity based on its yaw.
   *
   * @param entity The entity to get the location in front of.
   * @param roundedYaw The rounded yaw direction.
   * @param addDistance The distance in blocks to the front location.
   * @return The new location in front of the entity.
   */
  @NotNull
  public static Location getLocationBefore(final @NotNull Entity entity, final float roundedYaw, final double addDistance) {
    final var rad = Math.toRadians(roundedYaw);
    final var x = entity.getLocation().getX() + Math.sin(rad) * addDistance;
    final var z = entity.getLocation().getZ() + Math.cos(rad) * addDistance;

    final var newLocation = new Location(entity.getWorld(), x, entity.getLocation().getY(), z);
    newLocation.setYaw(entity.getLocation().getYaw());
    return newLocation;
  }

  /**
   * Gets a location behind the entity based on its yaw.
   * This calculates a new position directly behind the entity at a specified distance.
   *
   * @param entity The entity to get the location behind.
   * @param roundedYaw The rounded yaw direction (0, 90, 180, or 270 degrees).
   * @param addDistance The distance in blocks behind the entity.
   * @return The new location behind the entity.
   */
  @NotNull
  public static Location getLocationBack(final @NotNull Entity entity, final float roundedYaw, final double addDistance) {
    final var rad = Math.toRadians(roundedYaw);

    // Calculate new coordinates behind the entity
    final var x = entity.getLocation().getX() - Math.sin(rad) * addDistance;
    final var z = entity.getLocation().getZ() - Math.cos(rad) * addDistance;

    // Create a new location behind the entity
    final var newLocation = new Location(entity.getWorld(), x, entity.getLocation().getY(), z);
    newLocation.setYaw(entity.getLocation().getYaw());
    return newLocation;
  }

  /**
   * Gets a location to the left of the entity based on its yaw.
   * This calculates a new position to the left of the entity at a specified distance.
   *
   * @param entity The entity to get the location to the left of.
   * @param roundedYaw The rounded yaw direction (0, 90, 180, or 270 degrees).
   * @param addDistance The distance in blocks to the left of the entity.
   * @return The new location to the left of the entity.
   */
  @NotNull
  public static Location getLocationLeft(final @NotNull Entity entity, final float roundedYaw, final double addDistance) {
    final var adjustedYaw = Math.toRadians(roundedYaw - 90); // Move left 90 degrees relative to yaw

    // Calculate new coordinates to the left of the entity
    final var x = entity.getLocation().getX() + Math.sin(adjustedYaw) * addDistance;
    final var z = entity.getLocation().getZ() + Math.cos(adjustedYaw) * addDistance;

    // Create a new location to the left of the entity
    final var newLocation = new Location(entity.getWorld(), x, entity.getLocation().getY(), z);
    newLocation.setYaw(entity.getLocation().getYaw());
    return newLocation;
  }

  /**
   * Gets a location to the right of the entity based on its yaw.
   * This calculates a new position to the right of the entity at a specified distance.
   *
   * @param entity The entity to get the location to the right of.
   * @param roundedYaw The rounded yaw direction (0, 90, 180, or 270 degrees).
   * @param addDistance The distance in blocks to the right of the entity.
   * @return The new location to the right of the entity.
   */
  @NotNull
  public static Location getLocationRight(final @NotNull Entity entity, final float roundedYaw, final double addDistance) {
    final var adjustedYaw = Math.toRadians(roundedYaw + 90); // Move right 90 degrees relative to yaw

    // Calculate new coordinates to the right of the entity
    final var x = entity.getLocation().getX() + Math.sin(adjustedYaw) * addDistance;
    final var z = entity.getLocation().getZ() + Math.cos(adjustedYaw) * addDistance;

    // Create a new location to the right of the entity
    final var newLocation = new Location(entity.getWorld(), x, entity.getLocation().getY(), z);
    newLocation.setYaw(entity.getLocation().getYaw());
    return newLocation;
  }
}