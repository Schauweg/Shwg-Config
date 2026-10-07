package dev.shwg.shwgconfig.gui.components.widget.editbox;

import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Consumer;

public class HexColorEditBox extends GenericAbstractEditBox {

    private static final int PREVIEW_SIZE = 16;
    private static final int PREVIEW_PADDING = 2;
    private static final int CHECKER_SIZE = 4;
    private static final int CHECKER_LIGHT = 0xFFCCCCCC;
    private static final int CHECKER_DARK = 0xFF888888;

    private final int maxHexLength;
    private int currentColor;
    private final Consumer<Integer> applyValue;

    public HexColorEditBox(Font font, int x, int y, int width, int height, boolean hasAlpha, Consumer<Integer> applyValue) {
        super(font, x, y, width, height, Component.empty());
        this.maxHexLength = hasAlpha ? 8 : 6;
        setMaxLength(maxHexLength + 1);
        setFilter(s -> s.isEmpty() || s.matches("#[0-9A-Fa-f]{0," + maxHexLength + "}"));
        setResponder(this::onTextChanged);
        setValue("#");
        this.applyValue = applyValue;
    }

    private void onTextChanged(String text) {
        String upper = text.toUpperCase(Locale.ROOT);
        if (!upper.equals(text)) {
            int cursor = getCursorPosition();
            setValue(upper);
            setCursorPosition(cursor);
            return;
        }
        Integer color = getColorValue();
        if (color != null) {
            currentColor = color;
            applyValue.accept(color);
        }
    }

    public void setColorValue(int color) {
        this.currentColor = color;
        setValue(maxHexLength == 8
                ? "#" + String.format("%08X", color)
                : "#" + String.format("%06X", color & 0x00FFFFFF));
    }

    public Integer getColorValue() {
        String text = getValue();
        if (text.length() != maxHexLength + 1) return null;
        try {
            String hex = text.substring(1);
            return maxHexLength == 8
                    ? (int) Long.parseLong(hex, 16)
                    : Integer.parseInt(hex, 16) | 0xFF000000;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected boolean onCharTyped(GenericCharacterEvent event) {
        if (!getValue().isEmpty() && getHighlighted().equals(getValue())) {
            setValue("#");
        }
        return super.onCharTyped(event);
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent event) {
        if (event.isBackspace() && getCursorPosition() <= 1) {
            return false;
        }
        if (event.isPaste()) {
            String processed = processClipboard(Minecraft.getInstance().keyboardHandler.getClipboard());
            if (!processed.isEmpty()) {
                setValue("#" + processed);
            }
            return true;
        }
        boolean handled = super.onKeyPressed(event);
        if (getValue().isEmpty()) {
            setValue("#");
        }
        return handled;
    }

    private String processClipboard(String input) {
        String s = input.trim();
        if (s.startsWith("#")) s = s.substring(1);
        s = s.replaceAll("[^0-9A-Fa-f]", "").toUpperCase(Locale.ROOT);
        return s.substring(0, Math.min(s.length(), maxHexLength));
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int px = getX() + getWidth() - PREVIEW_PADDING - PREVIEW_SIZE;
        int py = getY() + (getHeight() - PREVIEW_SIZE) / 2;
        renderCheckerboard(graphics, px, py, PREVIEW_SIZE, PREVIEW_SIZE);
        graphics.fillWH(px, py, PREVIEW_SIZE, PREVIEW_SIZE, currentColor);
    }

    private static void renderCheckerboard(GenericGraphics graphics, int x, int y, int width, int height) {
        for (int cy = 0; cy < height; cy += CHECKER_SIZE) {
            for (int cx = 0; cx < width; cx += CHECKER_SIZE) {
                int color = ((cx / CHECKER_SIZE) + (cy / CHECKER_SIZE)) % 2 == 0 ? CHECKER_LIGHT : CHECKER_DARK;
                graphics.fillWH(x + cx, y + cy,
                        Math.min(CHECKER_SIZE, width - cx), Math.min(CHECKER_SIZE, height - cy), color);
            }
        }
    }

    public static Builder rgb(Font font, Component message) {
        return new Builder(font, message, false);
    }

    public static Builder argb(Font font, Component message) {
        return new Builder(font, message, true);
    }

    public static class Builder extends GenericAbstractEditBox.Builder<HexColorEditBox, HexColorEditBox.Builder> {

        private final boolean alpha;
        private int color = 0x00000000;
        private Consumer<Integer> applyValue = integer -> {};

        protected Builder(Font font, Component message, boolean alpha) {
            super(font, message);
            this.alpha = alpha;
        }

        public Builder setColor(int color) {
            this.color = color;
            return self();
        }

        public Builder applyValue(Consumer<Integer> applyValue) {
            this.applyValue = applyValue != null ? applyValue : integer -> {};
            return self();
        }

        @Override
        public HexColorEditBox build() {
            HexColorEditBox editBox = new HexColorEditBox(font, x, y, width, height, alpha, applyValue);
            editBox.setColorValue(color);
            return editBox;
        }

    }
}
