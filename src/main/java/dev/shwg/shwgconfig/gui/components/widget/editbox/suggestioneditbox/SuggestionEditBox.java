package dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox;

import dev.shwg.shwgconfig.gui.components.widget.editbox.GenericAbstractEditBox;
import dev.shwg.shwgconfig.gui.components.widget.OverlayWidget;
import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.gui.deferred.overlay.WidgetOverlay;
import dev.shwg.shwgconfig.gui.input.LastInput;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.shwgconfig.util.SuggestionSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SuggestionEditBox extends GenericAbstractEditBox implements OverlayWidget {

    public static final NineSlicedSprite EDITBOX = NineSlicedSprite.fromMcMeta("minecraft", "widget/text_field");
    public static final NineSlicedSprite EDITBOX_HIGHLIGHTED = NineSlicedSprite.fromMcMeta("minecraft", "widget/text_field_highlighted");

    private static final int DEFAULT_MAX_VISIBLE_ENTRIES = 5;

    private static final int DEFAULT_EDITBOX_ICON_SIZE = 16;
    private static final int EDITBOX_ICON_PADDING = 4;

    private static final Alignment.Horizontal DEFAULT_OVERLAY_ALIGNMENT = Alignment.Horizontal.RIGHT;
    private static final Alignment.OverlayDirection DEFAULT_OVERLAY_DIRECTION = Alignment.OverlayDirection.ABOVE;
    private static final Alignment.HorizontalIconPosition DEFAULT_ICON_POSITION = Alignment.HorizontalIconPosition.LEFT;

    public static CompletableFuture<List<SuggestionEntry>> getStringSuggestions (String query, List<String> strings, ListWidgetOverlay<? extends SuggestionEntry> overlay) {
        List<SuggestionEntry> results = new ArrayList<>();
        for (String candidate : strings) {
            results.add(new StringSuggestionEntry(overlay, candidate));
        }
        SuggestionSorting.filterByQuery(results, query);
        SuggestionSorting.sortByRelevance(results, query);
        return CompletableFuture.completedFuture(results);
    }

    public static CompletableFuture<List<SuggestionEntry>> getItemSuggestions(String query, ListWidgetOverlay<? extends SuggestionEntry> overlay) {
        List<SuggestionEntry> results = new ArrayList<>();

        for (Identifier id : RegistryHelper.getItems()) {
            results.add(new RegistrySuggestionEntry(
                    overlay, id, RegistryHelper::itemComponent, DisplayItemStack::getStackSimple
            ));
        }

        SuggestionSorting.filterByQuery(results, query);
        SuggestionSorting.sortByRelevance(results, query);
        return CompletableFuture.completedFuture(results);
    }

    public static CompletableFuture<List<SuggestionEntry>> getBlockSuggestions(String query, ListWidgetOverlay<? extends SuggestionEntry> overlay) {
        List<SuggestionEntry> results = new ArrayList<>();

        for (Identifier id : RegistryHelper.getBlocks()) {
            results.add(new RegistrySuggestionEntry(
                    overlay, id, RegistryHelper::itemComponent, DisplayItemStack::getStackSimple
            ));
        }

        SuggestionSorting.filterByQuery(results, query);
        SuggestionSorting.sortByRelevance(results, query);
        return CompletableFuture.completedFuture(results);
    }

    private final Overlay overlay;

    private boolean hasFetchedOnce = false;
    private int requestGeneration = 0;
    private String validValue;
    private @Nullable SuggestionEntry validEntry;
    private @Nullable SuggestionEntry currentEntry;

    private boolean autoSelectOnBlur;
    private final int iconSize;
    private final int maxVisibleEntries;
    private final SuggestionProvider suggestionProvider;
    private final Consumer<String> applyValue;
    private final Alignment.HorizontalIconPosition iconPosition;
    private boolean bordered = true;

    public SuggestionEditBox(Font font, int x, int y, int width, int height, Component message, SuggestionProvider suggestionProvider, Consumer<String> applyValue) {
        this(font, x, y, width, height, message, suggestionProvider, null, applyValue);
    }

    public SuggestionEditBox(Font font, int x, int y, int width, int height, Component message, SuggestionProvider suggestionProvider, String value, Consumer<String> applyValue) {
        this(font, x, y, width, height, DEFAULT_EDITBOX_ICON_SIZE, message, suggestionProvider, value, applyValue, true, width, DEFAULT_MAX_VISIBLE_ENTRIES, DEFAULT_OVERLAY_ALIGNMENT, DEFAULT_OVERLAY_DIRECTION, DEFAULT_ICON_POSITION);
    }

    public SuggestionEditBox(Font font, int x, int y, int width, int height,
                             int iconSize, Component message, SuggestionProvider suggestionProvider,
                             String value, Consumer<String> applyValue, boolean autoSelectOnBlur,
                             int overlayWidth, int maxVisibleEntries,
                             Alignment.Horizontal overlayAlignment, Alignment.OverlayDirection defaultAlignment,
                             Alignment.HorizontalIconPosition iconPosition) {
        super(font, x, y, width, height, message);
        this.getEditBox().setBordered(false);
        this.suggestionProvider = suggestionProvider;
        this.applyValue = applyValue;
        this.autoSelectOnBlur = autoSelectOnBlur;
        this.iconSize = iconSize;
        this.maxVisibleEntries = maxVisibleEntries;
        this.iconPosition = iconPosition;
        this.overlay = new Overlay(overlayWidth, overlayAlignment, defaultAlignment, iconPosition,this);
        setResponder(this::onTextChanged);
        resolveInitialEntry(value);
    }

    private void resolveInitialEntry(String value) {
        if (!autoSelectOnBlur) {
            return;
        }

        suggestionProvider.getSuggestions(value == null ? "" : value, overlay).thenAccept(suggestions ->
                Minecraft.getInstance().execute(() -> {
                    if (suggestions == null || suggestions.isEmpty()) {
                        return;
                    }
                    if (validEntry != null) {
                        for (SuggestionEntry entry : suggestions) {
                            if (entry.sortingString().equalsIgnoreCase(value)) {
                                applySuggestion(entry);
                            }
                        }
                    } else {
                        applySuggestion(suggestions.get(0));
                    }
                })
        );
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (bordered) {
            NineSlicedSprite sprite = isFocused() ? EDITBOX_HIGHLIGHTED : EDITBOX;
            graphics.drawTexture(sprite, getX(), getY(), getWidth(), getHeight());
        }
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (currentEntry != null && currentEntry.hasVisibleIcon()) {
            int iconX = iconPosition == Alignment.HorizontalIconPosition.LEFT
                    ? getX() + EDITBOX_ICON_PADDING
                    : getX() + getWidth() - EDITBOX_ICON_PADDING - iconSize;
            int iconY = getY() + (getHeight() - iconSize) / 2;
            currentEntry.drawIcon(graphics, iconX, iconY, iconSize, partialTick);
        }

        if (overlay.isOpen()) {
            graphics.addDeferred(overlay);
        }
    }

    public void setAutoSelectOnBlur(boolean autoSelectOnBlur) {
        this.autoSelectOnBlur = autoSelectOnBlur;
    }

    private void applySuggestion(SuggestionEntry entry) {
        String value = entry.sortingString();
        setValue(value);
        requestGeneration++;
        currentEntry = entry;
        updateEditBoxInsets();
        validValue = value;
        validEntry = entry;
        applyValue.accept(entry.saveValue());
        hasFetchedOnce = false;
        overlay.setItems(List.of());
        overlay.open(false);
    }

    private void revertToValidValue() {
        setValue(validValue);
        requestGeneration++;
        currentEntry = validEntry;
        updateEditBoxInsets();
        hasFetchedOnce = false;
        overlay.setItems(List.of());
        overlay.open(false);
    }

    @Override
    public WidgetOverlay getOverlay() {
        return overlay;
    }

    private void onTextChanged(String text) {
        currentEntry = null;
        updateSuggestions();
        updateEditBoxInsets();
    }

    private void updateSuggestions() {
        int generation = ++requestGeneration;

        suggestionProvider.getSuggestions(getValue(), overlay).thenAccept(suggestions ->
                Minecraft.getInstance().execute(() -> {
                    if (generation != requestGeneration) {
                        return;
                    }
                    hasFetchedOnce = true;
                    overlay.setItems(suggestions);
                    syncOverlayOpenState();
                })
        );
    }

    private void ensureSuggestionsLoaded() {
        if (!hasFetchedOnce) {
            updateSuggestions();
        } else {
            syncOverlayOpenState();
        }
    }

    private void syncOverlayOpenState() {
        overlay.open(isFocused() && overlay.hasItems());
    }

    private void updateEditBoxInsets() {
        boolean hasVisibleIcon = currentEntry != null && currentEntry.hasVisibleIcon();
        int iconInset = hasVisibleIcon ? iconSize + EDITBOX_ICON_PADDING : 0;
        boolean shiftForIcon = hasVisibleIcon && iconPosition == Alignment.HorizontalIconPosition.LEFT;

        if (bordered) {
            getEditBox().setX(getX() + TEXT_PADDING + (shiftForIcon ? iconInset : 0));
            getEditBox().setY(getY() + (getHeight() - TEXT_HEIGHT) / 2);
            getEditBox().setWidth(getWidth() - TEXT_PADDING * 2 - iconInset);
            getEditBox().setHeight(getHeight());
        } else {
            super.setX(getX());
            super.setY(getY());
            super.setWidth(getWidth());
            super.setHeight(getHeight());
            if (shiftForIcon) {
                getEditBox().setX(getEditBox().getX() + iconInset);
            }
            if (iconInset > 0) {
                getEditBox().setWidth(getEditBox().getWidth() - iconInset);
            }
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        updateEditBoxInsets();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateEditBoxInsets();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateEditBoxInsets();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateEditBoxInsets();
    }

    @Override
    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        updateEditBoxInsets();
    }

    @Override
    protected void onFocusGained() {
        setFocused(true);
        if (LastInput.input.isMouse() || (LastInput.input.isKeyboard() && LastInput.isTab())) {
            ensureSuggestionsLoaded();
        }
    }

    @Override
    protected void onFocusLost() {
        setFocused(false);
        if (autoSelectOnBlur) {
            revertToValidValue();
        } else {
            overlay.open(false);
        }
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        ensureSuggestionsLoaded();
        return super.onMouseClicked(mouseButtonEvent);
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (keyEvent.isEscape() && overlay.isOpen()) {
            revertToValidValue();
            return true;
        }
        ensureSuggestionsLoaded();
        return super.onKeyPressed(keyEvent);
    }

    private class Overlay extends ListWidgetOverlay<SuggestionEntry> {

        private final Alignment.OverlayDirection defaultAlignment;

        Overlay(int width, Alignment.Horizontal alignment, Alignment.OverlayDirection defaultAlignment, Alignment.HorizontalIconPosition iconPosition, GenericAbstractEditBox carrierWidget) {
            super(width, alignment, iconPosition, carrierWidget, maxVisibleEntries);
            this.defaultAlignment = defaultAlignment;
        }

        @Override
        public Alignment.OverlayDirection getDefaultDirection() {
            return defaultAlignment;
        }

        @Override
        protected void onEntryChosen(SuggestionEntry entry) {
            applySuggestion(entry);
        }
    }

    public static Builder builder(Font font, Component message, SuggestionProvider provider) {
        return new Builder(font, message, provider);
    }

    public static class Builder extends GenericAbstractEditBox.Builder<SuggestionEditBox, SuggestionEditBox.Builder> {

        private final SuggestionProvider provider;
        private Consumer<String> applyValue = s -> {};

        private int maxVisibleEntries = DEFAULT_MAX_VISIBLE_ENTRIES;
        private int iconSize = DEFAULT_EDITBOX_ICON_SIZE;
        private int overlayWidth = -1;
        private boolean autoSelectOnBlur = true;
        private String value;
        private Alignment.OverlayDirection defaultDirection = DEFAULT_OVERLAY_DIRECTION;
        private Alignment.Horizontal overlayAlignment = DEFAULT_OVERLAY_ALIGNMENT;
        private Alignment.HorizontalIconPosition iconPosition = DEFAULT_ICON_POSITION;

        public Builder(Font font, Component message, SuggestionProvider provider) {
            super(font, message);
            this.provider = provider;
            maxLength = 256;
        }

        public Builder applyValue(Consumer<String> applyValue) {
            this.applyValue = applyValue;
            return self();
        }

        public Builder maxVisibleEntries(int maxVisibleEntries) {
            this.maxVisibleEntries = maxVisibleEntries;
            return self();
        }

        @Override
        public Builder responder(Consumer<String> responder) {
            //ignoring responder since its use it to update overlay entries
            return self();
        }

        public Builder iconSize(int iconSize) {
            this.iconSize = iconSize;
            return self();
        }

        public Builder iconPosition(Alignment.HorizontalIconPosition iconPosition) {
            this.iconPosition = iconPosition;
            return self();
        }

        public Builder overlayWidth(int overlayWidth) {
            if (overlayWidth > 0) {
                this.overlayWidth = overlayWidth;
            }
            return self();
        }

        public Builder value(String value) {
            this.value = value;
            return self();
        }

        public Builder autoSelectOnBlur(boolean autoSelectOnBlur) {
            this.autoSelectOnBlur = autoSelectOnBlur;
            return self();
        }

        public Builder defaultOverlayDirection(Alignment.OverlayDirection defaultDirection) {
            this.defaultDirection = defaultDirection;
            return self();
        }

        public Builder overlayAlignment(Alignment.Horizontal overlayAlignment) {
            this.overlayAlignment = overlayAlignment;
            return self();
        }

        @Override
        public SuggestionEditBox build() {
            if (overlayWidth == -1) {
                overlayWidth = width;
            }

            SuggestionEditBox editBox = new SuggestionEditBox(
                    font, x, y, width, height, iconSize,
                    message, provider, value, applyValue, autoSelectOnBlur,
                    overlayWidth, maxVisibleEntries, overlayAlignment, defaultDirection,
                    iconPosition
            );

            editBox.setMaxLength(maxLength);
            if (hint != null) {
                editBox.setHint(hint);
            }
            editBox.setBordered(bordered);
            editBox.setEditable(editable);
            editBox.setFilter(filter);

            return editBox;
        }
    }

    @FunctionalInterface
    public interface SuggestionProvider {
        CompletableFuture<List<SuggestionEntry>> getSuggestions(String query, ListWidgetOverlay<? extends SuggestionEntry> overlay);
    }
}
