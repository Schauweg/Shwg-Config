package dev.shwg.shwgconfig.gui.deferred;

import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericTooltip implements DeferredElement {

    private static final int MARGIN = 9;

    /** Default maximum line width in pixels - the width vanilla wraps widget tooltips to. */
    public static final int DEFAULT_MAX_WIDTH = 170;
    /** Max width that disables wrapping; lines only break at {@code \n}. */
    public static final int NO_MAX_WIDTH = -1;
    /** Default symbol in front of bullet points. */
    public static final String DEFAULT_BULLET_SYMBOL = "•";

    public static final NineSlicedSprite TOOLTIP_BACKGROUND = NineSlicedSprite.fromMcMeta("minecraft", "tooltip/background");
    public static final NineSlicedSprite TOOLTIP_FRAME = NineSlicedSprite.fromMcMeta("minecraft", "tooltip/frame");

    public static void renderAdjustedTooltip(GenericGraphics graphics, int x, int y, int width, int height) {
        int tooltipX = x - MARGIN;
        int tooltipY = y - MARGIN;
        int tooltipWidth = width + MARGIN * 2;
        int tooltipHeight = height + MARGIN * 2;
        graphics.drawSprite(TOOLTIP_BACKGROUND, tooltipX, tooltipY, tooltipWidth, tooltipHeight);
        graphics.drawSprite(TOOLTIP_FRAME, tooltipX, tooltipY, tooltipWidth, tooltipHeight);
    }

    /**
     * One line as it was added - either a normal line or a bullet point.
     * Wrapping happens later, in {@link #getLines(Font)}.
     */
    private record Line(Component text, boolean bullet) {
    }

    private final List<Line> lines;
    @Nullable
    private final ItemStack icon;
    private final int maxWidth;
    private final String bulletSymbol;

    public GenericTooltip(List<Component> lines) {
        this(lines, null);
    }

    public GenericTooltip(List<Component> lines, @Nullable ItemStack icon) {
        this(lines, icon, DEFAULT_MAX_WIDTH);
    }

    public GenericTooltip(List<Component> lines, @Nullable ItemStack icon, int maxWidth) {
        this(toPlainLines(lines), icon, maxWidth, DEFAULT_BULLET_SYMBOL);
    }

    public GenericTooltip(Component singleLine) {
        this(List.of(singleLine));
    }

    /** Used by the builder, the only place bullet lines are created. */
    private GenericTooltip(List<Line> lines, @Nullable ItemStack icon, int maxWidth, String bulletSymbol) {
        this.lines = List.copyOf(lines);
        this.icon = icon;
        this.maxWidth = maxWidth;
        this.bulletSymbol = bulletSymbol;
    }

    private static List<Line> toPlainLines(@Nullable List<Component> components) {
        List<Line> result = new ArrayList<>();
        if (components != null) {
            for (Component component : components) {
                result.add(new Line(component, false));
            }
        }
        return result;
    }

    /**
     * The lines as they were added, without wrapping. Bullet points get their
     * symbol in front.
     */
    public List<Component> getLines() {
        List<Component> result = new ArrayList<>();
        for (Line line : lines) {
            result.add(line.bullet() ? bulletPrefix().append(line.text()) : line.text());
        }
        return result;
    }

    /**
     * The lines as they should be rendered: each line is broken at {@code \n}
     * and wrapped to {@link #getMaxWidth()}, keeping its styling. Wrapped
     * bullet points are indented so their text lines up under the first line.
     */
    public List<Component> getLines(Font font) {
        List<Component> result = new ArrayList<>();
        for (Line line : lines) {
            if (line.bullet()) {
                addBullet(font, line.text(), result);
            } else {
                for (FormattedText part : wrap(font, line.text(), maxWidth)) {
                    result.add(toComponent(part));
                }
            }
        }
        return result;
    }

    private void addBullet(Font font, Component text, List<Component> result) {
        MutableComponent prefix = bulletPrefix();
        int prefixWidth = font.width(prefix);

        // spaces as wide as the prefix (as close as the space width allows)
        int spaceCount = Math.max(1, Math.round(prefixWidth / (float) font.width(" ")));
        String indent = " ".repeat(spaceCount);

        int textWidth = maxWidth > 0 ? Math.max(1, maxWidth - prefixWidth) : NO_MAX_WIDTH;
        List<FormattedText> parts = wrap(font, text, textWidth);
        for (int i = 0; i < parts.size(); i++) {
            MutableComponent start = i == 0 ? bulletPrefix() : Component.literal(indent);
            result.add(start.append(toComponent(parts.get(i))));
        }
    }

    private MutableComponent bulletPrefix() {
        return Component.literal(bulletSymbol + " ");
    }

    /** Splits {@code text} into lines no wider than {@code width} (no limit if {@code width <= 0}). */
    private static List<FormattedText> wrap(Font font, Component text, int width) {
        int wrapWidth = width > 0 ? width : Integer.MAX_VALUE;
        List<FormattedText> parts = font.getSplitter().splitLines(text, wrapWidth, Style.EMPTY);
        // keep empty lines as spacers
        return parts.isEmpty() ? List.of(FormattedText.EMPTY) : parts;
    }

    /** Rebuilds a wrapped piece of text as a Component, one styled part at a time. */
    private static MutableComponent toComponent(FormattedText text) {
        MutableComponent component = Component.empty();
        text.<Object>visit((style, string) -> {
            component.append(Component.literal(string).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return component;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public String getBulletSymbol() {
        return bulletSymbol;
    }

    @Nullable
    public ItemStack getIcon() {
        return icon;
    }

    public boolean hasIcon() {
        return icon != null && !icon.isEmpty();
    }

    public boolean isEmpty() {
        return lines.isEmpty() && !hasIcon();
    }

    public static GenericTooltip of(Component... lines) {
        return new GenericTooltip(List.of(lines));
    }

    public static GenericTooltip ofItem(ItemStack stack, List<Component> lines) {
        return new GenericTooltip(lines, stack);
    }

    public static GenericTooltip empty() {
        return new GenericTooltip(List.of());
    }

    /**
     * Starts building a tooltip.
     *
     * <pre>{@code
     * GenericTooltip.builder()
     *     .line(Component.literal("Offsets the damage number by -1"))
     *     .bullet(Component.literal("ON: shows 0 when no final hit is left"))
     *     .bullet(Component.literal("OFF: vanilla behavior, shows 1"))
     *     .build()
     * }</pre>
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void render(GenericGraphics genericGraphics, int mouseX, int mouseY, float partialTicks) {
        genericGraphics.renderTooltip(this, mouseX, mouseY);
    }

    public static final class Builder {

        private final List<Line> lines = new ArrayList<>();
        @Nullable
        private ItemStack icon;
        private int maxWidth = DEFAULT_MAX_WIDTH;
        private String bulletSymbol = DEFAULT_BULLET_SYMBOL;

        private Builder() {
        }

        /** Adds a line. Long lines are wrapped; {@code \n} also starts a new line. */
        public Builder line(Component line) {
            lines.add(new Line(line, false));
            return this;
        }

        /** Adds several lines, in order. */
        public Builder lines(Component... lines) {
            for (Component line : lines) {
                line(line);
            }
            return this;
        }

        /** Adds several lines, in order. */
        public Builder lines(List<Component> lines) {
            for (Component line : lines) {
                line(line);
            }
            return this;
        }

        /** Adds an empty line, e.g. between a description and a list. */
        public Builder emptyLine() {
            return line(Component.empty());
        }

        /**
         * Adds a bullet point. If it wraps, the following lines are indented
         * so the text lines up under the first line.
         */
        public Builder bullet(Component text) {
            lines.add(new Line(text, true));
            return this;
        }

        /** Adds several bullet points, in order. */
        public Builder bullets(Component... texts) {
            for (Component text : texts) {
                bullet(text);
            }
            return this;
        }

        /** Shows the item's tooltip image (e.g. bundle contents) below the text. */
        public Builder icon(@Nullable ItemStack icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Maximum line width in pixels; defaults to {@link #DEFAULT_MAX_WIDTH}.
         * {@link #NO_MAX_WIDTH} disables wrapping.
         */
        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /** Disables wrapping; lines only break at {@code \n}. */
        public Builder noMaxWidth() {
            return maxWidth(NO_MAX_WIDTH);
        }

        /** Symbol in front of every bullet point; defaults to {@link #DEFAULT_BULLET_SYMBOL}. */
        public Builder bulletSymbol(String bulletSymbol) {
            this.bulletSymbol = bulletSymbol;
            return this;
        }

        public GenericTooltip build() {
            return new GenericTooltip(lines, icon, maxWidth, bulletSymbol);
        }
    }
}
