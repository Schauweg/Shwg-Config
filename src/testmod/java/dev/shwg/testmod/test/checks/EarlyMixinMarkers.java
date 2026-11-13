package dev.shwg.testmod.test.checks;

/**
 * Marker interfaces the two test mixins add to Minecraft, so ModLoaderChecks can see which of them got applied.
 * They live outside the mixin package because Mixin doesn't allow normal code to use classes from it.
 */
public final class EarlyMixinMarkers {

    /** Added by ModPresentMixin: expected on Minecraft. */
    public interface Present {
    }

    /** Added by ModAbsentMixin: must not be on Minecraft. */
    public interface Absent {
    }

    private EarlyMixinMarkers() {
    }
}
