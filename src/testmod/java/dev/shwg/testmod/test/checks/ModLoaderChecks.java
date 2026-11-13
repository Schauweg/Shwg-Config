package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.ModLoaderHelper;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.client.Minecraft;

import java.util.List;

import static dev.shwg.testmod.test.AutoTestReport.require;

/**
 * ModLoaderHelper: isModPresentEarly during mixin loading (recorded by {@link EarlyModPresence}) and on the
 * title screen, and isModLoaded on the title screen, both against the expected answer for every mod id.
 * Plus the actual use case: whether the test mixins that TestMixinPlugin decided with isModPresentEarly
 * really got applied or skipped (see {@link EarlyMixinMarkers}).
 */
public final class ModLoaderChecks implements CheckSuite {

    @Override
    public String name() {
        return "mod-loader";
    }

    @Override
    public void run(AutoTestReport report) {
        // Without this, every "early" check below would fail with a confusing "was null".
        report.check("early: the mixin plugin recorded results", () ->
                require(EarlyModPresence.ran(), "nothing recorded - did TestMixinPlugin.shouldApplyMixin run?"));

        // Object, so the compiler doesn't reject the check as impossible: the interfaces only exist through the mixins.
        Object minecraft = Minecraft.getInstance();
        report.check("mixin: applied when the mod is present (ModPresentMixin)", () ->
                require(minecraft instanceof EarlyMixinMarkers.Present, "ModPresentMixin was not applied"));
        report.check("mixin: skipped when the mod is absent (ModAbsentMixin)", () ->
                require(!(minecraft instanceof EarlyMixinMarkers.Absent), "ModAbsentMixin was applied anyway"));

        EarlyModPresence.EXPECTED.forEach((id, expected) -> {
            report.check("early: isModPresentEarly(" + id + ") is " + expected, () -> {
                String result = EarlyModPresence.earlyResult(id);
                require(String.valueOf(expected).equals(result), "was " + result);
            });
            report.check("title screen: isModPresentEarly(" + id + ") is " + expected, () -> {
                boolean result = ModLoaderHelper.isModPresentEarly(id);
                require(result == expected, "was " + result);
            });
            report.check("title screen: isModLoaded(" + id + ") is " + expected, () -> {
                boolean result = ModLoaderHelper.isModLoaded(id);
                require(result == expected, "was " + result);
            });
        });

        // Fabric's loader exists from the start; Forge / NeoForge only create ModList after mixin loading.
        // That's the reason isModPresentEarly exists. If this ever changes, the check says so.
        boolean lateApiExpected = EarlyModPresence.FABRIC;
        report.check("isModLoaded during mixin loading: " + (lateApiExpected ? "works" : "not available yet"), () -> {
            String result = EarlyModPresence.lateApiResult();
            require(result.startsWith("works") == lateApiExpected, result);
        });
    }
}
