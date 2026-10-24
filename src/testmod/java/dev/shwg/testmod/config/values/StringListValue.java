package dev.shwg.testmod.config.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.shwg.shwgconfig.api.values.ListValue;

import java.util.ArrayList;
import java.util.List;

/** A list of strings, saved as a plain JSON array. */
public class StringListValue extends ListValue<String> {

    public StringListValue(List<String> defaults) {
        super(defaults);
    }

    @Override
    public JsonElement toJsonElement() {
        JsonArray array = new JsonArray();
        for (String entry : value) array.add(entry);
        return array;
    }

    @Override
    public void loadFromJson(JsonElement element) {
        List<String> entries = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) entries.add(entry.getAsString());
        setValue(entries);
    }
}
