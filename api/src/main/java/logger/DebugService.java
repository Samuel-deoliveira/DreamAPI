package logger;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DebugService {

  void setGlobalDebug(final boolean enabled);
  boolean isGlobalDebug();

  void setCategory(final @NotNull String category, final boolean enabled);
  boolean isCategoryEnabled(String category);

  Map<String, Boolean> getCategories();

  void setRetentionDays(final int days);
  int getRetentionDays();

  void cleanupOldLogs();

}
