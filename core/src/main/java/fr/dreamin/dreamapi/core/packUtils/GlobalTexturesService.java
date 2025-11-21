package fr.dreamin.dreamapi.core.packUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

/**
 * Service interface for managing character-based texture mappings.
 * Supports dynamic add/update/remove operations and file-based persistence.
 */
public interface GlobalTexturesService {

  /**
   * Initializes the service and loads from file (YAML or JSON).
   *
   * @param dataFolder plugin data folder
   */
  void init(final @NotNull File dataFolder);

  /**
   * Loads mappings from the configuration file.
   */
  void load();

  /**
   * Saves current mappings to disk.
   */
  void save();

  /**
   * Applies default values (A000–A010).
   */
  void applyDefaults();

  /**
   * Adds a new entry if it does not exist.
   */
  void add(final @NotNull String key, final @NotNull String value);

  /**
   * Updates or inserts an entry.
   */
  void update(final @NotNull String key, final @NotNull String value);

  /**
   * Removes an entry by key.
   */
  void remove(final @NotNull String key);

  /**
   * Gets a mapped texture by string key.
   */
  String get(final @NotNull String key);

  /**
   * Gets a mapped texture by integer key.
   */
  String get(final int key);

  /**
   * Shortcut for “go” glyph.
   */
  String go();

  /**
   * Shortcut for “start” glyph.
   */
  String start();

  /**
   * Shortcut for “end” glyph.
   */
  String end();

  /**
   * @return immutable view of all loaded textures.
   */
  Map<String, String> getAll();

}
