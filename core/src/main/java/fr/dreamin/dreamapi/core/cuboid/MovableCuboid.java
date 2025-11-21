package fr.dreamin.dreamapi.core.cuboid;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
public final class MovableCuboid extends Cuboid {

  public MovableCuboid(Location locA, Location locB) {
    super(locA, locB);
  }

  /**
   * Moves the cuboid to have its center at the specified location.
   * Optionally moves entities and blocks within the cuboid relative to the new center.
   * @param centerLocation The new center location for the cuboid.
   * @param moveEntities If true, move entities within the cuboid.
   * @param moveBlocks If true, move blocks within the cuboid.
   */
  public void move(Location centerLocation, boolean moveEntities, boolean moveBlocks) {
    Location currentCenter = getCenter();
    if (currentCenter != null && centerLocation.getWorld().equals(currentCenter.getWorld())) {
      applyOffset(centerLocation.toVector().subtract(currentCenter.toVector()), moveEntities, moveBlocks);
    }
  }

  /**
   * Shifts the cuboid by a specified vector.
   * Optionally moves entities and blocks within the cuboid relative to the vector shift.
   * @param vector The vector by which to shift the cuboid.
   * @param moveEntities If true, move entities within the cuboid.
   * @param moveBlocks If true, move blocks within the cuboid.
   */
  public void add(Vector vector, boolean moveEntities, boolean moveBlocks) {
    if (getLocA().getWorld().equals(getLocB().getWorld())) {
      applyOffset(vector, moveEntities, moveBlocks);
    }
  }

  private void applyOffset(Vector offset, boolean moveEntities, boolean moveBlocks) {
    World world = getLocA().getWorld();

    // Move blocks if enabled
    if (moveBlocks) {
      Set<Location> newLocations = new HashSet<>();
      List<Block> blocks = getBlocks();
      HashMap<Block, BlockData> awaitChanged = new HashMap<>();

      for (Block block : blocks) {
        Location originalLocation = block.getLocation();
        Location newLocation = originalLocation.clone().add(offset);

        Block newWorldBlock = world.getBlockAt(newLocation);
        if (!awaitChanged.containsKey(newWorldBlock)) awaitChanged.put(newWorldBlock, block.getBlockData());
        newLocations.add(newLocation);  // Track the new locations

        // Only clear the original location if it's not part of the new location set
        if (!newLocations.contains(originalLocation)) block.setType(Material.AIR, false);
      }
      awaitChanged.forEach((block, blockData) ->{
        block.setBlockData(blockData, false);
      });
    }

    // Move entities if enabled
    if (moveEntities) {
      for (Entity entity : getEntities()) {
        if (entity.getWorld().equals(world)) {
          entity.teleport(entity.getLocation().add(offset));
        }
      }
    }

    // Shift locA and locB by the offset
    setLocA(getLocA().clone().add(offset));
    setLocB(getLocB().clone().add(offset));
  }
}
