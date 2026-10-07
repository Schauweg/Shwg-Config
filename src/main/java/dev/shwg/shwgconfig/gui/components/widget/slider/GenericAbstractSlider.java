package dev.shwg.shwgconfig.gui.components.widget.slider;

import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.LastInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Function;

public abstract class GenericAbstractSlider<T> extends GenericAbstractWidget {

    public static final NineSlicedSprite SLIDER = NineSlicedSprite.constantBorder("minecraft", "textures/gui/slider.png",
            0, 0, 200, 20, 256, 256,
            "widget/slider", 3, false);

    public static final NineSlicedSprite SLIDER_HIGHLIGHTED = NineSlicedSprite.constantBorder("minecraft", "textures/gui/slider.png",
            0, 20, 200, 20, 256, 256,
            "widget/slider_highlighted", 3, false);

    public static final NineSlicedSprite SLIDER_HANDLE = NineSlicedSprite.constantBorder("minecraft", "textures/gui/slider.png",
            0, 40, 200, 20, 256, 256,
            "widget/slider_handle", 3, false);

    public static final NineSlicedSprite SLIDER_HANDLE_HIGHLIGHTED = NineSlicedSprite.constantBorder("minecraft", "textures/gui/slider.png",
            0, 60, 200, 20, 256, 256,
            "widget/slider_handle_highlighted", 3, false);

    protected final GenericTexture slider;
    protected final GenericTexture sliderHighlighted;
    protected final GenericTexture sliderHandle;
    protected final GenericTexture sliderHandleHighlighted;

    protected final T minValue;
    protected final T maxValue;
    protected final T stepSize;

    protected T value;
    protected boolean dragging = false;
    protected boolean keyboardControlled = false;

    protected final Function<T, Component> valueToText;
    protected final Function<Double, T> fromDoubleFn;
    protected final Function<T, Double> toDoubleFn;


    public GenericAbstractSlider(int x, int y, int width, int height, Component message,
                                 T min, T max, T initialValue, T stepSize,
                                 Function<Double, T> fromDoubleFn, Function<T, Double> toDoubleFn,
                                 Function<T, Component> valueToText,
                                 GenericTexture slider, GenericTexture sliderHighlighted,
                                 GenericTexture sliderHandle, GenericTexture sliderHandleHighlighted) {
        super(x, y, width, height, message);

        this.fromDoubleFn = fromDoubleFn;
        this.toDoubleFn = toDoubleFn;
        this.slider = slider;
        this.sliderHighlighted = sliderHighlighted;
        this.sliderHandle = sliderHandle;
        this.sliderHandleHighlighted = sliderHandleHighlighted;
        this.minValue = min;
        this.maxValue = max;
        this.stepSize = stepSize;
        this.valueToText = valueToText != null ? valueToText : v -> Component.literal(String.valueOf(v));

        this.value = snapToStep(clamp(initialValue)); // now safe - fromDoubleFn/toDoubleFn are already assigned above
    }

    protected abstract void onValueChanged(T newValue);

    /** Converts a raw track position (0.0 at minValue, 1.0 at maxValue - though values
     *  outside that range are possible mid-computation) into a real T value. */
    protected T fromDouble(double value) {
        return fromDoubleFn.apply(value);
    }
    /** The inverse of {@link #fromDouble(double)} - where does this T value sit on the
     *  same double-space track position axis. This is the one hook that replaces the
     *  old {@code T extends Number} assumption; a non-numeric T just needs a sensible
     *  answer here (e.g. an enum's ordinal fraction). */
    protected double toDouble(T value) {
        return toDoubleFn.apply(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        T clamped = clamp(newValue);
        T snapped = snapToStep(clamped);

        if (!snapped.equals(this.value)) {
            this.value = snapped;
            onValueChanged(this.value);
        }
    }

    protected T clamp(T val) {
        double d = toDouble(val);
        if (d < toDouble(minValue)) return minValue;
        if (d > toDouble(maxValue)) return maxValue;
        return val;
    }

    protected T snapToStep(T val) {
        double step = toDouble(stepSize);
        if (step <= 0) return val;

        double min = toDouble(minValue);
        double v = toDouble(val);

        double snapped = Math.round((v - min) / step) * step + min;
        return fromDouble(snapped);
    }

    protected double getProgress() {
        double min = toDouble(minValue);
        double max = toDouble(maxValue);
        double v = toDouble(value);
        return (v - min) / (max - min);
    }

    protected void setProgress(double progress) {
        progress = Mth.clamp(progress, 0.0, 1.0);
        double raw = toDouble(minValue) + (toDouble(maxValue) - toDouble(minValue)) * progress;
        setValue(fromDouble(raw));
    }

    @Override
    protected void onFocusGained() {
        if (LastInput.isMouse() || (LastInput.isKeyboard() && LastInput.isTab())) {
            keyboardControlled = true;
        }
    }

    @Override
    protected void onFocusLost() {
        keyboardControlled = false;
    }

    @Override
    protected void onDrag(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging) {
            setValueFromMouse(event.getMouseX());
        }
    }

    @Override
    protected void onClick(GenericMouseButtonEvent event) {
        if (!this.active || !this.visible) return;

        this.dragging = true;
        this.keyboardControlled = true;
        setValueFromMouse(event.getMouseX());
    }

    @Override
    protected void onRelease(GenericMouseButtonEvent event) {
        this.dragging = false;
    }

    private void setValueFromMouse(double mouseX) {
        double progress = (mouseX - (getX() + 4)) / (getWidth() - 8);
        setProgress(progress);
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent event) {
        if (!this.active || !this.visible) return false;

        if (event.isSelection()) {
            this.keyboardControlled = !this.keyboardControlled;
            return true;
        }

        if (this.keyboardControlled) {
            boolean left = event.isLeft();
            boolean right = event.isRight();

            if (left || right) {
                double direction = left ? -1.0 : 1.0;
                double step = toDouble(stepSize) / (toDouble(maxValue) - toDouble(minValue));
                setProgress(getProgress() + direction * step);
                return true;
            }
        }
        return false;
    }

    protected void handleCursor(GenericGraphics graphics) {
        if (this.isHovered()) {
            if (this.dragging) {
                graphics.requestCursor(GenericCursors.RESIZE_EW);
            } else {
                graphics.requestCursor(GenericCursors.POINTING_HAND);
            }
        }
    }

    protected void drawSlider(GenericGraphics graphics) {
        GenericTexture trackTexture = this.isActive() && this.isFocused() && !this.keyboardControlled ? sliderHighlighted : slider;
        graphics.drawTexture(trackTexture, getX(), getY(), getWidth(), getHeight());
    }

    protected void drawHandler(GenericGraphics graphics) {
        boolean isHighlighted = this.isActive() && (this.isHovered() || this.dragging);
        int handleX = getX() + (int) (getProgress() * (getWidth() - 8));
        GenericTexture handleTexture = (isHighlighted || dragging || keyboardControlled) ? sliderHandleHighlighted : sliderHandle;
        graphics.drawTexture(handleTexture, handleX, getY(), 8, getHeight());
    }

    protected void drawString(GenericGraphics graphics) {
        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, valueToText.apply(value), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public void renderWidget(@org.jetbrains.annotations.NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Identical across all three old subclasses - belongs here now that only one
        // concrete numeric subclass remains, rather than re-declared per subclass.
        drawSlider(graphics);
        drawHandler(graphics);
        drawString(graphics);
        handleCursor(graphics);
    }

    public abstract static class Builder<T, B extends Builder<T, B>> extends GenericAbstractWidget.Builder<GenericAbstractSlider<T>, B> {

        protected GenericTexture slider = SLIDER;
        protected GenericTexture sliderHighlighted = SLIDER_HIGHLIGHTED;
        protected GenericTexture sliderHandle = SLIDER_HANDLE;
        protected GenericTexture sliderHandleHighlighted = SLIDER_HANDLE_HIGHLIGHTED;

        protected final T minValue;
        protected final T maxValue;
        protected T initialValue;
        protected T stepSize;

        protected Function<T, Component> valueToText;

        protected Builder(T min, T max, T initialValue, T stepSize, Component message) {
            super(message);
            this.minValue = min;
            this.maxValue = max;
            this.initialValue = initialValue;
            this.stepSize = stepSize;
        }

        public B valueToText(Function<T, Component> valueToText) {
            this.valueToText = valueToText;
            return self();
        }

        public B initialValue(T initialValue) {
            this.initialValue = initialValue;
            return self();
        }

        public B customTextures(GenericTexture slider, GenericTexture sliderHighlighted, GenericTexture sliderHandle, GenericTexture sliderHandleHighlighted) {
            this.slider = slider;
            this.sliderHighlighted = sliderHighlighted;
            this.sliderHandle = sliderHandle;
            this.sliderHandleHighlighted = sliderHandleHighlighted;
            return self();
        }
    }
}
