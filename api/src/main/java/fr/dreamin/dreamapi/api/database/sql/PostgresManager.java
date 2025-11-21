package fr.dreamin.dreamapi.api.database.sql;

import fr.dreamin.dreamapi.api.database.sql.core.SqlManager;
import org.jetbrains.annotations.NotNull;

/**
 * PostgreSQL database manager implementation.
 */
public final class PostgresManager extends SqlManager {

  public PostgresManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password
  ) {
    super(label, host, port, database, username, password);
  }

  @Override
  protected @NotNull String buildJdbcUrl() {
    return String.format(
      "jdbc:postgresql://%s:%d/%s",
      host, port, database
    );
  }

  @Override
  protected void applyProperties(@NotNull java.util.Properties props) {
    super.applyProperties(props);
    // You can add Postgres-specific properties here
    props.setProperty("ApplicationName", "DreamAPI");
  }
}