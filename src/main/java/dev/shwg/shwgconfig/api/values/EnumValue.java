package dev.shwg.shwgconfig.api.values;

/**
 * A config value that holds an enum value.
 * Used for cycle buttons to switch between predefined options.
 *
 * @param <E> The enum type
 */
public class EnumValue<E extends Enum<E>> extends ConfigValue<E> {
    private final Class<E> enumClass;
    private final E[] enumConstants;

    public EnumValue(E defaultValue) {
        super(defaultValue);
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>) defaultValue.getClass();
        this.enumClass = clazz;
        this.enumConstants = enumClass.getEnumConstants();
    }

    /**
     * Creates a new EnumValue with the given default.
     */
    public static <E extends Enum<E>> EnumValue<E> of(E defaultValue) {
        return new EnumValue<>(defaultValue);
    }

    /**
     * Cycles to the next enum value.
     * Wraps around to the first value after the last.
     */
    public void cycle() {
        int currentIndex = value.ordinal();
        int nextIndex = (currentIndex + 1) % enumConstants.length;
        setValue(enumConstants[nextIndex]);
    }

    /**
     * Cycles to the previous enum value.
     * Wraps around to the last value before the first.
     */
    public void cyclePrevious() {
        int currentIndex = value.ordinal();
        int previousIndex = (currentIndex - 1 + enumConstants.length) % enumConstants.length;
        setValue(enumConstants[previousIndex]);
    }

    /**
     * Gets all possible values for this enum.
     */
    public E[] getValues() {
        return enumConstants;
    }

    /**
     * Gets the enum class.
     */
    public Class<E> getEnumClass() {
        return enumClass;
    }
}
