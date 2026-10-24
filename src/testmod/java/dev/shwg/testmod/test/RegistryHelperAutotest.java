package dev.shwg.testmod.test;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.shwg.testmod.test.AutoTestReport.require;

/**
 * Phase 1: RegistryHelper enchantment checks on the title screen (no world loaded).
 * Called from {@link AutoTest} right before it opens the render test screen.
 * Runs on every version from 1.19 to 26.3.
 */
public final class RegistryHelperAutotest {

    private static final Identifier FORTUNE = RegistryHelper.build("minecraft", "fortune");
    private static final Identifier SILK_TOUCH = RegistryHelper.build("minecraft", "silk_touch");
    private static final Identifier MENDING = RegistryHelper.build("minecraft", "mending");
    private static final Identifier UNKNOWN = RegistryHelper.build("shwgtest", "does_not_exist");

    // Vanilla has ~38 enchantments in 1.19 and ~42 in 1.21; 30 leaves room without being meaningless.
    private static final int MIN_VANILLA_COUNT = 30;

    private static boolean ran = false;

    private RegistryHelperAutotest() {}

    /** Safe to call every tick; only runs the first time. */
    public static void runOnce() {
        if (ran) return;
        ran = true;
        run();
    }

    private static void run() {
        AutoTestReport report = new AutoTestReport("registry-helper");

        report.check("no world loaded", () ->
                require(Minecraft.getInstance().level == null,
                        "level is not null on the title screen, the rest of this report is not testing the no-world case"));

        report.check("getEnchantments() not null", () ->
                require(RegistryHelper.getEnchantments() != null, "returned null"));

        report.check("vanilla enchantments present", () -> {
            Set<Identifier> enchantments = RegistryHelper.getEnchantments();
            for (Identifier id : List.of(FORTUNE, SILK_TOUCH, MENDING)) {
                require(enchantments.contains(id), "missing " + id);
            }
        });

        report.check("plausible enchantment count", () -> {
            int count = RegistryHelper.getEnchantments().size();
            require(count >= MIN_VANILLA_COUNT, "only " + count + " enchantments, expected at least " + MIN_VANILLA_COUNT);
        });

        // The testmod adds no enchantments, so everything should be vanilla.
        // If you ever run the testmod with other mods that add enchantments, remove this check.
        report.check("only minecraft namespace", () -> {
            for (Identifier id : RegistryHelper.getEnchantments()) {
                require(id.getNamespace().equals("minecraft"), "unexpected enchantment " + id);
            }
        });

        report.check("getEnchantment(fortune) matches version", () -> {
            Enchantment fortune = RegistryHelper.getEnchantment(FORTUNE);
            /*? if >= 1.21 { */
            require(fortune == null, "expected null without a world on 1.21+ (no client registry yet)");
            /*? } else { */
            //require(fortune != null, "fortune not found in the static registry");
            /*? } */
        });

        // Not comparing against "Fortune" so the test works with any game language.
        report.check("vanilla name is translated", () -> {
            String name = RegistryHelper.enchantmentComponent(FORTUNE).getString();
            require(!name.equals(FORTUNE.toString()), "fell back to the raw id");
            require(!name.equals("enchantment.minecraft.fortune"), "translation key was not resolved");
            require(!name.isBlank(), "name is blank");
        });

        report.check("unknown id falls back to literal id", () -> {
            String name = RegistryHelper.enchantmentComponent(UNKNOWN).getString();
            require(name.equals(UNKNOWN.toString()), "expected '" + UNKNOWN + "' but got '" + name + "'");
        });

        // Sorted so the lists of two versions can be diffed directly.
        report.section("AVAILABLE ENCHANTMENTS", () -> RegistryHelper.getEnchantments().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .map(id -> String.format("%-32s %s", id, RegistryHelper.enchantmentComponent(id).getString()))
                .toList());

        report.write();
    }
}
