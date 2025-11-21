package fr.dreamin.dreamapi.core.packUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of {@link GlobalTexturesService} using Jackson for YAML/JSON persistence.
 * Provides runtime modification, file synchronization, and default mappings.
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@DreamAutoService(value = GlobalTexturesService.class)
public class GlobalTexturesServiceImpl implements GlobalTexturesService, DreamService {

  private static final ObjectMapper JSON = new ObjectMapper();

  private final Map<String, String> textures = new HashMap<>();

  private File configFile;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void init(@NotNull File dataFolder) {
    if (!dataFolder.exists()) dataFolder.mkdirs();
    this.configFile = new File(dataFolder, "textures.json");
    load();
  }

  @Override
  public void load() {
    if (!this.configFile.exists()) {
      applyDefaults();
      save();
      return;
    }

    try {
      Map raw = JSON.readValue(this.configFile, Map.class);

      this.textures.clear();

      if (raw != null) raw.forEach((k, v) -> this.textures.put((String) k, String.valueOf(v)));
    } catch (IOException e ) {
      e.printStackTrace();
      applyDefaults();
    }

  }

  @Override
  public void save() {
    try {
      JSON.writerWithDefaultPrettyPrinter().writeValue(this.configFile, this.textures);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void applyDefaults() {
    this.textures.clear();

    this.textures.put("black_screen", "\uE000");
    this.textures.put("left_click", "ꀑ");
    this.textures.put("right_click", "ꀒ");
    this.textures.put("go", "\uA000");
    this.textures.put("start", "\uA000");
    this.textures.put("end", "\uA000");

    for (var i = 1; i <= 10; i++) {
      this.textures.put(String.valueOf(i), String.valueOf((char) (0xA000 + i)));
    }
  }

  @Override
  public void add(@NotNull String key, @NotNull String value) {
    this.textures.putIfAbsent(key.toLowerCase(), value);
  }

  @Override
  public void update(@NotNull String key, @NotNull String value) {
    this.textures.put(key.toLowerCase(), value);
  }

  @Override
  public void remove(@NotNull String key) {
    this.textures.remove(key.toLowerCase());
  }

  @Override
  public String get(@NotNull String key) {
    return this.textures.getOrDefault(key.toLowerCase(), "?");
  }

  @Override
  public String get(int key) {
    return this.textures.getOrDefault(String.valueOf(key), "?");
  }

  @Override
  public String go() {
    return get("go");
  }

  @Override
  public String start() {
    return get("start");
  }

  @Override
  public String end() {
    return get("end");
  }

  @Override
  public Map<String, String> getAll() {
    return Collections.unmodifiableMap(textures);
  }
}
