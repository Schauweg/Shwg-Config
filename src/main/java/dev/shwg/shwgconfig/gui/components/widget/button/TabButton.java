package dev.shwg.shwgconfig.gui.components.widget.button;

import dev.shwg.shwgconfig.gui.components.tabs.ITab;
import dev.shwg.shwgconfig.gui.components.tabs.TabManager;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

public class TabButton extends GenericAbstractButton {

    public static final NineSlicedSprite TAB = NineSlicedSprite.fromMcMeta("shwgconfig", "widget/tab");
    public static final NineSlicedSprite TAB_HIGHLIGHTED = NineSlicedSprite.fromMcMeta("shwgconfig", "widget/tab_highlighted");
    public static final NineSlicedSprite TAB_SELECTED = NineSlicedSprite.fromMcMeta("shwgconfig", "widget/tab_selected");
    public static final NineSlicedSprite TAB_SELECTED_HIGHLIGHTED = NineSlicedSprite.fromMcMeta("shwgconfig", "widget/tab_selected_highlighted");

    private static final int UNDERLINE_HEIGHT = 1;
    private static final int UNDERLINE_MARGIN_X = 3;
    private static final int UNDERLINE_MARGIN_BOTTOM = 1;

    private final TabManager tabManager;
    private final ITab tab;

    private final GenericTexture tabTexture;
    private final GenericTexture tabHighlighted;
    private final GenericTexture tabSelected;
    private final GenericTexture tabSelectedHighlighted;

    public TabButton(TabManager tabManager, ITab tab, int x, int y, int width, int height,
                     GenericTexture tabTexture, GenericTexture tabHighlighted,
                     GenericTexture tabSelected, GenericTexture tabSelectedHighlighted) {
        super(x, y, width, height, tab.getTabTitle());
        this.tabManager = tabManager;
        this.tab = tab;
        this.tabTexture = tabTexture;
        this.tabHighlighted = tabHighlighted;
        this.tabSelected = tabSelected;
        this.tabSelectedHighlighted = tabSelectedHighlighted;
    }

    public ITab tab() {
        return tab;
    }

    public boolean isSelected() {
        return tabManager.getCurrentTab() == tab;
    }

    @Override
    protected void onPress() {
        if (this.isActive()) {
            tabManager.setCurrentTab(tab, true);
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // no-op - TabManager.setCurrentTab is the single source of the selection sound,
        // fired only when the tab actually changes. Avoids a double-sound on keyboard selection.
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawTexture(currentButtonTexture(), getX(), getY(), getWidth(), getHeight());

        Font font = Minecraft.getInstance().font;
        int color = this.active ? 0xFFFFFFFF : 0xFF808080;
        int availableWidth = getWidth() - UNDERLINE_MARGIN_X * 2;

        if (font.width(getMessage()) <= availableWidth) {
            graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
        } else {
            boolean scrollActive = isHoveredOrFocused() || isSelected();
            graphics.drawScrollingString(font, getMessage(), getX() + UNDERLINE_MARGIN_X, getY() + (getHeight() - 8) / 2, availableWidth, scrollActive, color);
        }

        if (isSelected()) {
            renderUnderline(graphics, font, color);
        }
        if (this.isHovered) {
            renderDeferredTooltip(graphics, mouseX, mouseY);
            graphics.requestCursor(GenericCursors.POINTING_HAND);
        }
    }

    @Override
    protected GenericTexture currentButtonTexture() {
        boolean highlighted = this.isHoveredOrFocused();
        if (isSelected()) {
            return highlighted ? tabSelectedHighlighted : tabSelected;
        }
        return highlighted ? tabHighlighted : tabTexture;
    }

    private void renderUnderline(GenericGraphics graphics, Font font, int color) {
        int lineWidth = Math.min(font.width(getMessage()), getWidth() - UNDERLINE_MARGIN_X * 2);
        int lineX = getX() + (getWidth() - lineWidth) / 2;
        int lineY = getY() + getHeight() / 2 + 4 + UNDERLINE_MARGIN_BOTTOM;
        graphics.fill(lineX, lineY, lineX + lineWidth, lineY + UNDERLINE_HEIGHT, color);
    }

    public static Builder builder(TabManager tabManager, ITab tab) {
        return new Builder(tabManager, tab);
    }

    public static class Builder extends GenericAbstractWidget.Builder<TabButton, Builder> {

        private final TabManager tabManager;
        private final ITab tab;

        private GenericTexture tabTexture = TAB;
        private GenericTexture tabHighlighted = TAB_HIGHLIGHTED;
        private GenericTexture tabSelected = TAB_SELECTED;
        private GenericTexture tabSelectedHighlighted = TAB_SELECTED_HIGHLIGHTED;
        private int padding = 2;

        public Builder(TabManager tabManager, ITab tab) {
            super(tab.getTabTitle());
            this.tabManager = tabManager;
            this.tab = tab;
            this.width = Minecraft.getInstance().font.width(message) + 2 * padding;
            this.height = 14;
        }

        public Builder textures(GenericTexture tabTexture, GenericTexture tabHighlighted, GenericTexture tabSelected, GenericTexture tabSelectedHighlighted) {
            this.tabTexture = tabTexture;
            this.tabHighlighted = tabHighlighted;
            this.tabSelected = tabSelected;
            this.tabSelectedHighlighted = tabSelectedHighlighted;
            return self();
        }

        public Builder padding(int padding) {
            this.padding = padding;
            this.width = Minecraft.getInstance().font.width(message) + 2 * padding;
            return self();
        }

        @Override
        public TabButton build() {
            return new TabButton(tabManager, tab, x, y, width, height,
                    tabTexture, tabHighlighted, tabSelected, tabSelectedHighlighted);
        }
    }
}
