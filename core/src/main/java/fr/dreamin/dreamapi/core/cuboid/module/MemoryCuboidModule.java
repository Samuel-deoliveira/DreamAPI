package fr.dreamin.dreamapi.core.cuboid.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.core.cuboid.MemoryCuboid;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitLocationModule;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class MemoryCuboidModule extends SimpleModule {

  public MemoryCuboidModule() {
    super ("MemoryCuboidModule");

    addSerializer(MemoryCuboid.class, new JsonSerializer<>() {
      @Override
      public void serialize(final MemoryCuboid value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        final Map<String, Object> result = new HashMap<>();

        if (!Configurations.containModule(BukkitLocationModule.class))
          Configurations.addModule(new BukkitLocationModule());

        result.put("locA", value.getLocA());
        result.put("locB", value.getLocB());

        gen.writeObject(result);
      }
    });

    addDeserializer(MemoryCuboid.class, new JsonDeserializer<>() {
      @Override
      public MemoryCuboid deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final var node = jsonParser.getCodec().readTree(jsonParser);

        if (!Configurations.containModule(BukkitLocationModule.class))
          Configurations.addModule(new BukkitLocationModule());

        final var locA = jsonParser.getCodec().treeToValue(node.get("locA"), Location.class);
        final var locB = jsonParser.getCodec().treeToValue(node.get("locB"), Location.class);

        return new MemoryCuboid(locA, locB);
      }
    });
  }

}
