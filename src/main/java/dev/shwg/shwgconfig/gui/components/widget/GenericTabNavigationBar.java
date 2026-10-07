package dev.shwg.shwgconfig.gui.components.widget;

import dev.shwg.shwgconfig.gui.components.tabs.TabManager;
import dev.shwg.shwgconfig.gui.components.widget.button.TabButton;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.gui.layout.GenericHorizontalLayout;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.GenericVerticalLayout;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GenericTabNavigationBar extends GenericAbstractContainerWidget {

    private final TabManager tabManager;
    private final List<TabButton> tabButtons;
    private final GenericLayout row;
    private final Alignment.Axis orientation;
    private final boolean stretchWidth;

    public GenericTabNavigationBar(int x, int y, int width, int height, TabManager tabManager, List<TabButton> tabButtons, boolean stretchWidth, Alignment.Axis orientation, CrossAlignment alignment) {
        super(x, y, width, height, Component.empty());
        this.tabManager = tabManager;
        this.tabButtons = List.copyOf(tabButtons);
        this.orientation = orientation;
        this.row = buildRow(x, y, width, height, alignment);
        this.stretchWidth = stretchWidth;
        arrangeButtons(width, height);
    }

    public Alignment.Axis getOrientation() {
        return orientation;
    }

    private GenericLayout buildRow(int x, int y, int width, int height, CrossAlignment alignment) {
        if (orientation == Alignment.Axis.HORIZONTAL) {
            GenericHorizontalLayout horizontal = new GenericHorizontalLayout(x, y, width, height, 0, 0, toVertical(alignment));
            tabButtons.forEach(horizontal::add);
            return horizontal;
        } else {
            GenericVerticalLayout vertical = new GenericVerticalLayout(x, y, width, height, 0, 0, toHorizontal(alignment));
            tabButtons.forEach(vertical::add);
            return vertical;
        }
    }

    private static Alignment.Vertical toVertical(CrossAlignment alignment) {
        return switch (alignment) {
            case START -> Alignment.Vertical.TOP;
            case CENTER -> Alignment.Vertical.CENTER;
            case END -> Alignment.Vertical.BOTTOM;
        };
    }

    private static Alignment.Horizontal toHorizontal(CrossAlignment alignment) {
        return switch (alignment) {
            case START -> Alignment.Horizontal.LEFT;
            case CENTER -> Alignment.Horizontal.CENTER;
            case END -> Alignment.Horizontal.RIGHT;
        };
    }

    /**
     * Recomputes the bar's own bounds, then re-arranges and re-centers the button row
     * within them. Call on init and on every resize.
     */
    public void arrangeButtons(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);

        if (orientation == Alignment.Axis.HORIZONTAL) {
            row.setHeight(this.getHeight());
            if (stretchWidth) {
                int buttonWidth = width / tabButtons.size();
                tabButtons.forEach(button -> button.setWidth(buttonWidth));
            }
            row.arrange();
            int centeredX = this.getX() + (this.getWidth() - row.getContentWidth()) / 2;
            row.setX(centeredX);
            row.setY(this.getY());
        } else {
            row.setWidth(this.getWidth());
            if (stretchWidth) {
                int buttonHeight = height / tabButtons.size();
                tabButtons.forEach(button -> button.setHeight(buttonHeight));
            }
            row.arrange();
            int centeredY = this.getY() + (this.getHeight() - row.getContentHeight()) / 2;
            row.setX(this.getX());
            row.setY(centeredY);
        }
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (TabButton button : tabButtons) {
            button.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void onChildFocusChanged(@Nullable GuiEventListener newFocused) {
        if (newFocused instanceof TabButton button && button.isActive()) {
            tabManager.setCurrentTab(button.tab(), true);
        }
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return tabButtons;
    }

    public void selectTab(int index, boolean playSound) {
        TabButton button = tabButtons.get(index);
        if (isFocused()) {
            setFocused(button);
        } else if (button.isActive()) {
            tabManager.setCurrentTab(button.tab(), playSound);
        }
    }

    public void setTabActiveState(int index, boolean active) {
        if (index >= 0 && index < tabButtons.size()) {
            tabButtons.get(index).active = active;
        }
    }

    private int currentTabIndex() {
        for (int i = 0; i < tabButtons.size(); i++) {
            if (tabButtons.get(i).tab() == tabManager.getCurrentTab()) {
                return i;
            }
        }
        return -1;
    }

    public int maxButtonWidth() {
        int max = 0;
        for (TabButton button : tabButtons) {
            max = Math.max(max, button.getWidth());
        }
        return max;
    }

    /**
     * Where buttons sit within the bar's cross-axis thickness - TOP/LEFT, CENTER, or BOTTOM/RIGHT, depending on {@link Alignment.Axis}.
     */
    public enum CrossAlignment {
        START, CENTER, END
    }

    public static Builder builder(TabManager tabManager) {
        return new Builder(tabManager);
    }

    public static class Builder extends GenericAbstractWidget.Builder<GenericTabNavigationBar, GenericTabNavigationBar.Builder> {
        private final TabManager tabManager;
        private final List<TabButton> tabButtons = new ArrayList<>();
        private Alignment.Axis orientation = Alignment.Axis.HORIZONTAL;
        private CrossAlignment alignment = CrossAlignment.CENTER;
        private boolean equalButtonWidth = true;
        private boolean stretchWidth = false;

        public Builder(TabManager tabManager) {
            super(Component.empty());
            this.tabManager = tabManager;
        }

        public Builder addTab(TabButton button) {
            tabButtons.add(button);
            this.height = maxButtonHeight();
            return self();
        }

        public Builder addTabs(TabButton... buttons) {
            for (TabButton button : buttons) {
                addTab(button);
            }
            return self();
        }

        public Builder addTabs(List<TabButton> buttons) {
            for (TabButton button : buttons) {
                addTab(button);
            }
            return self();
        }

        public Builder orientation(Alignment.Axis orientation) {
            this.orientation = orientation;
            return self();
        }

        public Builder alignment(CrossAlignment alignment) {
            this.alignment = alignment;
            return self();
        }

        public Builder equalButtonWidth(boolean equalButtonWidth) {
            this.equalButtonWidth = equalButtonWidth;
            return self();
        }

        public Builder stretchWidth(boolean stretchWidth) {
            this.stretchWidth = stretchWidth;
            return self();
        }

        private int maxButtonHeight() {
            int max = 0;
            for (TabButton button : tabButtons) {
                max = Math.max(max, button.getHeight());
            }
            return max;
        }

        private int maxButtonWidth() {
            int max = 0;
            for (TabButton button : tabButtons) {
                max = Math.max(max, button.getWidth());
            }
            return max;
        }

        public GenericTabNavigationBar build() {
            if (equalButtonWidth && !stretchWidth) {
                int max = maxButtonWidth();
                tabButtons.forEach(b -> b.setWidth(max));
            }
            return new GenericTabNavigationBar(x, y, width, height, tabManager, tabButtons, stretchWidth, orientation, alignment);
        }
    }
}
