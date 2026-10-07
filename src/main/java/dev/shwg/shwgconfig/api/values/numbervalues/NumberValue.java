package dev.shwg.shwgconfig.api.values.numbervalues;

import dev.shwg.shwgconfig.api.values.ConfigValue;

/**
 * Base class for a bounded numeric {@link ConfigValue} —
 * a value, minimum, maximum, and step. Values outside {@code [min, max]} are rejected
 * by {@link #isValid(Number)}. See {@link IntValue},
 * {@link FloatValue}, and
 * {@link DoubleValue} for the concrete types.
 *
 * @param <T> the numeric type held
 */
public abstract class NumberValue<T extends Number> extends ConfigValue<T> {

    private final T min;
    private final T max;
    private final T step;

    protected NumberValue(T defaultValue, T min, T max, T step) {
        super(defaultValue);

        if (min.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException("Min cannot be greater than max");
        }
        if (defaultValue.doubleValue() < min.doubleValue() || defaultValue.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException("Default value must be within min/max bounds");
        }
        if (step.doubleValue() <= 0) {
            throw new IllegalArgumentException("Step must be greater than 0");
        }

        this.min = min;
        this.max = max;
        this.step = step;
    }

    /** The minimum allowed value, inclusive. */
    public T getMin() { return min; }

    /** The maximum allowed value, inclusive. */
    public T getMax() { return max; }

    /** The increment size used by sliders and number input fields for this value. */
    public T getStep() { return step; }

    @Override
    public boolean isValid(T value) {
        if (value == null) return false;
        return value.doubleValue() >= min.doubleValue()
                && value.doubleValue() <= max.doubleValue();
    }
}
