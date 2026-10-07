package dev.shwg.shwgconfig.util;

import com.google.gson.*;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.api.values.CustomJsonSerializable;

import java.lang.reflect.Type;

/**
 * Gson adapter that serializes ConfigValue to just its value, not the whole object.
 */
public class ConfigValueAdapter implements JsonSerializer<ConfigValue<?>>, JsonDeserializer<ConfigValue<?>> {

    @Override
    public JsonElement serialize(ConfigValue<?> configValue, Type type, JsonSerializationContext context) {
        //check if custom serializer exists and uses that instead e.g. ColorValue
        if (configValue instanceof CustomJsonSerializable custom) {
            return custom.toJsonElement();
        }
        // Only serialize the value, not the whole ConfigValue object
        return context.serialize(configValue.getValue());
    }

    @Override
    public ConfigValue<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        // We can't deserialize back to ConfigValue here - we'll handle it differently
        // This is just for reading, the actual ConfigValue object already exists
        throw new UnsupportedOperationException("Use fromJson on ConfigBase instead");
    }
}
