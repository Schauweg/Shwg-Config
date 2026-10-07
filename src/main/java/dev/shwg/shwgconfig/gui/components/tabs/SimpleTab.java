package dev.shwg.shwgconfig.gui.components.tabs;

import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import net.minecraft.network.chat.Component;

/**
 * A plain {@link ITab} for the common case: a title and a pre-built {@link GenericLayout},
 * with no custom behavior needed. Covers most config-screen tabs without requiring a
 * dedicated class per tab (unlike vanilla's per-screen inner classes).
 */
public class SimpleTab implements ITab {

    private final Component title;
    private final Component extraNarration;
    private final GenericLayout layout;

    public SimpleTab(Component title, GenericLayout layout) {
        this(title, Component.empty(), layout);
    }

    public SimpleTab(Component title, Component extraNarration, GenericLayout layout) {
        this.title = title;
        this.extraNarration = extraNarration;
        this.layout = layout;
    }

    @Override
    public Component getTabTitle() {
        return title;
    }

    @Override
    public Component getTabExtraNarration() {
        return extraNarration;
    }

    @Override
    public GenericLayout getLayout() {
        return layout;
    }
}
