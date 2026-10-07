package dev.shwg.shwgconfig.api;

import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.model.Tab;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Root builder for a config screen. Start here - add one or more {@link Tab}s
 * (built via {@link TabBuilder}), then call {@link #build()} to get the
 * {@link ConfigManager.ScreenManager} your {@link ConfigBase#buildScreen()} needs to return.
 *
 * <pre>{@code
 * Builder.create(Component.literal("My Mod Configs"))
 *     .addTab(myTab)
 *     .build();
 * }</pre>
 */
public class Builder {

    private final Component title;
    private final List<Tab> tabs = new ArrayList<>();

    private Builder(Component title) {
        this.title = title;
    }


    /**
     * Starts building a screen with the given title.
     */
    public static Builder create(Component title) {
        return new Builder(title);
    }

    /**
     * Convenience for a screen that only has one tab's worth of content - wraps the
     * given category in a single internal tab (reusing the screen's own title) and
     * skips the tab-navigation bar entirely.
     */
    public Builder addSingleTabCategory(ConfigCategory category) {
        return addTab(TabBuilder.create(title).addCategory(category).build());
    }

    /**
     * Adds a tab, built via {@link TabBuilder}, to the screen.
     */
    public Builder addTab(Tab tab) {
        this.tabs.add(tab);
        return this;
    }

    /**
     * Finishes building and returns the {@link ConfigManager.ScreenManager} to
     * return from {@link ConfigBase#buildScreen()}.
     */
    public ConfigManager.ScreenManager build() {
        return new ConfigManager.ScreenManager(title, tabs);
    }

}
