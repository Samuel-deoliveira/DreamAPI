package fr.dreamin.dreamapi.core.logger;

import logger.DebugService;
import logger.DreamLogger;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DreamLoggerImpl implements DreamLogger {

  private static final ConcurrentLinkedQueue<String> WRITE_QUEUE = new ConcurrentLinkedQueue<>();
  private static volatile boolean writerRunning = false;

  private final Plugin plugin;
  private final String category;
  private final DebugService debug;

  public DreamLoggerImpl(final @NotNull Plugin plugin, final @NotNull String category, final @NotNull DebugService debug) {
    this.plugin = plugin;
    this.category = category;
    this.debug = debug;

    startWriterThread();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void info(String msg, Object... args) {
    final var out = prefix("INFO") + " " + format(msg, args);
    this.plugin.getLogger().info(out);
    enqueue("[" + LocalTime.now() + "] [INFO] " + format(msg, args));
  }

  @Override
  public void warn(String msg, Object... args) {
    final var out = prefix("WARN") + " " + format(msg, args);
    this.plugin.getLogger().warning(out);
    enqueue("[" + LocalTime.now() + "] [WARN] " + format(msg, args));
  }

  @Override
  public void error(String msg, Object... args) {
    final var out = prefix("ERROR") + " " + format(msg, args);
    this.plugin.getLogger().severe(out);
    enqueue("[" + LocalTime.now() + "] [DEBUG] " + format(msg, args));
  }

  @Override
  public void debug(String msg, Object... args) {
    if (!isDebugEnabled()) return;
    this.plugin.getLogger().info(prefix("DEBUG") + format(msg, args));
  }

  @Override
  public boolean isDebugEnabled() {
    return this.debug.isDebugEnabled(this.category);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void enqueue(final @NotNull String line) {
    WRITE_QUEUE.add(line);
  }

  private void startWriterThread() {
    if (writerRunning) return;

    writerRunning = true;

    Thread thread = new Thread(() -> {
      while (writerRunning) {
        final var entry = WRITE_QUEUE.poll();

        if (entry == null) {
          try { Thread.sleep(10); } catch (Exception ignored) {}
          continue;
        }

        writeToFile(entry);
      }
    });

    thread.setDaemon(true);
    thread.setName("DreamAPI-Logger");
    thread.start();
  }

  private void writeToFile(final @NotNull String entry) {
    try {
      final var date = LocalDate.now().toString();
      final var folder = new File(plugin.getDataFolder(), "debug/" + date);
      if (!folder.exists()) folder.mkdirs();

      final var file = new File(folder, category + ".log");

      try (final var fw = new FileWriter(file, true)) {
        fw.write(entry + "\n");
      }
    } catch (Exception e) {
      plugin.getLogger().severe("[DreamAPI-Logger] Failed writing debug file: " + e.getMessage());
    }
  }

  private String prefix(final @NotNull String level) {
    return String.format("[DreamAPI][%s][%s]", this.category, level);
  }

  private String format(final @NotNull String msg, Object... args) {
    if (msg.contains("{}"))
      return String.format(msg.replace("{}", "%s"), args);

    return String.format(msg, args);
  }


}
