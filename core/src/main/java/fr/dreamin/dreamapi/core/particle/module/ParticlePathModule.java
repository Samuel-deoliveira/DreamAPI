package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitLocationModule;
import fr.dreamin.dreamapi.core.particle.ParticlePath;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public final class ParticlePathModule extends SimpleModule {

  public ParticlePathModule() {
    super("ParticlePathModule");

    addSerializer(ParticlePath.class, new JsonSerializer<ParticlePath>() {
      @Override
      public void serialize(ParticlePath value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (!Configurations.containModule(BukkitLocationModule.class))
          Configurations.addModule(new BukkitLocationModule());

        if (Objects.equals(value, ParticlePath.none()))
          result.put("type", "none");
        else {
          result.put("type", "between");
          result.put("start", value.getStart());
          result.put("end", value.getEnd());
          result.put("ease", value.getEase().name());
        }

        gen.writeObject(result);
      }
    });

    addDeserializer(ParticlePath.class, new JsonDeserializer<ParticlePath>() {
      @Override
      public ParticlePath deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final var type = node.get("type").asText();

        if (!Configurations.containModule(BukkitLocationModule.class))
          Configurations.addModule(new BukkitLocationModule());

        if (type.equals("none"))
          return ParticlePath.none();

        final var start = jsonParser.getCodec().treeToValue(node.get("start"), Location.class);
        final var end = jsonParser.getCodec().treeToValue(node.get("end"), Location.class);
        final var ease = InterpolationType.valueOf(node.get("ease").asText());

        return ParticlePath.between(start, end).ease(ease);
      }
    });

  }


}
