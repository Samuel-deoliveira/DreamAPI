package fr.dreamin.dreamapi.core.logger;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.services.Inject;
import logger.DebugService;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Inject
@RequiredArgsConstructor
@DreamAutoService(value = DebugService.class)
public class DebugServiceImpl implements DreamService, DebugService {

  private final Plugin plugin;

  private boolean globalDebug = false;
  private final Map<String, Boolean> categoryDebug = new ConcurrentHashMap<>();

  private int retentionDays = -1; // -1 = disabled


  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void setGlobalDebug(boolean enabled) {
    this.globalDebug = enabled;
  }

  @Override
  public boolean isGlobalDebug() {
    return this.globalDebug;
  }

  @Override
  public void setCategory(@NotNull String category, boolean enabled) {
    this.categoryDebug.put(category.toLowerCase(), enabled);
  }

  @Override
  public boolean isCategoryEnabled(String category) {
    if (!this.globalDebug) return false;

    return this.categoryDebug.getOrDefault(category.toLowerCase(), true);
  }

  @Override
  public Map<String, Boolean> getCategories() {
    return Collections.unmodifiableMap(this.categoryDebug);
  }

  @Override
  public void setRetentionDays(int days) {
    this.retentionDays = days;
  }

  @Override
  public int getRetentionDays() {
    return this.retentionDays;
  }

  @Override
  public void cleanupOldLogs() {
    if (this.retentionDays <= 0) return;

    final var debugFolder = new File(this.plugin.getDataFolder(), "debug");
    if (!debugFolder.exists()) return;

    final var threshold = System.currentTimeMillis() - (retentionDays * 86400000L);

    for (final var dayFolder : Objects.requireNonNull(debugFolder.listFiles())) {
      if (!dayFolder.isDirectory()) continue;

      if (dayFolder.lastModified() < threshold)
        deleteRecursive(dayFolder);
    }

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void deleteRecursive(final @NotNull File file) {
    if (file.isDirectory()) {
      for (final var f : Objects.requireNonNull(file.listFiles())) {
        deleteRecursive(f);
      }
    }
    file.delete();
  }

}
