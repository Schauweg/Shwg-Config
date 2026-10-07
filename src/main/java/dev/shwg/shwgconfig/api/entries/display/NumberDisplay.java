package dev.shwg.shwgconfig.api.entries.display;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/**
 * Controls how a number is formatted for display on a slider - the raw value,
 * a percentage, or a fully custom format. Pass to any {@link CategoryBuilder}
 * {@code addNumberSlider(...)} overload that accepts one.
 *
 * @param <N> the numeric type being displayed
 */
public interface NumberDisplay<N extends Number> {
    Function<N, Component> textFunction(N min, N max);

    /**
     * Displays the raw value as-is, e.g. {@code 42}.
     */
    static <N extends Number> NumberDisplay<N> raw() {
        return (min, max) -> n -> Component.literal(String.valueOf(n));
    }

    /**
     * Appends {@code %} to the raw value with no math applied, e.g. {@code 42%}.
     * Meaningful when the value's own range is already 0–100.
     */
    static <N extends Number> NumberDisplay<N> percentage() {
        return (min, max) -> n -> Component.literal(n + "%");
    }

    /**
     * Maps the value's [min, max] range proportionally onto 0–100%, e.g. a range
     * of 0–200 with a value of 100 displays as {@code 50%}.
     */
    static <N extends Number> NumberDisplay<N> mappedPercentage() {
        return (min, max) -> n -> {
            double fraction = (n.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue());
            return Component.literal(Math.round(fraction * 100) + "%");
        };
    }


    /**
     * Appends a suffix to the raw value, e.g. {@code suffix("chunks", true)} →
     * {@code 8 chunks}. <br>
     * Overloaded method → {@link #suffix(String, boolean)}
     *
     * @param suffix the text to append
     */
    static <N extends Number> NumberDisplay<N> suffix(String suffix) {
        return suffix(suffix, true);
    }

    /**
     * Appends a suffix to the raw value, e.g. {@code suffix("chunks", true)} →
     * {@code 8 chunks}.
     *
     * @param suffix the text to append
     * @param space  whether to insert a space before the suffix
     */
    static <N extends Number> NumberDisplay<N> suffix(String suffix, boolean space) {
        return (min, max) -> n -> Component.literal(n + (space ? " " : "") + suffix);
    }

    /**
     * Fully custom formatting - use when none of the above fit.
     */
    static <N extends Number> NumberDisplay<N> custom(Function<N, Component> fn) {
        return (min, max) -> fn;
    }
}
