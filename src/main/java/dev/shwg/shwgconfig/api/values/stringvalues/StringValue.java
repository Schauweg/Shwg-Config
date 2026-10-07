package dev.shwg.shwgconfig.api.values.stringvalues;

import dev.shwg.shwgconfig.api.values.ConfigValue;

/**
 * A config value that holds a string.
 * Used for text fields.
 */
public class StringValue extends ConfigValue<String> {

    public StringValue(String defaultValue) {
        super(defaultValue);
    }

    /**
     * A String that can be used for rendering. <br>
     * Can be used when the saved value does not represent the displayed value e.g:
     * <p>{@link ItemValue} gets saved as {@code minecraft:diamond} but should be displayed as {@code Diamond}
     * @return  the string value used for rendering
     */
    public String getDisplayName() {
        return value;
    }

    /**
     * Creates a new StringValue with the given default.
     *
     * @param defaultValue the default value for this config value
     * @return returns a {@link StringValue}
     */
    public static StringValue of(String defaultValue) {
        return new StringValue(defaultValue);
    }
}
