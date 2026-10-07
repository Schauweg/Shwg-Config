package dev.shwg.shwgconfig.api.values.numbervalues;

/**
 * A config value that holds a float with min/max bounds.
 * Used for sliders and number inputs.
 */
public class FloatValue extends NumberValue<Float> {

    public FloatValue(float defaultValue, float min, float max, float step) {
        super(defaultValue, min, max, step);
    }

    /**
     * Creates a new FloatValue with the given default, min, max and a default step of {@code 0.1}.
     */
    public static FloatValue of(float defaultValue, float min, float max) {
        return new FloatValue(defaultValue, min, max, 0.1f);
    }

    /**
     * Creates a new FloatValue with the given default, min, max and step.
     */
    public static FloatValue of(float defaultValue, float min, float max, float step) {
        return new FloatValue(defaultValue, min, max, step);
    }

    /**
     * Creates an unbounded FloatValue intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Float.MIN_VALUE} / {@code Float.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     *
     * @param defaultValue The initial value
     */
    public static FloatValue of(float defaultValue) {
        return new FloatValue(defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1f);
    }

    /**
     * Creates an unbounded FloatValue with a step, intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Float.MIN_VALUE} / {@code Float.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     *
     * @param defaultValue The initial value
     * @param step         The increment size - must be greater than zero
     */
    public static FloatValue of(float defaultValue, float step) {
        return new FloatValue(defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE, step);
    }
}
