package dev.shwg.shwgconfig.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.shwg.shwgconfig.ShwgConfig;
import dev.shwg.shwgconfig.api.model.Tab;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.gui.components.screens.ShwgConfigScreen;
import dev.shwg.shwgconfig.util.ConfigPathResolver;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Owns a {@link ConfigBase} instance's lifecycle - loading and saving it as JSON,
 * and building the config screen. Autosaves on every {@link ConfigValue}
 * change, so there's no need to call {@link #save()} manually after editing a value in code.
 *
 * <pre>{@code
 * public static final MyConfig CONFIG = new MyConfig();
 * public static final ConfigManager MANAGER = ConfigManager.of(CONFIG, "mymod.json");
 *
 * public void onInitializeClient() {
 *     MANAGER.load();
 * }
 * }</pre>
 */
public class ConfigManager {

    private final ConfigBase config;
    private final Path configFile;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a manager for {@code config}, persisted to {@code filename} in the
     * standard config directory.
     */
    public ConfigManager(ConfigBase config, String filename) {
        this.config = config;
        configFile = ConfigPathResolver.getConfigDir(filename);
        config.initializeConfigValues();
        registerSaveListeners();
    }

    /**
     * Creates a manager for {@code config}, persisted to {@code filename} in the
     * standard config directory.
     */
    public static ConfigManager of(ConfigBase config, String filename) {
        return new ConfigManager(config, filename);
    }

    /**
     * Returns the {@link ConfigBase} instance this manager owns.
     * @return config instance
     */
    public ConfigBase config() {
        return config;
    }

    private void registerSaveListeners() {
        for (Field field : config.getClass().getDeclaredFields()) {
            if (ConfigValue.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    ConfigValue<?> value = (ConfigValue<?>) field.get(config);
                    value.addListener(v -> save());
                } catch (Exception e) {
                    ShwgConfig.LOGGER.error("Failed to register save listener for config value '{}'", field.getName(), e);
                }
            }
        }
    }

    /**
     * Saves the current state of {@link #config()} to disk. Called automatically
     * whenever any {@link ConfigValue} on the config changes -
     * manual calls are only needed for something that bypasses that mechanism.
     */
    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            JsonObject json = config.toJson();
            String jsonString = GSON.toJson(json);
            Files.writeString(configFile, jsonString);
        } catch (IOException e) {
            ShwgConfig.LOGGER.error("Failed to save config to: {}", configFile, e);
        }
    }

    /**
     * Loads {@link #config()}'s state from disk, or creates the file with default
     * values if it doesn't exist yet. Call once during mod initialization.
     */
    public void load() {
        if (!Files.exists(configFile)) {
            ShwgConfig.LOGGER.info("Config file doesn't exist, creating default: {}", configFile);
            save();
            return;
        }

        try {
            String jsonString = Files.readString(configFile);
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            config.fromJson(json);

            ShwgConfig.LOGGER.info("Config loaded from: {}", configFile);
        } catch (IOException e) {
            ShwgConfig.LOGGER.error("Failed to load config from: {}", configFile, e);
        }
    }

    /**
     * Builds the config's {@link ScreenManager} by calling {@link ConfigBase#buildScreen()}.
     */
    public ScreenManager getScreenManager() {
        return config.buildScreen();
    }

    /**
     * Builds and returns a ready-to-open config screen with {@code parent} as its back/cancel target.
     * 
     * @param parent    parent screen the config screen returns to after closing it
     * @return          returns config screen
     */
    public ShwgConfigScreen getScreen(Screen parent) {
        return getScreenManager().getScreen(parent, this);
    }

    public static class ScreenManager {
        private final Component title;
        private final List<Tab> tabs;

        public ScreenManager(Component title, List<Tab> tabs) {
            this.title = title;
            this.tabs = tabs;
        }

        public ShwgConfigScreen getScreen(Screen parent, ConfigManager manager) {
            return new ShwgConfigScreen(title, tabs, parent, manager);
        }
    }
}
