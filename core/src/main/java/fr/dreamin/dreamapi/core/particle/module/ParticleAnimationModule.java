package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import fr.dreamin.dreamapi.core.particle.ParticleAnimation;
import fr.dreamin.dreamapi.core.particle.ParticlePath;
import fr.dreamin.dreamapi.core.particle.ParticleShape;
import org.bukkit.Particle;

import java.io.IOException;
import java.util.HashMap;

public final class ParticleAnimationModule extends SimpleModule {

  public ParticleAnimationModule() {
    super("ParticleAnimationModule");

    addSerializer(ParticleAnimation.class, new JsonSerializer<>() {

      @Override
      public void serialize(final ParticleAnimation value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (!Configurations.containModule(ParticleShapeModule.class))
          Configurations.addModule(new ParticleShapeModule());

        if (!Configurations.containModule(ParticlePathModule.class))
          Configurations.addModule(new ParticlePathModule());

        if (!Configurations.containModule(ParticleOptionsModule.class))
          Configurations.addModule(new ParticleOptionsModule());

        result.put("type", "ParticleAnimation");
        result.put("particle", value.particle().name());
        result.put("shape", value.shape());
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

    addDeserializer(ParticleAnimation.class, new JsonDeserializer<>() {
      @Override
      public ParticleAnimation deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (!Configurations.containModule(ParticleShapeModule.class))
          Configurations.addModule(new ParticleShapeModule());

        if (!Configurations.containModule(ParticlePathModule.class))
          Configurations.addModule(new ParticlePathModule());

        if (!Configurations.containModule(ParticleOptionsModule.class))
          Configurations.addModule(new ParticleOptionsModule());

        final var particle = Particle.valueOf(node.get("particle").asText());
        final var shape = jsonParser.getCodec().treeToValue(node.get("shape"), ParticleShape.class);
        final var path = jsonParser.getCodec().treeToValue(node.get("path"), ParticlePath.class);
        final var duration = node.get("duration").asInt();
        final var speed = node.get("speed").asDouble();
        final var loop = node.get("loop").asBoolean();
        final var reverse = node.get("reverse").asBoolean();
        final var progressiveDraw = node.get("progressiveDraw").asBoolean();
        InterpolationType interpolationType = InterpolationType.valueOf(node.get("interpolationType").asText());
        ParticleShape.ParticleOptions options = jsonParser.getCodec().treeToValue(node.get("options"), ParticleShape.ParticleOptions.class);

        return ParticleAnimation.create()
          .particle(particle)
          .shape(shape)
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