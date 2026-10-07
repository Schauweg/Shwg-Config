package dev.shwg.shwgconfig.gui.components.widget.button;

import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericIcons;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleButton<T> extends GenericAbstractButton {

    private static final int ARROW_PADDING = 3;
    private static final float ARROW_SCALE = 0.5f;

    private final List<T> options;
    private final Function<T, Component> labelFn;
    private final Consumer<T> applyValue;
    private T value;

    public CycleButton(int x, int y, int width, int height, List<T> options, Function<T, Component> labelFn,
                       T value, Consumer<T> applyValue, GenericTexture button, GenericTexture buttonHighlighted, GenericTexture buttonDisabled) {
        super(x, y, width, height, labelFn.apply(value), button, buttonHighlighted, buttonDisabled);
        this.options = options;
        this.labelFn = labelFn;
        this.value = value;
        this.applyValue = applyValue;
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        if (isMouseOverLeftArrow(mouseX, mouseY)) {
            advance(-1);
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }  else if (isMouseOverRightArrow(mouseX, mouseY) || event.isRightClick()) {
            advance(1);
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return super.onMouseClicked(event);
    }

    @Override
    protected void onPress() {
        advance(1);
    }

    private void advance(int direction) {
        int index = options.indexOf(value);
        value = options.get(Math.floorMod(index + direction, options.size()));
        applyValue.accept(value);
        setMessage(labelFn.apply(value));
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawTexture(currentButtonTexture(), getX(), getY(), width, height);
        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);

        graphics.pushPose();
        graphics.scale(ARROW_SCALE, ARROW_SCALE);

        GenericIcons.drawArrowLeft(graphics, Math.round(getLeftArrowX() / ARROW_SCALE), Math.round(getArrowY() / ARROW_SCALE), isMouseOverLeftArrow(mouseX, mouseY));
        GenericIcons.drawArrowRight(graphics, Math.round(getRightArrowX() / ARROW_SCALE), Math.round(getArrowY() / ARROW_SCALE), isMouseOverRightArrow(mouseX, mouseY));

        graphics.popPose();

        if (isHovered) {
            graphics.requestCursor(GenericCursors.POINTING_HAND);
        }
    }

    private boolean isMouseOverLeftArrow(double mouseX, double mouseY) {
        return mouseX >= getLeftArrowX() && mouseX < getLeftArrowX() + getScaledArrowWidth()
                && mouseY >= getArrowY() && mouseY < getArrowY() + getScaledArrowHeight();
    }

    private boolean isMouseOverRightArrow(double mouseX, double mouseY) {
        return mouseX >= getRightArrowX() && mouseX < getRightArrowX() + getScaledArrowWidth()
                && mouseY >= getArrowY() && mouseY < getArrowY() + getScaledArrowHeight();
    }

    private int getScaledArrowWidth() {
        return Math.round(GenericIcons.LARGE_ARROW_GLYPH_WIDTH * ARROW_SCALE);
    }

    private int getScaledArrowHeight() {
        return Math.round(GenericIcons.LARGE_ARROW_GLYPH_HEIGHT * ARROW_SCALE);
    }

    private int getArrowY() {
        return getY() + height / 2 - getScaledArrowHeight() / 2;
    }

    private int getLeftArrowX() {
        return getX() + ARROW_PADDING;
    }

    private int getRightArrowX() {
        return getX() + width - ARROW_PADDING - getScaledArrowWidth();
    }

    public static <T> Builder<T> of(List<T> options, Function<T, Component> labelFn, T value) {
        return new Builder<>(options, labelFn, value);
    }

    public static class Builder<T> extends GenericAbstractButton.Builder<CycleButton<T>, Builder<T>> {
        private final List<T> options;
        private final Function<T, Component> labelFn;
        private final T value;
        private Consumer<T> applyValue = v -> {};

        private Builder(List<T> options, Function<T, Component> labelFn, T value) {
            super(labelFn.apply(value));
            this.options = options;
            this.labelFn = labelFn;
            this.value = value;
        }

        public Builder<T> applyValue(Consumer<T> applyValue) {
            this.applyValue = applyValue != null ? applyValue : v -> {};
            return self();
        }

        @Override
        public CycleButton<T> build() {
            return new CycleButton<>(x, y, width, height, options, labelFn, value, applyValue, button, buttonHighlighted, buttonDisabled);
        }
    }
}
