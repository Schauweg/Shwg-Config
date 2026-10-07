package dev.shwg.shwgconfig.api.values;

import dev.shwg.shwgconfig.ShwgConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for all configuration values.
 * Handles value storage, change notifications, and serialization.
 *
 * <p>
 * Every ConfigValue subclass can be created using a Constructor: <br>
 * {@code public final ConfigValue<T> value = new ConfigValue(defaultValue);}
 * </p>
 * <p>
 * But every ConfigValue directly provided by this library
 * has some sort of static constructor method: <br>
 * e.g.: {@code public final ItemValue itemValue = ItemValue.of(Items.Diamond);}
 * </p>
 *
 * @param <T> The type of value this config holds
 */
public abstract class ConfigValue<T> {
    protected T value;
    protected final T defaultValue;
    protected final List<Consumer<T>> changeListeners;

    private String name;

    /**
     * Creates a new config value with the given default.
     *
     * @param defaultValue The default value (also the initial value)
     */
    public ConfigValue(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.changeListeners = new ArrayList<>();
    }

    /**
     * Gets the current value.
     *
     * @return returns the currently set value
     */
    public T getValue() {
        return value;
    }


    /**
     * Sets the value and notifies all listeners.
     *
     * @param newValue The new value
     */
    public void setValue(T newValue) {
        if (!isValid(newValue)) {
            ShwgConfig.LOGGER.warn( "Config value '{}' tried to set an invalid value {}", name, newValue );
            return;
        }

        T oldValue = this.value;
        this.value = newValue;

        if (!oldValue.equals(newValue)) {
            notifyListeners(newValue);
        }
    }

    /**
     * Gets the default value.
     *
     * @return returns the default value
     */
    public T getDefault() {
        return defaultValue;
    }

    /**
     * Resets the value to its default.
     */
    public void reset() {
        setValue(defaultValue);
    }

    /**
     * Adds a listener that will be called when the value changes.
     *
     * @param listener The listener to add
     */
    public void addListener(Consumer<T> listener) {
        changeListeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     *
     * @param listener The listener to remove
     */
    public void removeListener(Consumer<T> listener) {
        changeListeners.remove(listener);
    }

    /**
     * Validates that a value is acceptable.
     * Override this to add custom validation logic.
     *
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    protected boolean isValid(T value) {
        return value != null;
    }

    /**
     * Sets the name of this config value. <br>
     * Intended to be called by ConfigBase during initialization.
     *
     * @param name name of the variable
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this config value variable.
     *
     * @return name of the variable
     */
    public String getName() {
        return name;
    }

    /**
     * Notifies all listeners of a value change.
     *
     * @param newValue the new value all listeners will receive
     */
    protected void notifyListeners(T newValue) {
        for (Consumer<T> listener : changeListeners) {
            listener.accept(newValue);
        }
    }

}
