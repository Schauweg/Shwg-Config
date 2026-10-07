package dev.shwg.shwgconfig.api.values.numbervalues;

/**
 * A config value that holds an integer with min/max bounds.
 * Used for sliders and number inputs.
 */
public class IntValue extends NumberValue<Integer> {
    /**
     * Creates an IntValue with bounds and step.
     *
     * @param defaultValue The default value
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     */
    public IntValue(int defaultValue, int min, int max, int step) {
        super(defaultValue, min, max, step);
    }

    /**
     * Creates a new IntValue with the given default, min, and max.
     */
    public static IntValue of(int defaultValue, int min, int max) {
        return new IntValue(defaultValue, min, max, 1);
    }

    /**
     * Creates a new IntValue with the given default, min, and max.
     */
    public static IntValue of(int defaultValue, int min, int max, int step) {
        return new IntValue(defaultValue, min, max, step);
    }

    /**
     * Creates an unbounded IntValue intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Integer.MIN_VALUE} / {@code Integer.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     *
     * @param defaultValue The initial value
     */
    public static IntValue of(int defaultValue) {
        return new IntValue(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
    }

    /**
     * Creates an unbounded IntValue with a step, intended for use with a number input field.
     * <p>
     * Do NOT use with a slider - {@code Integer.MIN_VALUE} / {@code Integer.MAX_VALUE}
     * bounds are meaningless for range-based controllers.
     * <p>
     * The step controls how much the value increments/decrements per input action,
     * and restricts valid values to multiples of {@code step} offset from {@code min}.
     *
     * @param defaultValue The initial value
     * @param step         The increment size - must be greater than zero
     */
    public static IntValue of(int defaultValue, int step) {
        return new IntValue(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, step);
    }
}
