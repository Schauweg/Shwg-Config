package dev.shwg.shwgconfig.api.values;

import com.google.gson.JsonElement;
import dev.shwg.shwgconfig.api.ConfigBase;

/**
 * Implemented by a {@link ConfigValue} that needs
 * custom JSON (de)serialization instead of the default reflection-based
 * serialization {@link ConfigBase} uses for ordinary fields - e.g. a value that
 * stores more than just its raw type, like {@link ColorValue}.
 */
public interface CustomJsonSerializable {

    /** Serializes this value's current state to JSON. */
    JsonElement toJsonElement();

    /** Restores this value's state from previously serialized JSON. */
    void loadFromJson(JsonElement element);
}
