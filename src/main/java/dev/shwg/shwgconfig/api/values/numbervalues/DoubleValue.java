package dev.shwg.shwgconfig.api.values.numbervalues;

/**
 * A config value that holds a double with min/max bounds.
 * Used for sliders and number inputs.
 */
public class DoubleValue extends NumberValue<Double> {

    public DoubleValue(double defaultValue, double min, double max, double step) {
        super(defaultValue, min, max, step);
    }

    /**
     * Creates a new DoubleValue with the given default, min, max and a default step of {@code 0.1}.
     */
    public static DoubleValue of(double defaultValue, double min, double max) {
        return new DoubleValue(defaultValue, min, max, 0.1);
    }

    /**
     * Creates a new DoubleValue with the given default, min, max and step.
     */
    public static DoubleValue of(double defaultValue, double min, double max, double step) {
        return new DoubleValue(defaultValue, min, max, step);
    }

    /**
     * Creates an unbounded DoubleValue intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Double.MIN_VALUE} / {@code Double.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     *
     * @param defaultValue The initial value
     */
    public static DoubleValue of(double defaultValue) {
        return new DoubleValue(defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
    }

    /**
     * Creates an unbounded DoubleValue with a step, intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Double.MIN_VALUE} / {@code Double.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     *
     * @param defaultValue The initial value
     * @param step         The increment size - must be greater than zero
     */
    public static DoubleValue of(double defaultValue, double step) {
        return new DoubleValue(defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE, step);
    }
}
