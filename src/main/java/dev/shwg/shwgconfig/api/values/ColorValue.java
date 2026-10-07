package dev.shwg.shwgconfig.api.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.TextColor;

public class ColorValue extends ConfigValue<Integer> implements CustomJsonSerializable {

    private final boolean hasAlpha;

    private ColorValue(int defaultValue, boolean hasAlpha) {
        super(defaultValue);
        this.hasAlpha = hasAlpha;
    }

    /** RGB color - is always opaque
     * @param defaultValue default value best provided as {@code 0xRRGGBB }
     * */
    public static ColorValue rgb(int defaultValue) {
        return new ColorValue(defaultValue | 0xFF000000, false);
    }

    /** ARGB color - transparency is read from the first to bytes as-is.
     * @param defaultValue default value best provided as {@code 0xAARRGGBB }
     * */
    public static ColorValue argb(int defaultValue) {
        return new ColorValue(defaultValue, true);
    }

    /** RGB color from a {@link TextColor} - is always opaque. */
    public static ColorValue ofTextColor(TextColor defaultColor) {
        return new ColorValue(defaultColor.getValue(), false);
    }

    /**
     * Whether the color supports an alpha channel for transparency or not,
     * used for example inside the: <br>
     * {@link dev.shwg.shwgconfig.gui.components.widget.editbox.HexColorEditBox}.
     * @return Boolean value whether color is transparent or not
     */
    public boolean hasAlpha() {
        return hasAlpha;
    }

    @Override
    public JsonElement toJsonElement() {
        return new JsonPrimitive(toHexString(value));
    }

    @Override
    public void loadFromJson(JsonElement element) {
        setValue(fromHexString(element.getAsString()));
    }

    private String toHexString(int color) {
        if (hasAlpha) {
            return String.format("%08X", color);
        } else {
            return String.format("%06X", color & 0x00FFFFFF);
        }
    }

    private int fromHexString(String hex) {
        if (hasAlpha) {
            // Long parse needed - 8-char hex can exceed Integer.MAX_VALUE
            return (int) Long.parseLong(hex, 16);
        } else {
            return Integer.parseInt(hex, 16) | 0xFF000000;
        }
    }
}
