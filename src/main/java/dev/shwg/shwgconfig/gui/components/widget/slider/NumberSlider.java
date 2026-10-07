package dev.shwg.shwgconfig.gui.components.widget.slider;

import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public class NumberSlider<N extends Number> extends GenericAbstractSlider<N> {

    private final Consumer<N> applyValue;

    public NumberSlider(int x, int y, int width, int height, Component message, N min, N max, N initialValue, N stepSize,
                        Function<Double, N> fromDoubleFn, Function<N, Component> valueToText, Consumer<N> applyValue,
                        GenericTexture slider, GenericTexture sliderHighlighted, GenericTexture sliderHandle, GenericTexture sliderHandleHighlighted) {
        super(x, y, width, height, message, min, max, initialValue, stepSize,
                fromDoubleFn, Number::doubleValue, valueToText,
                slider, sliderHighlighted, sliderHandle, sliderHandleHighlighted);
        this.applyValue = applyValue;
    }

    @Override
    protected void onValueChanged(N newValue) {
        applyValue.accept(newValue);
    }

    @Override
    protected N fromDouble(double value) {
        return fromDoubleFn.apply(value);
    }

    @Override
    protected double toDouble(N value) {
        return value.doubleValue();
    }

    public static Builder<Integer> intSlider(Component message, int min, int max, int initialValue) {
        return intSlider(message, min, max, initialValue, 1);
    }

    public static Builder<Integer> intSlider(Component message, int min, int max, int initialValue, int stepSize) {
        return new Builder<>(message, min, max, initialValue, stepSize,d -> (int) Math.round(d), n -> Component.literal(String.valueOf(n)));
    }

    public static Builder<Float> floatSlider(Component message, float min, float max, float initialValue, float stepSize) {
        return new Builder<>(message, min, max, initialValue, stepSize,
                Double::floatValue, n -> Component.literal(String.format("%.2f", n)));
    }

    public static Builder<Double> doubleSlider(Component message, double min, double max, double initialValue, double stepSize) {
        return new Builder<>(message, min, max, initialValue, stepSize,
                d -> d, n -> Component.literal(String.format("%.2f", n)));
    }

    public static class Builder<N extends Number> extends GenericAbstractSlider.Builder<N, Builder<N>> {

        private final Function<Double, N> fromDoubleFn;
        private Consumer<N> applyValue = v -> {};

        private Builder(Component message, N min, N max, N initialValue, N stepSize,
                        Function<Double, N> fromDoubleFn, Function<N, Component> defaultValueToText) {
            super(min, max, initialValue, stepSize, message);
            this.fromDoubleFn = fromDoubleFn;
            this.valueToText = defaultValueToText;
        }

        public Builder<N> applyValue(Consumer<N> applyValue) {
            this.applyValue = applyValue != null ? applyValue : v -> {};
            return self();
        }

        @Override
        public NumberSlider<N> build() {
            return new NumberSlider<>(x, y, width, height, message, minValue, maxValue, initialValue, stepSize,
                    fromDoubleFn, valueToText, applyValue, slider, sliderHighlighted, sliderHandle, sliderHandleHighlighted);
        }
    }
}
