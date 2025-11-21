package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.core.particle.ParticleShape;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.HashMap;

public final class ParticleOptionsModule extends SimpleModule {

  public ParticleOptionsModule() {
    super ("ParticleOptionsModule");

    addSerializer(ParticleShape.ParticleOptions.class, new JsonSerializer<>() {

      @Override
      public void serialize(ParticleShape.ParticleOptions value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (value.color() != null)
          result.put("color", value.color().asRGB());
        else
          result.put("color", null);

        result.put("size", value.size());
        result.put("offset", value.offset());
        result.put("count", value.count());

        gen.writeObject(result);
      }

    });

    addDeserializer(ParticleShape.ParticleOptions.class, new JsonDeserializer() {


      @Override
      public ParticleShape.ParticleOptions deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        Color color = null;
        if (node.hasNonNull("color"))
          color = Color.fromRGB(node.get("color").asInt());

        final var size = node.get("size").floatValue();
        final var offset = jsonParser.getCodec().treeToValue(node.get("offset"), Vector.class);
        final var count = node.get("count").asInt();

        return new ParticleShape.ParticleOptions(color, size, offset, count);
      }
    });

  }

}
