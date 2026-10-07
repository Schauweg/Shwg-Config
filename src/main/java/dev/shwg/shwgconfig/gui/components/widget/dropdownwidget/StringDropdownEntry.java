package dev.shwg.shwgconfig.gui.components.widget.dropdownwidget;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.gui.deferred.overlay.ScrollableWidgetOverlay;
import net.minecraft.network.chat.Component;

public class StringDropdownEntry extends DropdownEntry<String> {

    private final String text;

    public StringDropdownEntry(ListWidgetOverlay<?> owner, String text) {
        super(owner, 12);
        this.text = text;
    }

    @Override
    public String value() {
        return text;
    }

    @Override public Component displayComponent() { return Component.literal(text); }

}
