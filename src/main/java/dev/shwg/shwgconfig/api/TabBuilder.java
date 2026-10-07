package dev.shwg.shwgconfig.api;

import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.model.Tab;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a single {@link Tab} out of one or more {@link ConfigCategory}s, built via
 * {@link CategoryBuilder}. Hand the finished {@link Tab} to {@link Builder#addTab(Tab)}.
 *
 * <pre>{@code
 * TabBuilder.create(Component.literal("General"))
 *     .addCategory(myCategory)
 *     .build();
 * }</pre>
 */
public class TabBuilder {

    private final Component title;
    private final List<ConfigCategory> categories = new ArrayList<>();

    private TabBuilder(Component title) {
        this.title = title;
    }

    /**
     * Starts building a tab with the given title, shown on its tab button.
     */
    public static TabBuilder create(Component title) {
        return new TabBuilder(title);
    }

    /**
     * Adds a category, built via {@link CategoryBuilder}, to this tab.
     */
    public TabBuilder addCategory(ConfigCategory category) {
        this.categories.add(category);
        return this;
    }

    /**
     * Finishes building and returns the {@link Tab}.
     */
    public Tab build() {
        return new Tab(title, categories);
    }
}
