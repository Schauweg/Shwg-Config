package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.shwg.testmod.test.AutoTestReport.require;
import static dev.shwg.testmod.test.checks.CheckHelpers.requireTranslated;

/** {@link RegistryHelper} enchantments without a world: vanilla only, with translated names. */
public final class EnchantmentRegistryChecks implements CheckSuite {

    private static final Identifier FORTUNE = RegistryHelper.build("minecraft", "fortune");
    private static final Identifier SILK_TOUCH = RegistryHelper.build("minecraft", "silk_touch");
    private static final Identifier MENDING = RegistryHelper.build("minecraft", "mending");
    private static final Identifier UNKNOWN = RegistryHelper.build("shwgtest", "does_not_exist");

    // Vanilla has ~38 enchantments in 1.19 and ~43 in 1.21.11; 30 leaves room without being meaningless.
    private static final int MIN_VANILLA_COUNT = 30;

    @Override
    public String name() {
        return "registry-enchantments";
    }

    @Override
    public void run(AutoTestReport report) {
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

        report.check("vanilla name is translated", () ->
                requireTranslated(RegistryHelper.enchantmentComponent(FORTUNE), FORTUNE.toString()));

        report.check("unknown id falls back to literal id", () -> {
            String name = RegistryHelper.enchantmentComponent(UNKNOWN).getString();
            require(name.equals(UNKNOWN.toString()), "expected '" + UNKNOWN + "' but got '" + name + "'");
        });

        // Sorted so the lists of two versions can be diffed directly.
        report.section("AVAILABLE ENCHANTMENTS", () -> RegistryHelper.getEnchantments().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .map(id -> String.format("%-32s %s", id, RegistryHelper.enchantmentComponent(id).getString()))
                .toList());
    }
}
