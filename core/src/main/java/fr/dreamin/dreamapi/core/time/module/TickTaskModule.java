package fr.dreamin.dreamapi.core.time.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.dreamin.dreamapi.core.time.TickTask;

import java.io.IOException;

public final class TickTaskModule extends SimpleModule {

  public TickTaskModule() {
    super("TickTaskModule");

    // ------------------------------------------------------------
    // ✅ Serializer for TickTask
    // ------------------------------------------------------------
    addSerializer((Class<TickTask<?>>)(Class<?>) TickTask.class, new TickTaskSerializer());


    // ------------------------------------------------------------
    // ✅ Deserializer for TickTask
    // ------------------------------------------------------------
    addDeserializer((Class<TickTask<?>>)(Class<?>) TickTask.class, new JsonDeserializer<>() {
      @Override
      public TickTask<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);
        TickTask<?> task = new TickTask() {};

        if (node.has("current")) task.reset().limit(node.get("limit").asLong());
        if (node.has("start")) task.startAt(node.get("limit").asLong());
        if (node.has("delay")) task.delay(node.get("delay").asLong());
        if (node.has("every")) task.every(node.get("every").asLong());
        if (node.has("async")) task.async(node.get("async").asBoolean());
        if (node.has("autoStop")) task.autoStop(node.get("autoStop").asBoolean());

        // paused/running not restored automatically (runtime only)
        return task;
      }
    });
  }

  private static final class TickTaskSerializer extends JsonSerializer<TickTask<?>> {
    @Override
    public void serialize(TickTask<?> task, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      gen.writeNumberField("current", task.current());
      gen.writeNumberField("start", task.startAt());
      gen.writeNumberField("limit", task.limit());
      gen.writeNumberField("delay", task.delay());
      gen.writeNumberField("every", task.every());
      gen.writeBooleanField("paused", task.paused());
      gen.writeBooleanField("async", task.async());
      gen.writeBooleanField("running", task.running());
      gen.writeBooleanField("autoStop", task.autoStop());
      gen.writeEndObject();
    }
  }

}