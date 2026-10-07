package dev.shwg.shwgconfig.gui.components.tabs;

import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tracks the currently-selected {@link ITab} and delegates widget registration to whatever
 * owns the actual widget list (a {@code GenericScreen} or {@code GenericScrollableWidget}).
 * Deliberately slimmer than vanilla's equivalent - no onSelected/onDeselected callback pair,
 * since nothing currently needs to observe tab switches from outside.
 */
public class TabManager {

    private final Consumer<LayoutElement> addElement;
    private final Consumer<LayoutElement> removeElement;

    private @Nullable ITab currentTab;

    private boolean hasArea = false;
    private int areaX, areaY, areaWidth, areaHeight;

    public TabManager(Consumer<LayoutElement> addElement, Consumer<LayoutElement> removeElement) {
        this.addElement = addElement;
        this.removeElement = removeElement;
    }

    /**
     * Records the content area available to tabs, and immediately re-lays-out the current
     * tab within it. Call this whenever that area changes - on init and on every resize -
     * not just once, since a tab laid out for a stale size will look wrong after a resize.
     */
    public void setTabArea(int x, int y, int width, int height) {
        this.areaX = x;
        this.areaY = y;
        this.areaWidth = width;
        this.areaHeight = height;
        this.hasArea = true;

        if (currentTab != null) {
            currentTab.adjustLayout(x, y, width, height);
        }
    }

    public void setCurrentTab(ITab tab, boolean playSound) {
        setCurrentTab(tab, playSound, true);
    }

    public void setCurrentTab(ITab tab, boolean playSound, boolean addWidgets) {
        if (Objects.equals(this.currentTab, tab)) {
            return;
        }

        if (this.currentTab != null) {
            this.currentTab.visitChildren(removeElement);
        }

        this.currentTab = tab;

        if (addWidgets) {
            tab.visitChildren(addElement);
        }

        if (hasArea) {
            tab.adjustLayout(areaX, areaY, areaWidth, areaHeight);
        }

        if (playSound) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public @Nullable ITab getCurrentTab() {
        return currentTab;
    }
}
