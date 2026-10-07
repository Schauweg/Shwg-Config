package dev.shwg.shwgconfig.gui.components.widget.dropdownwidget;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.OverlayWidget;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericAbstractButton;
import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.gui.deferred.overlay.WidgetOverlay;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericIcons;
import dev.shwg.shwgconfig.render.textures.GenericSprite;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DropdownWidget<T> extends GenericAbstractButton implements OverlayWidget {

    //top left corner of texture starts at x18 y20
    private static final GenericSprite ARROW_DOWN = GenericSprite.changedVanillaTexture("textures/gui/resource_packs.png", 64, 0, 32, 32, 256, 256, "transferable_list/move_down");
    private static final GenericSprite ARROW_DOWN_HIGHLIGHTED = GenericSprite.changedVanillaTexture("textures/gui/resource_packs.png", 64, 32, 32, 32, 256, 256, "transferable_list/move_down_highlighted");
    //top left corner of texture starts at x18 y5
    private static final GenericSprite ARROW_UP = GenericSprite.changedVanillaTexture("textures/gui/resource_packs.png", 96, 0, 32, 32, 256, 256, "transferable_list/move_up");
    private static final GenericSprite ARROW_UP_HIGHLIGHTED = GenericSprite.changedVanillaTexture("textures/gui/resource_packs.png", 96, 32, 32, 32, 256, 256, "transferable_list/move_up_highlighted");

    private static final int arrowTextureX = 18;
    private static final int arrowHeight = 7;
    private static final int arrowWidth = 11;

    private static final int DEFAULT_MAX_VISIBLE_ENTRIES = 5;

    private static final int DEFAULT_EDITBOX_ICON_SIZE = 16;
    private static final int EDITBOX_ICON_PADDING = 4;

    private static final Alignment.Horizontal DEFAULT_OVERLAY_ALIGNMENT = Alignment.Horizontal.LEFT;
    private static final Alignment.OverlayDirection DEFAULT_OVERLAY_DIRECTION = Alignment.OverlayDirection.BELOW;

    private final Overlay overlay;
    private final Consumer<T> applyValue;
    private @Nullable DropdownEntry<T> currentEntry;

    private final int maxVisibleEntries;

    public DropdownWidget(int x, int y, int width, int height, int iconSize,
                          Component message, List<Function<ListWidgetOverlay<?>, DropdownEntry<T>>> entryFactories,
                          T value, Consumer<T> applyValue, int overlayWidth, int maxVisibleEntries,
                          Alignment.Horizontal overlayAlignment, Alignment.OverlayDirection defaultDirection, Alignment.HorizontalIconPosition iconPosition,
                          GenericTexture button, GenericTexture buttonHighlighted, GenericTexture buttonDisabled) {
        super(x, y, width, height, message, button, buttonHighlighted, buttonDisabled);
        this.applyValue = applyValue;
        this.maxVisibleEntries = maxVisibleEntries;
        this.overlay = new Overlay(overlayWidth, overlayAlignment, defaultDirection, iconPosition, this);
        List<DropdownEntry<T>> entries = entryFactories.stream()
                .map(factory -> factory.apply(overlay))
                .collect(Collectors.toList());
        overlay.setItems(entries);

        this.currentEntry = resolveInitialEntry(entries, value);
    }

    private @Nullable DropdownEntry<T> resolveInitialEntry(List<DropdownEntry<T>> entries, @Nullable T initialValue) {
        if (entries.isEmpty()) {
            return null;
        }
        if (initialValue != null) {
            for (DropdownEntry<T> entry : entries) {
                if (Objects.equals(entry.value(), initialValue)) {
                    return entry;
                }
            }
        }
        return entries.get(0);
    }

    private void select(DropdownEntry<T> entry) {
        currentEntry = entry;
        applyValue.accept(entry.value());
        overlay.open(false);
    }

    @Override
    protected void onFocusLost() {
        overlay.open(false);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHoveredOrFocused()) {
            graphics.drawTexture(this.buttonHighlighted, this.getX(), this.getY(), this.width, this.height);
        } else if (this.isActive()) {
            graphics.drawTexture(this.button, this.getX(), this.getY(), this.width, this.height);
        } else {
            graphics.drawTexture(this.buttonDisabled, this.getX(), this.getY(), this.width, this.height);
        }
        Component label = currentEntry != null ? currentEntry.displayComponent() : getMessage();
        graphics.drawString(Minecraft.getInstance().font, label,
                getX() + 4, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);

        int arrowX = getX() + width - EDITBOX_ICON_PADDING - GenericIcons.SMALL_ARROW_GLYPH_WIDTH;
        int arrowY = getY() + (height - GenericIcons.SMALL_ARROW_GLYPH_HEIGHT) / 2;
        boolean highlighted = isHoveredOrFocused();
        if (overlay.isOpen()) {
            graphics.addDeferred(overlay);
            GenericIcons.drawArrowUp(graphics, arrowX, arrowY, highlighted);
        } else {
            GenericIcons.drawArrowDown(graphics, arrowX, arrowY, highlighted);
        }
    }

    @Override
    protected void onPress() {
        overlay.open(!overlay.isOpen());
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (keyEvent.isEscape() && overlay.isOpen()) {
            overlay.open(false);
            return true;
        }
        return super.onKeyPressed(keyEvent);
    }

    @Override
    public WidgetOverlay getOverlay() {
        return overlay;
    }

    private class Overlay extends ListWidgetOverlay<DropdownEntry<T>> {

        private final Alignment.OverlayDirection defaultDirection;

        Overlay(int width, Alignment.Horizontal alignment, Alignment.OverlayDirection defaultDirection, Alignment.HorizontalIconPosition iconPosition, GenericAbstractWidget carrier) {
            super(width, alignment, iconPosition, carrier, maxVisibleEntries);
            this.defaultDirection = defaultDirection;
        }

        @Override
        public Alignment.OverlayDirection getDefaultDirection() {
            return defaultDirection;
        }

        @Override
        protected void onEntryChosen(DropdownEntry<T> entry) {
            select(entry);
        }
    }

    public static <T> Builder<T> ofEntries(List<Function<ListWidgetOverlay<?>, DropdownEntry<T>>> factories) {
        return new Builder<>(new ArrayList<>(factories));
    }

    public static Builder<String> ofStrings(List<String> options) {
        List<Function<ListWidgetOverlay<?>, DropdownEntry<String>>> factories = options.stream()
                .<Function<ListWidgetOverlay<?>, DropdownEntry<String>>>map(s -> owner -> new StringDropdownEntry(owner, s))
                .collect(Collectors.toList());
        return new Builder<>(factories);
    }

    public static <E extends Enum<E>> Builder<E> ofEnum(Class<E> enumClass, Function<E, Component> displayFn) {
        List<Function<ListWidgetOverlay<?>, DropdownEntry<E>>> factories = new ArrayList<>();
        for (E constant : enumClass.getEnumConstants()) {
            factories.add(owner -> new EnumDropdownEntry<>(owner, constant, displayFn));
        }
        return new Builder<>(factories);
    }

    public static class Builder<T> extends GenericAbstractButton.Builder<DropdownWidget<T>, Builder<T>> {

        private final List<Function<ListWidgetOverlay<?>, DropdownEntry<T>>> entryFactories;
        private Consumer<T> applyValue = v -> {};
        private int maxVisibleEntries = DEFAULT_MAX_VISIBLE_ENTRIES;
        private int iconSize = DEFAULT_EDITBOX_ICON_SIZE;
        private int overlayWidth = -1;
        private T value = null;
        private Alignment.OverlayDirection defaultDirection = DEFAULT_OVERLAY_DIRECTION;
        private Alignment.Horizontal overlayAlignment = DEFAULT_OVERLAY_ALIGNMENT;
        private Alignment.HorizontalIconPosition iconPosition = Alignment.HorizontalIconPosition.LEFT;

        private Builder(List<Function<ListWidgetOverlay<?>, DropdownEntry<T>>> factories) {
            super(Component.empty());
            this.entryFactories = factories;
        }

        public Builder<T> applyValue(Consumer<T> applyValue) {
            this.applyValue = applyValue;
            return self();
        }

        public Builder<T> maxVisibleEntries(int maxVisibleEntries) {
            this.maxVisibleEntries = maxVisibleEntries;
            return self();
        }

        public Builder<T> iconSize(int iconSize) {
            this.iconSize = iconSize;
            return self();
        }

        public Builder<T> overlayWidth(int overlayWidth) {
            if (overlayWidth > 0) {
                this.overlayWidth = overlayWidth;
            }
            return self();
        }

        public Builder<T> value(T value) {
            this.value = value;
            return self();
        }

        public Builder<T> defaultOverlayDirection(Alignment.OverlayDirection defaultDirection) {
            this.defaultDirection = defaultDirection;
            return self();
        }

        public Builder<T> overlayAlignment(Alignment.Horizontal overlayAlignment) {
            this.overlayAlignment = overlayAlignment;
            return self();
        }

        public Builder<T> iconPosition(Alignment.HorizontalIconPosition iconPosition) {
            this.iconPosition = iconPosition;
            return self();
        }

        @Override
        public DropdownWidget<T> build() {
            if (overlayWidth < 1){
                overlayWidth = width;
            }
            return new DropdownWidget<>(x, y, width, height, iconSize, message, entryFactories, value, applyValue, overlayWidth, maxVisibleEntries, overlayAlignment, defaultDirection, iconPosition, button, buttonHighlighted, buttonDisabled);
        }
    }
}
