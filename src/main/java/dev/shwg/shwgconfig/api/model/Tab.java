package dev.shwg.shwgconfig.api.model;

import dev.shwg.shwgconfig.api.TabBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A single tab of a config screen - a title (shown on its tab button) and the
 * {@link ConfigCategory}s it contains. Built via {@link TabBuilder}.
 */
public class Tab {
    private final Component title;
    private final List<ConfigCategory> categories;

    public Tab(Component title, List<ConfigCategory> categories) {
        this.title = title;
        this.categories = categories;
    }

    public Component getTitle() {
        return title;
    }

    public List<ConfigCategory> getCategories() {
        return categories;
    }
}
