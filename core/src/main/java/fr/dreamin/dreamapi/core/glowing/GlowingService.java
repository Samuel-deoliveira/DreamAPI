package fr.dreamin.dreamapi.core.glowing;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Service for managing glowing effects on entities and blocks.
 * Uses Paper 1.21.10-compatible mechanics via GlowingEntities/GlowingBlocks.
 */
public interface GlowingService {

  // ###############################################################
  // --------------------------- ENTITY ----------------------------
  // ###############################################################

  /** Make an entity glow for one or more viewers (no auto-unset). */
  void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final @NotNull Player... viewers);

  /** Make an entity glow for viewers, auto-unset after duration ticks. */
  void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers);

  /** Stop glowing an entity for the given viewers (if none passed, stop for all viewers). */
  void stopEntity(final @NotNull Entity entity, final @NotNull  Player... viewers);

  // ###############################################################
  // --------------------------- BLOCK -----------------------------
  // ###############################################################

  /** Make a block glow for one or more viewers (no auto-unset). */
  void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final @NotNull Player... viewers);

  /** Make a block glow for viewers, auto-unset after duration ticks. */
  void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers);

  /** Stop glowing a block for the given viewers (if none passed, stop for all viewers). */
  void stopBlock(final @NotNull Block block, final @NotNull Player... viewers);

  // ###############################################################
  // ----------------------- BULK / VIEWER -------------------------
  // ###############################################################

  /** Clear all glows (entities & blocks) visible by this viewer. */
  void clearForViewer(final @NotNull Player viewer);

  /** Re-apply all glows for a viewer (e.g., on join). */
  void reapplyForViewer(final @NotNull Player viewer);

  void reapplyTargetPlayerForAllViewers(final @NotNull Player target);

  // ###############################################################
  // --------------------------- QUERIES ---------------------------
  // ###############################################################

  /** Returns the set of glowing entities seen by the viewer. */
  Set<Entity> getGlowingEntities(final @NotNull Player viewer);

  /** Returns the set of glowing blocks seen by the viewer. */
  Set<Block> getGlowingBlocks(final @NotNull Player viewer);
}
