package fr.dreamin.dreamapi.api.database.sql.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Abstract base class for SQL database connections.
 * Provides shared logic for all database managers (MySQL, PostgreSQL, etc.).
 */
@Getter
public abstract class SqlManager {

  protected final @NotNull String label;
  protected final @NotNull String host;
  protected final int port;
  protected final @NotNull String database;
  protected final @NotNull String username;
  protected final @NotNull String password;
  protected boolean autoReconnect = true;

  protected Connection connection;
  protected final Logger logger = DreamAPI.getAPI().getLogger();

  protected SqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password
  ) {
    this.label = label;
    this.host = host;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
  }

  // ###############################################################
  // -------------------- ABSTRACT METHODS -------------------------
  // ###############################################################

  /**
   * Must build and return the database-specific JDBC URL.
   * Example: jdbc:mysql://localhost:3306/test?useSSL=false
   */
  protected abstract @NotNull String buildJdbcUrl();

  /**
   * Allows subclasses to add database-specific connection properties.
   */
  protected void applyProperties(final @NotNull Properties props) {
    props.setProperty("user", username);
    props.setProperty("password", password);
    props.setProperty("characterEncoding", "utf8");
    props.setProperty("useUnicode", "true");
  }

  // ###############################################################
  // ---------------------- CONNECTION LOGIC -----------------------
  // ###############################################################

  public boolean connect() {
    if (isOnline()) return true;

    try {
      final String url = buildJdbcUrl();
      final Properties props = new Properties();
      applyProperties(props);

      connection = DriverManager.getConnection(url, props);
      logger.info(String.format("[%s] Connected to %s:%d/%s", label, host, port, database));
      return true;
    } catch (SQLException e) {
      logger.severe(String.format("[%s] Failed to connect: %s", label, e.getMessage()));
      return false;
    }
  }

  public void disconnect() {
    if (!isOnline()) return;
    try {
      connection.close();
      connection = null;
      logger.info(String.format("[%s] Connection closed", label));
    } catch (SQLException e) {
      logger.severe(String.format("[%s] Failed to close connection: %s", label, e.getMessage()));
    }
  }

  public boolean isOnline() {
    try {
      return connection != null && !connection.isClosed();
    } catch (SQLException e) {
      return false;
    }
  }

  public void reconnect() {
    disconnect();
    connect();
  }

  public Connection getConnection() {
    try {
      if (!isOnline()) connect();
      return connection;
    } catch (Exception e) {
      logger.severe(String.format("[%s] Could not provide a valid connection: %s", label, e.getMessage()));
      return null;
    }
  }

  public boolean testConnection() {
    try (var stmt = getConnection().createStatement()) {
      stmt.execute("SELECT 1");
      return true;
    } catch (SQLException e) {
      logger.warning(String.format("[%s] Test connection failed: %s", label, e.getMessage()));
      return false;
    }
  }
}
