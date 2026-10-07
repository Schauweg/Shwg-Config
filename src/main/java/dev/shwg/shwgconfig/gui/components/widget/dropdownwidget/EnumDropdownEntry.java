package dev.shwg.shwgconfig.gui.components.widget.dropdownwidget;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class EnumDropdownEntry<E extends Enum<E>> extends DropdownEntry<E> {

    private final E value;
    private final Function<E, Component> displayFn;

    public EnumDropdownEntry(ListWidgetOverlay<?> owner, E value, Function<E, Component> displayFn) {
        super(owner, 12);
        this.value = value;
        this.displayFn = displayFn;
    }

    public E value() {
        return value;
    }

    @Override public Component displayComponent() { return displayFn.apply(value); }
}
