package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class BukkitLocationModule extends SimpleModule {

  public BukkitLocationModule() {
    super("BukkitLocationModule");

    addSerializer(World.class, new JsonSerializer<>() {
      @Override
      public void serialize(World world, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(world.getName());
      }
    });

    addDeserializer(World.class, new JsonDeserializer<>() {
      @Override
      public World deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Bukkit.getWorld(p.getValueAsString());
      }
    });

    addSerializer(Location.class, new JsonSerializer<>() {
      @Override
      public void serialize(Location loc, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("world", loc.getWorld().getName());
        gen.writeNumberField("x", loc.getX());
        gen.writeNumberField("y", loc.getY());
        gen.writeNumberField("z", loc.getZ());
        gen.writeNumberField("yaw", loc.getYaw());
        gen.writeNumberField("pitch", loc.getPitch());
        gen.writeEndObject();
      }
    });

    addDeserializer(Location.class, new JsonDeserializer<>() {
      @Override
      public Location deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);

        final var worldName = node.get("world").asText();
        final var world = Bukkit.getWorld(worldName);

        final var x = node.get("x").asDouble();
        final var y = node.get("y").asDouble();
        final var z = node.get("z").asDouble();
        final var yaw = node.has("yaw") ? (float) node.get("yaw").asDouble() : 0f;
        final var pitch = node.has("pitch") ? (float) node.get("pitch").asDouble() : 0f;

        return new Location(world, x, y, z, yaw, pitch);
      }
    });
  }
}
