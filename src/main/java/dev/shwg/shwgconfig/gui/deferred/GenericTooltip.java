package dev.shwg.shwgconfig.gui.deferred;

import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericTooltip implements DeferredElement {

    private static final int MARGIN = 9;

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

    private final List<Component> lines;
    private final List<FormattedCharSequence> wrappedLines;
    @Nullable
    private final ItemStack icon;

    public GenericTooltip(List<Component> lines) {
        this(lines, null);
    }

    public GenericTooltip(List<Component> lines, @Nullable ItemStack icon) {
        this(lines, icon, List.of());
    }

    private GenericTooltip(List<Component> lines, @Nullable ItemStack icon, List<FormattedCharSequence> wrappedLines) {
        this.lines = lines != null ? lines : List.of();
        this.icon = icon;
        this.wrappedLines = wrappedLines != null ? wrappedLines : List.of();
    }

    public GenericTooltip(Component singleLine) {
        this(List.of(singleLine));
    }

    public List<Component> getLines() {
        return lines;
    }

    public List<FormattedCharSequence> getWrappedLines() {
        return wrappedLines;
    }

    @Nullable
    public ItemStack getIcon() {
        return icon;
    }

    public boolean hasIcon() {
        return icon != null && !icon.isEmpty();
    }

    public boolean isWrapped() {
        return !wrappedLines.isEmpty();
    }

    public boolean isEmpty() {
        return lines.isEmpty() && wrappedLines.isEmpty() && !hasIcon();
    }

    public static GenericTooltip of(Component... lines) {
        return new GenericTooltip(List.of(lines));
    }

    public static GenericTooltip ofItem(ItemStack stack, List<Component> lines) {
        return new GenericTooltip(lines, stack);
    }

    /**
     * A tooltip whose lines are already word-wrapped (via {@code Font#split}).
     * Used for {@code SHOW_TEXT} hover events, which vanilla wraps to half the
     * screen width. Wrapped tooltips never carry an icon, matching vanilla's
     * own hover-text behavior.
     */
    public static GenericTooltip ofWrapped(List<FormattedCharSequence> wrappedLines) {
        return new GenericTooltip(List.of(), null, wrappedLines);
    }

    public static GenericTooltip empty() {
        return new GenericTooltip(List.of());
    }

    @Override
    public void render(GenericGraphics genericGraphics, int mouseX, int mouseY, float partialTicks) {
        genericGraphics.renderTooltip(this, mouseX, mouseY);
    }
}
