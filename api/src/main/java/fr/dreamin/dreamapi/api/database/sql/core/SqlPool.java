package fr.dreamin.dreamapi.api.database.sql.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Global SQL connection pool that manages all database managers (MySQL, PostgreSQL, etc.).
 * <p>
 * Supports registration, lazy creation, retrieval, and clean shutdown of all SQL connections.
 */
public final class SqlPool {

  private static final Map<String, SqlManager> DATABASES = new ConcurrentHashMap<>();

  /**
   * Registers a new SQL database connection and connects it immediately.
   *
   * @param db The {@link SqlManager} instance (MySQL, Postgres, etc.)
   * @return The same instance, connected and stored.
   */
  public static @NotNull <T extends SqlManager> T register(final @NotNull T db) {
    DATABASES.put(db.getLabel(), db);
    db.connect();
    return db;
  }

  /**
   * Retrieves a registered SQL database by its label.
   *
   * @param label The unique label for the database.
   * @return The registered {@link SqlManager}.
   * @throws IllegalStateException if no database is registered with that label.
   */
  public static @NotNull SqlManager get(final @NotNull String label) {
    final SqlManager db = DATABASES.get(label);
    if (db == null)
      throw new IllegalStateException("[DreamAPI] No SQL database registered with label: " + label);
    return db;
  }

  /**
   * Retrieves a database by label or registers it if it does not exist.
   *
   * @param label   The label of the database.
   * @param creator The supplier used to create and register the database if missing.
   * @return The registered or newly created {@link SqlManager}.
   */
  public static @NotNull <T extends SqlManager> T getOrRegister(final @NotNull String label, final @NotNull Supplier<T> creator) {
    return (T) DATABASES.computeIfAbsent(label, key -> {
      final SqlManager db = creator.get();
      db.connect();
      return db;
    });
  }

  /**
   * Tries to find a database by label without throwing.
   *
   * @param label The label of the database.
   * @return Optional containing the {@link SqlManager} if found.
   */
  public static Optional<SqlManager> find(final @NotNull String label) {
    return Optional.ofNullable(DATABASES.get(label));
  }

  // ###############################################################
  // ------------------------ UTILITIES ----------------------------
  // ###############################################################

  /**
   * Checks if a label is registered.
   */
  public static boolean exists(final @NotNull String label) {
    return DATABASES.containsKey(label);
  }

  /** Returns all registered databases. */
  public static Collection<SqlManager> all() {
    return Collections.unmodifiableCollection(DATABASES.values());
  }

  /** Closes all databases and clears the pool. */
  public static void closeAll() {
    DATABASES.values().forEach(SqlManager::disconnect);
    DATABASES.clear();
    DreamAPI.getAPI().getLogger().info("§c[DreamAPI] All SQL database connections closed.");
  }

  /** Unregisters a single database and closes its connection. */
  public static void unregister(String label) {
    Optional.ofNullable(DATABASES.remove(label)).ifPresent(SqlManager::disconnect);
  }
}
