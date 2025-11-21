package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.util.Map;

public class BukkitPotionEffectModule extends SimpleModule {

  public BukkitPotionEffectModule() {
    super("BukkitPotionEffectModule");

    addSerializer(PotionEffect.class, new JsonSerializer<>() {
      @Override
      public void serialize(final PotionEffect value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeObject(value.serialize());
      }
    });

    addDeserializer(PotionEffect.class, new JsonDeserializer<>() {
      @Override
      public PotionEffect deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final var mapper = (ObjectMapper) jsonParser.getCodec();
        final var typeFactory = mapper.getTypeFactory();
        final var mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);

        return new PotionEffect(mapper.readValue(jsonParser, mapType));
      }
    });

  }

}
