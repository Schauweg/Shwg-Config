package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
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

    private Component tooltip;
    private Component labelTooltip;
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
        this.tooltip = tooltip;
        return this;
    }

    /**
     * Sets the tooltip shown when hovering the entry's label text, independent of
     * the control's own tooltip set via {@link #tooltip(Component)}.
     */
    public EntryOptions labelTooltip(Component labelTooltip) {
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

    public Component getLabelTooltip() {
        return labelTooltip;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public Supplier<Boolean> getEnabledCondition() {
        return enabledCondition;
    }
}
