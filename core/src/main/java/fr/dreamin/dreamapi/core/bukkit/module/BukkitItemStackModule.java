package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class BukkitItemStackModule extends SimpleModule {

  public BukkitItemStackModule() {
    super ("BukkitItemStackModule");

    addSerializer(ItemStack.class, new JsonSerializer<>() {
      @Override
      public void serialize(final ItemStack value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        gen.writeObject(value.serialize());
      }
    });

    addDeserializer(ItemStack.class, new JsonDeserializer<>() {
      @Override
      public ItemStack deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        final TypeFactory typeFactory = mapper.getTypeFactory();
        final var mapType = typeFactory.constructMapType(java.util.Map.class, String.class, Object.class);

        return ItemStack.deserialize(mapper.readValue(jsonParser, mapType));
      }
    });
  }

}
