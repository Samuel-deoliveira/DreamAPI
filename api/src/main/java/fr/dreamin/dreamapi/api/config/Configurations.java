package fr.dreamin.dreamapi.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.Module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;


public final class Configurations {

  private static final Set<Module> REGISTERED_MODULES = new HashSet<>();
  public static ObjectMapper MAPPER = createMapper();

  private Configurations() {
  }

  private static ObjectMapper createMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);

    for (Module module : REGISTERED_MODULES) {
      mapper.registerModule(module);
    }

    return mapper;
  }

  public static <T> T loadJson(File file, Class<T> tClass) throws IOException {
    return MAPPER.readValue(file, tClass);
  }

  public static <T> T loadJson(File file, TypeReference<T> typeReference) throws IOException {
    return MAPPER.readValue(file, typeReference);
  }

  public static <T> T copyAndLoadJson(InputStream srcFile, File file, Class<T> tClass) throws IOException {
    copyIfNotExist(srcFile, file);
    return loadJson(file, tClass);
  }

  public static <T> T copyAndLoadJson(InputStream srcFile, File file, TypeReference<T> typeReference)
    throws IOException {
    copyIfNotExist(srcFile, file);
    return loadJson(file, typeReference);
  }

  public static void copyIfNotExist(InputStream srcFile, File destFile) throws IOException {
    if (!destFile.exists())
      Files.copy(srcFile, destFile.toPath());
  }

  public static void saveJson(File file, Object object) throws IOException {
    MAPPER.writeValue(file, object);
  }

  public static void addModule(Module module) {
    REGISTERED_MODULES.add(module);
    MAPPER.registerModule(module);
  }

  public static boolean containModule(Class<? extends Module> moduleClass) {
    return REGISTERED_MODULES.stream().anyMatch(module -> module.getClass().equals(moduleClass));
  }

  public static void removeModule(Class<? extends Module> moduleClass) {
    REGISTERED_MODULES.removeIf(m -> m.getClass().equals(moduleClass));
    MAPPER = createMapper();
  }

}