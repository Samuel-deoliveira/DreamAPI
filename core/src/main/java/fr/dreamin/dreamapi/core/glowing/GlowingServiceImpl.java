package fr.dreamin.dreamapi.core.glowing;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.glowing.event.set.GlowingBlockSetEvent;
import fr.dreamin.dreamapi.core.glowing.event.set.GlowingEntitySetEvent;
import fr.dreamin.dreamapi.core.glowing.event.unset.GlowingBlockUnSetEvent;
import fr.dreamin.dreamapi.core.glowing.event.unset.GlowingEntityUnSetEvent;
import fr.dreamin.dreamapi.core.time.TickTask;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link GlowingService} using GlowingEntities and GlowingBlocks.
 * Provides a unified API for managing glowing effects.
 */
@DreamAutoService(value = GlowingService.class)
public class GlowingServiceImpl implements GlowingService, DreamService, Listener {

  private final Plugin plugin;

  private final @NotNull GlowingEntities glowingEntities;
  private final @NotNull GlowingBlocks glowingBlocks;

  /** Per viewer state: entities (with color) & blocks currently glowing for that viewer. */
  private final Map<UUID, ViewerState> byViewer = new ConcurrentHashMap<>();

  /** Reverse index: entityId -> viewers who see it glowing. */
  private final Map<UUID, Set<UUID>> viewersByEntity = new ConcurrentHashMap<>();

  /** Reverse index: blockKey -> viewers who see it glowing. */
  private final Map<BlockKey, Set<UUID>> viewersByBlock = new ConcurrentHashMap<>();;

  /**
   * Scheduled auto-unset for ENTITY per (viewer, entityId).
   * key = viewerId + "|" + entityId
   */
  private final Map<String, TickTask<?>> entityTimers = new ConcurrentHashMap<>();

  /**
   * Scheduled auto-unset for BLOCK per (viewer, blockKey).
   * key = viewerId + "|" + blockKey
   */
  private final Map<String, TickTask<?>> blockTimers = new ConcurrentHashMap<>();

  public GlowingServiceImpl(Plugin plugin) {
    this.plugin = plugin;
    this.glowingEntities = new GlowingEntities(plugin);
    this.glowingBlocks = new GlowingBlocks(plugin);
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  // ###############################################################
  // --------------------------- ENTITY ----------------------------
  // ###############################################################

  @Override
  public void glowEntity(@NotNull Entity entity, @NotNull ChatColor color, @NotNull Player... viewers) {
    for (var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showEntityToViewer(entity, color, viewer)) {
        var viewerState = byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.entities.put(entity, color);
        this.viewersByEntity.computeIfAbsent(entity.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());
      }
    }
  }

  @Override
  public void glowEntity(@NotNull Entity entity, @NotNull ChatColor color, long durationTicks, @NotNull Player... viewers) {
    for (var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showEntityToViewer(entity, color, viewer)) {
        var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.entities.put(entity, color);
        this.viewersByEntity.computeIfAbsent(entity.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());
        final var key = entityTimerKey(viewer.getUniqueId(), entity.getUniqueId());
        cancelIfPresent(this.entityTimers, key);
        this.entityTimers.put(key, runLater(durationTicks, () -> stopEntity(entity, viewer)));
      }
    }
  }

  @Override
  public void stopEntity(@NotNull Entity entity, @NotNull Player... viewers) {
    if (viewers.length == 0) {
      final var original = this.viewersByEntity.get(entity.getUniqueId());
      if (original == null || original.isEmpty()) return;
      final var viewersSet = new HashSet<>(original);
      if (viewersSet == null) return;

      for (var viewerId : viewersSet) {
        final var viewer = Bukkit.getPlayer(viewerId);
        if (viewer == null) continue;
        var viewerState = this.byViewer.get(viewerId);

        if (hideEntityFromViewer(entity, viewer)) {
          if (viewerState != null)
            viewerState.entities.remove(entity);

          original.remove(viewerId);
          cancelIfPresent(this.entityTimers, entityTimerKey(viewerId, entity.getUniqueId()));
        }
      }
      return;
    }

    for (var viewer : viewers) {
      var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null) viewerState.entities.remove(entity);

      var rev = this.viewersByEntity.get(entity.getUniqueId());

      if (hideEntityFromViewer(entity, viewer)) {
        if (rev != null) {
          rev.remove(viewer.getUniqueId());
          if (rev.isEmpty()) this.viewersByEntity.remove(entity.getUniqueId());
        }

        cancelIfPresent(this.entityTimers, entityTimerKey(viewer.getUniqueId(), entity.getUniqueId()));
      }
    }

  }

  // ###############################################################
  // --------------------------- BLOCK -----------------------------
  // ###############################################################

  @Override
  public void glowBlock(@NotNull Block block, @NotNull ChatColor color, @NotNull Player... viewers) {
    if (!isBlockValid(block) || viewers.length == 0) return;

    final var key = BlockKey.of(block);
    for (var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showBlockToViewer(block, color, viewer)) {
        var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.blocks.put(block, color);
        this.viewersByBlock.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());
      }
    }
  }

  @Override
  public void glowBlock(@NotNull Block block, @NotNull ChatColor color, long durationTicks, @NotNull Player... viewers) {
    if (!isBlockValid(block) || viewers.length == 0) return;

    final var key = BlockKey.of(block);
    for (var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showBlockToViewer(block, color, viewer)) {
        var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.blocks.put(block, color);
        this.viewersByBlock.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());

        final var tkey = blockTimerKey(viewer.getUniqueId(), key);
        cancelIfPresent(this.blockTimers, tkey);
        this.blockTimers.put(tkey, runLater(durationTicks, () -> stopBlock(block, viewer)));
      }
    }

  }

  @Override
  public void stopBlock(@NotNull Block block, @NotNull Player... viewers) {
    if (!isBlockValid(block)) return;

    final var key = BlockKey.of(block);

    if (viewers.length == 0) {
      final var original = this.viewersByBlock.get(block);
      if (original == null || original.isEmpty()) return;
      final var viewersSet = new HashSet<>(original);
      if (viewersSet != null) {
        for (var viewerId : viewersSet) {
          final var viewer = Bukkit.getPlayer(viewerId);
          if (viewer != null) {
            var viewerState = this.byViewer.get(viewerId);

            if (hideBlockFromViewer(block, viewer)) {
              if (viewerState != null) viewerState.blocks.remove(block);
              original.remove(viewerId);
              cancelIfPresent(this.blockTimers, blockTimerKey(viewerId, key));
            }
          }
        }
      }
      return;
    }

    for (var viewer : viewers) {
      if (viewer == null) continue;
      var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null) viewerState.blocks.remove(block);

      var rev = this.viewersByBlock.get(key);

      if (hideBlockFromViewer(block, viewer)) {
        if (rev != null) {
          rev.remove(viewer.getUniqueId());
          if (rev.isEmpty()) this.viewersByBlock.remove(key);
        }

        cancelIfPresent(this.blockTimers, blockTimerKey(viewer.getUniqueId(), key));
      }
    }
  }

  // ###############################################################
  // ----------------------- BULK / VIEWER -------------------------
  // ###############################################################

  @Override
  public void clearForViewer(@NotNull Player viewer) {
    final var viewerUniqueId = viewer.getUniqueId();
    var viewerState = byViewer.computeIfAbsent(viewerUniqueId, id -> new ViewerState());

    for (var entity : new ArrayList<>(viewerState.entities.keySet())) {
      var rev = this.viewersByEntity.get(entity.getUniqueId());

      if (hideEntityFromViewer(entity, viewer)) {
        if (rev != null) {
          rev.remove(viewerUniqueId);
          if (rev.isEmpty()) this.viewersByEntity.remove(entity.getUniqueId());
        }

        cancelIfPresent(this.entityTimers, entityTimerKey(viewerUniqueId, entity.getUniqueId()));
      }
    }

    for (var b : new ArrayList<>(viewerState.blocks.keySet())) {
      final var bk = BlockKey.of(b);
      var rev = this.viewersByBlock.get(bk);

      if (hideBlockFromViewer(b, viewer)) {
        if (rev != null) {
          rev.remove(viewerUniqueId);
          if (rev.isEmpty()) this.viewersByBlock.remove(bk);
        }

        cancelIfPresent(this.blockTimers, blockTimerKey(viewerUniqueId, bk));
      }
    }
  }

  @Override
  public void reapplyForViewer(@NotNull Player viewer) {
    if (!isViewerValid(viewer)) return;
    var viewerState = byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());

    for (var e : viewerState.entities.entrySet()) {
      final var ent = e.getKey();
      if (isEntityValid(ent))
        showEntityToViewer(ent, e.getValue(), viewer);
    }
    for (var e : viewerState.blocks.entrySet()) {
      final var block = e.getKey();
      if (isBlockValid(block))
        showBlockToViewer(block, e.getValue(), viewer);
    }
  }

  @Override
  public void reapplyTargetPlayerForAllViewers(Player target) {
    final var viewers = this.viewersByEntity.get(target.getUniqueId());
    if (viewers == null || viewers.isEmpty()) return;

    for (var viewerId : viewers) {
      final var viewer = Bukkit.getPlayer(viewerId);
      if (!isViewerValid(viewer)) continue;

      final var vs = this.byViewer.get(viewerId);
      if (vs == null) continue;
      final var color = vs.entities.get(target);
      if (color != null)
        showEntityToViewer(target, color, viewer);
    }
  }

  // ###############################################################
  // --------------------------- QUERIES ---------------------------
  // ###############################################################

  @Override
  public Set<Entity> getGlowingEntities(@NotNull Player viewer) {
    var viewerState = byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
    return Collections.unmodifiableSet(viewerState.entities.keySet());
  }

  @Override
  public Set<Block> getGlowingBlocks(@NotNull Player viewer) {
    var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
    return Collections.unmodifiableSet(viewerState.blocks.keySet());
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onViewerJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();

    reapplyTargetPlayerForAllViewers(player);
    reapplyForViewer(player);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onTargetEntityDeath(final @NotNull EntityDeathEvent event) {
    final var entity = event.getEntity();
    final var uuid = entity.getUniqueId();

    final var original = this.viewersByEntity.get(uuid);
    if (original == null || original.isEmpty()) return;
    final var viewersSet = new HashSet<>(original);

    for (var viewerId : viewersSet) {
      final var viewer = Bukkit.getPlayer(viewerId);
      final var viewerState = this.byViewer.get(viewerId);

      if (!isViewerValid(viewer)) {
        if (hideEntityFromViewer(entity, viewer)) {
          original.remove(entity.getUniqueId());
          viewerState.entities.remove(entity);
        }

      }
    }

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onBlockBreak(final @NotNull BlockBreakEvent event) {
    stopBlock(event.getBlock());
  }

  // ###############################################################
  // ---------------------------- GLUE -----------------------------
  // ###############################################################

  private boolean showEntityToViewer(final @NotNull Entity entity, final @NotNull ChatColor color, final @NotNull Player viewer) {
    final var event = new GlowingEntitySetEvent(viewer, color, entity);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.glowingEntities.setGlowing(entity, viewer, event.getColor());
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning(e.toString());
    }

    return true;
  }

  private boolean hideEntityFromViewer(final @NotNull Entity entity, final @NotNull Player viewer) {
    final var event = new GlowingEntityUnSetEvent(viewer, entity);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.glowingEntities.unsetGlowing(entity, viewer);
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning(e.toString());
    }

    return true;
  }

  private boolean showBlockToViewer(final @NotNull Block block, final @NotNull ChatColor color, final @NotNull Player viewer) {
    final var event = new GlowingBlockSetEvent(viewer, color, block);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.glowingBlocks.setGlowing(block, viewer, event.getColor());
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning(e.toString());
    }

    return true;
  }

  private boolean hideBlockFromViewer(final @NotNull Block block, final @NotNull Player viewer) {
    final var event = new GlowingBlockUnSetEvent(viewer, block);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.glowingBlocks.unsetGlowing(block, viewer);
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning(e.toString());
    }

    return true;
  }

  // ###############################################################
  // --------------------------- HELPER ----------------------------
  // ###############################################################

  private boolean isViewerValid(Player viewer) {
    return viewer != null && viewer.isOnline();
  }

  private boolean isEntityValid(Entity e) {
    return e != null && (e.isValid() || (e instanceof Player p && p.isOnline()));
  }

  private boolean isBlockValid(Block b) {
    return b != null && b.getType() != Material.AIR;
  }

  private String entityTimerKey(UUID viewerId, UUID entityId) {
    return viewerId + "|" + entityId;
  }

  private String blockTimerKey(UUID viewerId, BlockKey key) {
    return viewerId + "|" + key.toString();
  }

  private void cancelIfPresent(Map<String, TickTask<?>> map, String key) {
    TickTask<?> task = map.remove(key);
    if (task != null)
      task.stop();
  }

  /** Run a task later using your TickTask; fallback to Bukkit scheduler if needed. */
  private TickTask<?> runLater(long ticks, Runnable action) {
    return new TickTask() {
      @Override public void onEnd() { action.run(); }
    }.limit(1)
      .delay(ticks)
      .autoStop(true)
      .start();
  }

  // ###############################################################
  // --------------------------- CLASS -----------------------------
  // ###############################################################

  /** Per-viewer state. */
  private static final class ViewerState {
    final Map<Entity, ChatColor> entities = new HashMap<>();
    final Map<Block, ChatColor> blocks = new HashMap<>();
  }

  /** Stable key for a block position. */
  private record BlockKey(UUID world, int x, int y, int z) {
    static BlockKey of(Block b) {
      return new BlockKey(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }
    @Override public String toString() { return world + ":" + x + "," + y + "," + z; }
  }

}
