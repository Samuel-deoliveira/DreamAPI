package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.core.particle.ParticleSequenceItem;
import fr.dreamin.dreamapi.core.time.TickTask;
import fr.dreamin.dreamapi.core.time.module.TickTaskModule;

import java.io.IOException;
import java.util.HashMap;

public final class ParticleSequenceItemModule extends SimpleModule {

  public ParticleSequenceItemModule() {
    super ("ParticleSequenceItemModule");

    addSerializer(ParticleSequenceItem.class, new JsonSerializer<ParticleSequenceItem>() {
      @Override
      public void serialize(ParticleSequenceItem value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (!Configurations.containModule(TickTaskModule.class))
          Configurations.addModule(new TickTaskModule());

        result.put("type", "ParticleSequenceItem");
        result.put("task", value.task());
        result.put("delayBefore", value.delayBefore());
        result.put("delayAfter", value.delayAfter());

        gen.writeObject(result);
      }
    });

    addDeserializer(ParticleSequenceItem.class, new JsonDeserializer<ParticleSequenceItem>() {
      @Override
      public ParticleSequenceItem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (!Configurations.containModule(TickTaskModule.class))
          Configurations.addModule(new TickTaskModule());

        final var task = jsonParser.getCodec().treeToValue(node.get("task"), TickTask.class);
        final var delayBefore = node.get("delayBefore").asInt();
        final var delayAfter = node.get("delayAfter").asInt();

        return ParticleSequenceItem.of(task, delayBefore, delayAfter);
      }
    });

  }

}
