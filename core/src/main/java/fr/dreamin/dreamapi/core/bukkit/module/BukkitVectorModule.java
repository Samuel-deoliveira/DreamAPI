package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Map;

public class BukkitVectorModule extends SimpleModule {

  public BukkitVectorModule() {
    super("BukkitVectorModule");

    addSerializer(Vector.class, new com.fasterxml.jackson.databind.JsonSerializer<>() {
      @Override
      public void serialize(final Vector value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        gen.writeObject(value.serialize());
      }
    });

    addDeserializer(Vector.class, new JsonDeserializer<>() {
      @Override
      public Vector deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JacksonException {
        final var mapper = (ObjectMapper) jsonParser.getCodec();
        final var typeFactory = mapper.getTypeFactory();
        final var mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);

        return Vector.deserialize(mapper.readValue(jsonParser, mapType));
      }
    });
  }

}
