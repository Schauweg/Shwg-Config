package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Optional per-entry configuration - tooltip, label tooltip, and a condition for
 * whether the entry is enabled. Pass to any {@link CategoryBuilder} {@code addX(...)}
 * overload that accepts one; use {@link #NONE} (the default) for an entry with none
 * of these.
 *
 * <pre>{@code
 * EntryOptions.create()
 *     .tooltip(Component.literal("Shown when hovering the control"))
 *     .enabledWhen(otherValue::getValue)
 * }</pre>
 */
public final class EntryOptions {

    /**
     * Shared instance for an entry with no tooltip, label tooltip, or enabled condition.
     */
    public static final EntryOptions NONE = new EntryOptions();

    private GenericTooltip tooltip;
    private GenericTooltip labelTooltip;
    private Supplier<Boolean> enabledCondition;

    /**
     * Starts building a new set of options.
     */
    public static EntryOptions create() {
        return new EntryOptions();
    }

    /**
     * Sets the tooltip shown when hovering the entry's control (the widget itself).
     */
    public EntryOptions tooltip(Component tooltip) {
        return tooltip(tooltip != null ? GenericTooltip.of(tooltip) : null);
    }

    /**
     * Sets the tooltip shown when hovering the entry's control, built with
     * {@link GenericTooltip#builder()} for bullet points, several lines, etc.
     */
    public EntryOptions tooltip(GenericTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    /**
     * Sets the tooltip shown when hovering the entry's label text, independent of
     * the control's own tooltip set via {@link #tooltip(Component)}.
     */
    public EntryOptions labelTooltip(Component labelTooltip) {
        return labelTooltip(labelTooltip != null ? GenericTooltip.of(labelTooltip) : null);
    }

    /**
     * Sets the tooltip shown when hovering the entry's label text, built with
     * {@link GenericTooltip#builder()} for bullet points, several lines, etc.
     */
    public EntryOptions labelTooltip(GenericTooltip labelTooltip) {
        this.labelTooltip = labelTooltip;
        return this;
    }

    /**
     * Sets a condition controlling whether this entry is enabled. Re-evaluated
     * every frame; the entry is disabled (grayed out, non-interactive) whenever
     * this returns {@code false}.
     */
    public EntryOptions enabledWhen(Supplier<Boolean> condition) {
        this.enabledCondition = condition;
        return this;
    }

    public GenericTooltip getLabelTooltip() {
        return labelTooltip;
    }

    public GenericTooltip getTooltip() {
        return tooltip;
    }

    public Supplier<Boolean> getEnabledCondition() {
        return enabledCondition;
    }
}
