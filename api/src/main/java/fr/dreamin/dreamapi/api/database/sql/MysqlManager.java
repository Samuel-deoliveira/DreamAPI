package fr.dreamin.dreamapi.api.database.sql;

import fr.dreamin.dreamapi.api.database.sql.core.SqlManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single MySQL database connection, managed individually.
 */
@Getter
@Setter
public final class MysqlManager extends SqlManager {

  public MysqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password
  ) {
    super(label, host, port, database, username, password);
  }

  public MysqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password,
    final boolean autoReconnect
  ) {
    super(label, host, port, database, username, password);
    this.autoReconnect = autoReconnect;
  }

  @Override
  protected @NotNull String buildJdbcUrl() {
    return String.format(
      "jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=%b&serverTimezone=UTC",
      host, port, database, autoReconnect
    );
  }
}