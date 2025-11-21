package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.core.particle.ParticleShape;

import java.io.IOException;

public final class ParticleShapeModule extends SimpleModule {

  public ParticleShapeModule() {
    super("ParticleShapeModule");

    addSerializer(ParticleShape.class, new JsonSerializer<>() {
      @Override
      public void serialize(ParticleShape shape, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final var mapper = (ObjectMapper) gen.getCodec();
        final var node = mapper.createObjectNode();

        // Save type info for deserialization
        node.put("type", shape.getClass().getName());

        // Optionally: include custom fields if shape has them
        JsonNode content = mapper.valueToTree(shape);
        node.set("data", content);

        gen.writeTree(node);
      }
    });

    addDeserializer(ParticleShape.class, new JsonDeserializer<>() {
      @Override
      public ParticleShape deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final var mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        // Read the stored class type
        final var type = node.get("type").asText();
        final var data = node.get("data");

        try {
          Class<?> clazz = Class.forName(type);
          if (!ParticleShape.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Invalid ParticleShape type: " + type);
          }

          // Rebuild the specific shape instance
          return (ParticleShape) mapper.treeToValue(data, clazz);
        } catch (ClassNotFoundException e) {
          throw new IOException("Unknown ParticleShape type: " + type, e);
        }
      }
    });

  }

}
