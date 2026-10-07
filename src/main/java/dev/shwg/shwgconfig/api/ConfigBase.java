package dev.shwg.shwgconfig.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shwg.shwgconfig.ShwgConfig;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.api.values.CustomJsonSerializable;
import dev.shwg.shwgconfig.util.ConfigValueAdapter;

import java.lang.reflect.Field;

/**
 * Base class for a mod's config. Declare your settings as public final
 * {@link ConfigValue} fields, then implement
 * {@link #buildScreen()} to describe how they should be presented — see
 * {@link Builder}, {@link TabBuilder}, and {@link CategoryBuilder}.
 *
 * <p>Values are (de)serialized automatically via reflection over
 * {@link ConfigValue} fields — no manual
 * (de)serialization code needed. Use {@link ConfigManager} to load, save, and
 * open the resulting screen.
 */
public abstract class ConfigBase {

    /**
     * Walks every {@link ConfigValue} field on this
     * config via reflection and sets each value's name to its field name — e.g.
     * {@code enableFeature}'s {@code ConfigValue} knows it's named {@code "enableFeature"}.
     * Purely for debugging: it's what lets a failed {@code isValid(...)} check (or any
     * other log output) identify *which* value it came from, rather than just printing
     * a bare value with no context.
     *
     * <p>Runs once, automatically, when a {@code ConfigBase} is constructed — never
     * needs to be called manually. Only direct {@code ConfigValue} fields on this class
     * are named this way; a value nested inside something else (a list, a custom
     * wrapper object) won't be reached by this reflection pass.
     */
    public ConfigBase() {
        initializeConfigValues();
    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(ConfigValue.class, new ConfigValueAdapter())
            .create();

    /**
     * Describes the config screen for this config. Called once per screen open.
     *
     * @return ScreenManager of the ConfigManager used for building config Screen
     */
    public abstract ConfigManager.ScreenManager buildScreen();

    /**
     * Serializes every {@link ConfigValue} field on this config to JSON.
     *
     * @return serialized config as JsonObject
     */
    public JsonObject toJson() {
        return GSON.toJsonTree(this).getAsJsonObject();
    }

    /**
     * Loads every {@link ConfigValue} field on this config from JSON, leaving
     * fields not present in {@code json} at their current value.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void fromJson(JsonObject json) {
        // Load each field's value from JSON
        for (Field field : this.getClass().getDeclaredFields()) {
            if (ConfigValue.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    ConfigValue<?> configValue = (ConfigValue<?>) field.get(this);

                    if (json.has(field.getName())) {
                        // Get the raw value from JSON and set it
                        JsonElement valueJson = json.get(field.getName());
                        if (configValue instanceof CustomJsonSerializable custom) {
                            custom.loadFromJson(valueJson);
                        } else {
                            Object newValue = GSON.fromJson(valueJson, configValue.getValue().getClass());
                            ((ConfigValue) configValue).setValue(newValue);
                        }
                    }

                } catch (Exception e) {
                    ShwgConfig.LOGGER.error("Failed to load field: {}", field.getName());
                }
            }
        }
    }

    /**
     * Initializes all ConfigValue fields with their field names.
     * This must be called after the subclass has been constructed,
     * because Java initializes the superclass before subclass fields.
     */
    public void initializeConfigValues() {
        for (Field field : getClass().getDeclaredFields()) {
            if (!ConfigValue.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                ConfigValue<?> value = (ConfigValue<?>) field.get(this);
                if (value != null) {
                    value.setName(field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to initialize config value: " + field.getName(), e);
            }
        }
    }

}
