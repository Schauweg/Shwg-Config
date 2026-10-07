package dev.shwg.shwgconfig.api.entries.display;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Function;

/**
 * Label formatting for enum-backed dropdowns and cycle buttons. Pass to any
 * {@link CategoryBuilder} overload that accepts a label function.
 */
public final class EnumLabels {
    private EnumLabels() {}

    /**
     * Displays the enum constant's name exactly as declared, e.g. {@code MY_VALUE}.
     */
    public static <E extends Enum<E>> Function<E, Component> raw() {
        return e -> Component.literal(e.name());
    }

    /**
     * Displays the enum constant's name formatted from {@code SCREAMING_SNAKE_CASE}
     * to title case with spaces, e.g. {@code MY_VALUE} → {@code My Value}.
     */
    public static <E extends Enum<E>> Function<E, Component> formatted() {
        return e -> Component.literal(formatEnumName(e.name()));
    }

    private static String formatEnumName(String name) {
        String[] words = name.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word, 1, word.length());
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Fully custom formatting - use when none of the above fit.
     */
    public static <E extends Enum<E>> Function<E, Component> custom(Function<E, Component> fn) {
        return fn;
    }
}
