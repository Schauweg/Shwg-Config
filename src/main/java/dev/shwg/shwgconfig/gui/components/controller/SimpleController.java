package dev.shwg.shwgconfig.gui.components.controller;

import dev.shwg.shwgconfig.api.entries.ControllerEntry;
import dev.shwg.shwgconfig.api.entries.EntryOptions;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SimpleController implements RenderableElement, GenericLayout {

    public static final int DEFAULT_CONTROLLER_WIDTH = 100;
    public static final int DEFAULT_CONTROLLER_HEIGHT = 20;
    public static final int LABEL_CONTROLLER_SPACING = 20;
    private static final int LINE_SPACING = 2;

    private final List<FormattedCharSequence> labelLines;
    private final int maxLabelWidth;
    private final GenericTooltip labelTooltip;
    private final Supplier<Boolean> enabledCondition;
    private final GenericAbstractWidget widget;
    private final Font font;

    private int x, y, width, height;
    private boolean visible = true;

    public <T> SimpleController(ControllerEntry<T> entry, int rowWidth, int minRowHeight, Font font) {
        this.font = font;
        this.width = rowWidth;

        this.maxLabelWidth = Math.max(20, rowWidth - LABEL_CONTROLLER_SPACING - DEFAULT_CONTROLLER_WIDTH);
        this.labelLines = font.split(entry.getLabel(), maxLabelWidth);

        EntryOptions options = entry.getEntryOptions();
        this.labelTooltip = options.getLabelTooltip() != null ? GenericTooltip.of(options.getLabelTooltip()) : null;
        this.enabledCondition = options.getEnabledCondition();

        this.widget = buildWidget(entry, DEFAULT_CONTROLLER_HEIGHT, font);

        int textBlockHeight = labelLines.size() * (font.lineHeight + LINE_SPACING) - LINE_SPACING;
        this.height = Math.max(minRowHeight, Math.max(textBlockHeight, widget.getHeight()));

        if (options.getTooltip() != null) {
            widget.setTooltip(GenericTooltip.of(options.getTooltip()));
        }
    }

    private static <T> GenericAbstractWidget buildWidget(ControllerEntry<T> entry, int height, Font font) {
        return entry.getWidgetFactory().createWidget(DEFAULT_CONTROLLER_WIDTH, height, font, entry.getConfigValue());
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(widget);
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        if (enabledCondition != null) {
            widget.active = enabledCondition.get();
        }

        if (isMouseOver(mouseX, mouseY)) {
            graphics.fillWH(x - 2, y - 1, width + 4, height + 2, 0x1AC8C8C8);
        }

        int textColor = widget.active ? 0xFFFFFFFF : 0xFF666666;
        int textBlockHeight = labelLines.size() * (font.lineHeight + LINE_SPACING) - LINE_SPACING;
        int textY = y + (height - textBlockHeight) / 2;

        for (int i = 0; i < labelLines.size(); i++) {
            graphics.drawString(font, labelLines.get(i), x, textY + i * (font.lineHeight + LINE_SPACING), textColor);
        }

        widget.render(graphics, mouseX, mouseY, partialTicks);

        if (labelTooltip != null && isMouseOverLabel(mouseX, mouseY)) {
            graphics.addDeferred(labelTooltip);
        }
    }

    private boolean isMouseOverLabel(int mouseX, int mouseY) {
        int textBlockHeight = labelLines.size() * (font.lineHeight + LINE_SPACING) - LINE_SPACING;
        int textY = y + (height - textBlockHeight) / 2;
        return mouseX >= x && mouseX <= x + maxLabelWidth
                && mouseY >= textY && mouseY <= textY + textBlockHeight;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= getRight() &&
                mouseY >= y && mouseY <= getBottom();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        widget.setX(x + width - widget.getWidth());
    }

    @Override
    public void setY(int y) {
        this.y = y;
        widget.setY(y + (height - widget.getHeight()) / 2);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        widget.setX(x + width - widget.getWidth());
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }

    @Override
    public int getContentHeight() {
        return getHeight();
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        widget.setVisible(visible);
    }
}
