package fr.dreamin.dreamapi.core.world.impl;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.DreamContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@DreamAutoService(value = WorldService.class)
public final class WorldServiceImpl implements WorldService, DreamService {

  private final Path worldContainer = Bukkit.getWorldContainer().toPath();
  private final Map<String, BukkitRunnable> tempWorlds = new ConcurrentHashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void resetWorld(@NotNull String worldLabel, @NotNull String templateLabel, @Nullable Consumer<World> callback) {
    final var targetPath = this.worldContainer.resolve(worldLabel);
    final var templatePath = this.worldContainer.resolve("templates").resolve(templateLabel);

    if (!Files.exists(templatePath) || !Files.isDirectory(templatePath)) {
      logError("Template '%s', not found in templates/", templateLabel);
      if (callback != null) callback.accept(null);
      return;
    }

    unloadWorld(worldLabel);

    Bukkit.getScheduler().runTaskAsynchronously(DreamContext.getPlugin(), () -> {
      try {
        deleteDirectory(targetPath);
        copyDirectory(templatePath, targetPath);
      } catch (Exception e) {
        logError("Failed to reset world '%s' from template '%s': %s", worldLabel, templateLabel, e.getMessage());
        if (callback != null) callback.accept(null);
        return;
      }

      loadWorldAsync(worldLabel, callback);
    });

  }

  @Override
  public void cloneWorld(@NotNull String sourceWorldLabel, @NotNull String cloneWorldLabel, @Nullable Consumer<World> callback) {
    final var sourcePath = worldContainer.resolve(sourceWorldLabel);
    final var clonePath = worldContainer.resolve(cloneWorldLabel);

    if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
      logError("Cannot clone: source world '%s' does not exist.", sourceWorldLabel);
      if (callback != null) callback.accept(null);
      return;
    }

    unloadWorld(cloneWorldLabel);

    Bukkit.getScheduler().runTaskAsynchronously(DreamContext.getPlugin(), () -> {
      try {
        deleteDirectory(clonePath);
        copyDirectory(sourcePath, clonePath);
      } catch (Exception e) {
        logError("Failed to clone world '%s' → '%s': %s", sourceWorldLabel, cloneWorldLabel, e.getMessage());
        if (callback != null) callback.accept(null);
        return;
      }

      loadWorldAsync(cloneWorldLabel, callback);
    });
  }

  @Override
  public void deleteWorld(@NotNull String worldLabel, @Nullable Runnable callback) {
    unloadWorld(worldLabel);
    final var worldPath = worldContainer.resolve(worldLabel);

    Bukkit.getScheduler().runTaskAsynchronously(DreamContext.getPlugin(), () -> {
      try {
        deleteDirectory(worldPath);
        logInfo("World '%s' deleted successfully.", worldLabel);
      } catch (IOException e) {
        logError("Failed to delete world '%s': %s", worldLabel, e.getMessage());
      }

      if (callback != null)
        Bukkit.getScheduler().runTask(DreamContext.getPlugin(), callback);
    });
  }

  @Override
  public void markTemporary(@NotNull String worldLabel, @NotNull Duration duration) {
    cancelAutoDelete(worldLabel);

    BukkitRunnable task = new BukkitRunnable() {
      @Override
      public void run() {
        deleteWorld(worldLabel, () ->
          logInfo("Temporary world '%s' auto-deleted after %d seconds.", worldLabel, duration.toSeconds())
        );
        tempWorlds.remove(worldLabel);
      }
    };

    final var ticks = duration.toSeconds() * 20;
    task.runTaskLater(DreamContext.getPlugin(), ticks);
    tempWorlds.put(worldLabel, task);

    logInfo("Temporary world '%s' scheduled for deletion in %d seconds.", worldLabel, duration.toSeconds());
  }

  @Override
  public void cancelAutoDelete(@NotNull String worldLabel) {
    final var task = tempWorlds.remove(worldLabel);
    if (task != null) {
      task.cancel();
      logInfo("Cancelled auto-deletion for world '%s'.", worldLabel);
    }
  }

  @Override
  public void extendAutoDelete(@NotNull String worldLabel, @NotNull Duration newDuration) {
    cancelAutoDelete(worldLabel);
    markTemporary(worldLabel, newDuration);
    logInfo("Rescheduled auto-deletion for '%s' in %d seconds.", worldLabel, newDuration.toSeconds());
  }

  @Override
  public boolean isTemporary(@NotNull String worldLabel) {
    return tempWorlds.containsKey(worldLabel);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void unloadWorld(String worldLabel) {
    final var world = Bukkit.getWorld(worldLabel);
    if (world != null) {
      if (Bukkit.unloadWorld(world, false))
        logInfo("World '%s' unloaded.", worldLabel);
      else
        logError("Failed to unload world '%s'.", worldLabel);
    }
  }

  private void loadWorldAsync(@NotNull String worldLabel, @Nullable Consumer<World> callback) {
    new BukkitRunnable() {
      @Override
      public void run() {
        final var world = WorldCreator.name(worldLabel).createWorld();
        if (world == null) logError("World '%s' failed to load.", worldLabel);
        else logInfo("World '%s' loaded successfully.", worldLabel);
        if (callback != null) callback.accept(world);
      }
    }.runTask(DreamContext.getPlugin());
  }

  private void deleteDirectory(@NotNull Path path) throws IOException {
    if (!Files.exists(path)) return;
    try (var walk = Files.walk(path)) {
      walk.sorted(Comparator.reverseOrder())
        .forEach(p -> {
          try { Files.deleteIfExists(p); }
          catch (IOException e) { logError("Failed to delete file: %s", p); }
        });
    }
  }

  private void copyDirectory(@NotNull Path src, @NotNull Path dest) throws IOException {
    Files.createDirectories(dest);
    try (var stream = Files.walk(src)) {
      stream.forEach(source -> {
        final var target = dest.resolve(src.relativize(source));
        try {
          if (Files.isDirectory(source)) Files.createDirectories(target);
          else Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new RuntimeException(e); }
      });
    }
  }

  private void logInfo(String msg, Object... args) { DreamContext.getPlugin().getLogger().info(String.format("[WorldService] " + msg, args)); }
  private void logError(String msg, Object... args) { DreamContext.getPlugin().getLogger().severe(String.format("[WorldService] " + msg, args)); }

}
