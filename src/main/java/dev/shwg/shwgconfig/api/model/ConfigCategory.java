package dev.shwg.shwgconfig.api.model;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.api.TabBuilder;
import dev.shwg.shwgconfig.api.entries.CategoryEntry;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A group of entries shown together under an optional title, built via
 * {@link CategoryBuilder}. Add one or more to a {@link Tab} via {@link TabBuilder}.
 */
public class ConfigCategory {

    @Nullable
    private final Component title;
    private final List<CategoryEntry> entries;
    @Nullable
    private final RenderableElement previewWidget;

    private final boolean centerPreviewWithControls;
    private final boolean useOwnControllerWidth;

    public ConfigCategory(@Nullable Component title, List<CategoryEntry> entries, @Nullable RenderableElement previewWidget, boolean centerPreviewWithControls, boolean useOwnControllerWidth) {
        this.title = title;
        this.entries = entries;
        this.previewWidget = previewWidget;
        this.centerPreviewWithControls = centerPreviewWithControls;
        this.useOwnControllerWidth = useOwnControllerWidth;
    }

    @Nullable
    public Component getTitle() {
        return title;
    }

    public List<CategoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Nullable
    public RenderableElement getPreviewWidget() {
        return previewWidget;
    }

    public boolean centerPreviewWithControls() {
        return centerPreviewWithControls;
    }

    public boolean useOwnControllerWidth() {
        return useOwnControllerWidth;
    }

}
