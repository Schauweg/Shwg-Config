package dev.shwg.shwgconfig.api.values;

import dev.shwg.shwgconfig.ShwgConfig;

import java.util.List;
import java.util.Objects;

/**
 * A config value holding an ordered list of elements, edited in the config screen
 * with a list controller (one row per element).
 *
 * <p>The stored list is immutable. To change it, build a new list and pass it to
 * {@link #setValue(List)} - changing a list in place would never be saved.</p>
 *
 * <p>Subclasses decide how the list is written to the config file:</p>
 *
 * <pre>{@code
 * public class StringListValue extends ListValue<String> {
 *
 *     public StringListValue(List<String> defaults) {
 *         super(defaults);
 *     }
 *
 *     @Override
 *     public JsonElement toJsonElement() {
 *         JsonArray array = new JsonArray();
 *         for (String entry : value) array.add(entry);
 *         return array;
 *     }
 *
 *     @Override
 *     public void loadFromJson(JsonElement element) {
 *         List<String> entries = new ArrayList<>();
 *         for (JsonElement entry : element.getAsJsonArray()) entries.add(entry.getAsString());
 *         setValue(entries);
 *     }
 * }
 * }</pre>
 *
 * @param <E> the element type, ideally immutable (e.g. a String or a record)
 */
public abstract class ListValue<E> extends ConfigValue<List<E>> implements CustomJsonSerializable {

    /**
     * @param defaults the default elements in order; copied, so changing the passed list later
     *                 has no effect. Must not contain {@code null}.
     */
    protected ListValue(List<E> defaults) {
        super(List.copyOf(defaults));
    }

    /**
     * Returns the current elements in order. The returned list is immutable - see the
     * class description for how to change it.
     */
    @Override
    public List<E> getValue() {
        return value;
    }

    /**
     * Replaces the whole list with a copy of {@code newValue}. Rejected (with a warning,
     * like every other invalid value) if {@code newValue} is {@code null} or contains
     * {@code null}.
     */
    @Override
    public void setValue(List<E> newValue) {
        if (newValue == null || newValue.stream().anyMatch(Objects::isNull)) {
            ShwgConfig.LOGGER.warn("Config value '{}' tried to set a list that is null or contains null", getName());
            return;
        }
        super.setValue(List.copyOf(newValue));
    }

    /** Number of elements in the list. */
    public int size() {
        return value.size();
    }

    /** Whether the list has no elements. */
    public boolean isEmpty() {
        return value.isEmpty();
    }
}
