package dev.shwg.shwgconfig.api.values;

/**
 * A config value that holds a boolean (true/false).
 */
public class BooleanValue extends ConfigValue<Boolean> {

    public BooleanValue(boolean defaultValue) {
        super(defaultValue);
    }

    /**
     * Creates a new BooleanValue with the given default.
     */
    public static BooleanValue of(boolean defaultValue) {
        return new BooleanValue(defaultValue);
    }

    @Override
    public void setValue(Boolean newValue) {
        super.setValue(newValue);
    }
}
