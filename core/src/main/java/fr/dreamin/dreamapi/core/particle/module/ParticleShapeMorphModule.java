package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import fr.dreamin.dreamapi.core.particle.ParticlePath;
import fr.dreamin.dreamapi.core.particle.ParticleShape;
import fr.dreamin.dreamapi.core.particle.ParticleShapeMorph;
import org.bukkit.Particle;

import java.io.IOException;
import java.util.HashMap;

public final class ParticleShapeMorphModule extends SimpleModule {

  public ParticleShapeMorphModule() {
    super("ParticleShapeMorphModule");

    addSerializer(ParticleShapeMorph.class, new JsonSerializer<ParticleShapeMorph>() {
      @Override
      public void serialize(ParticleShapeMorph value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (!Configurations.containModule(ParticleShapeModule.class))
          Configurations.addModule(new ParticleShapeModule());

        if (!Configurations.containModule(ParticlePathModule.class))
          Configurations.addModule(new ParticlePathModule());

        if (!Configurations.containModule(ParticleOptionsModule.class))
          Configurations.addModule(new ParticleOptionsModule());

        result.put("type", "ParticleShapeMorph");
        result.put("particle", value.particle().name());
        result.put("fromShape", value.fromShape());
        result.put("toShape", value.toShape());
        result.put("path", value.path());
        result.put("duration", value.duration());
        result.put("speed", value.speed());
        result.put("loop", value.loop());
        result.put("reverse", value.reverse());
        result.put("progressiveDraw", value.progressiveDraw());
        result.put("interpolationType", value.interpolationType().name());
        result.put("options", value.options());

        gen.writeObject(result);
      }
    });

    addDeserializer(ParticleShapeMorph.class, new JsonDeserializer<ParticleShapeMorph>() {
      @Override
      public ParticleShapeMorph deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (!Configurations.containModule(ParticleShapeModule.class))
          Configurations.addModule(new ParticleShapeModule());

        if (!Configurations.containModule(ParticlePathModule.class))
          Configurations.addModule(new ParticlePathModule());

        if (!Configurations.containModule(ParticleOptionsModule.class))
          Configurations.addModule(new ParticleOptionsModule());

        final var particle = Particle.valueOf(node.get("particle").asText());
        final var fromShape = jsonParser.getCodec().treeToValue(node.get("fromShape"), ParticleShape.class);
        final var toShape = jsonParser.getCodec().treeToValue(node.get("toShape"), ParticleShape.class);
        final var path = jsonParser.getCodec().treeToValue(node.get("path"), ParticlePath.class);
        final var duration = node.get("duration").asInt();
        final var speed = node.get("speed").asDouble();
        final var loop = node.get("loop").asBoolean();
        final var reverse = node.get("reverse").asBoolean();
        final var progressiveDraw = node.get("progressiveDraw").asBoolean();
        final var interpolationType = InterpolationType.valueOf(node.get("interpolationType").asText());
        final var options = jsonParser.getCodec().treeToValue(node.get("options"), ParticleShape.ParticleOptions.class);

        return ParticleShapeMorph.create()
          .particle(particle)
          .fromShape(fromShape)
          .toShape(toShape)
          .path(path)
          .duration(duration)
          .speed(speed)
          .loop(loop)
          .reverse(reverse)
          .progressiveDraw(progressiveDraw)
          .interpolation(interpolationType)
          .options(options)
          .build();

      }
    });

  }

}
