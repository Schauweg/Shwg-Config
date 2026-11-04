package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.resources.Identifier;

import java.util.Comparator;

import static dev.shwg.testmod.test.AutoTestReport.require;
import static dev.shwg.testmod.test.checks.CheckHelpers.requireTranslated;

/** {@link RegistryHelper} entity types, and the spawn egg naming the entity suggestions use for icons. */
public final class EntityRegistryChecks implements CheckSuite {

    private static final Identifier PIG = RegistryHelper.build("minecraft", "pig");

    // Vanilla has ~110 entity types in 1.19 and more since.
    private static final int MIN_VANILLA_COUNT = 100;

    @Override
    public String name() {
        return "registry-entities";
    }

    @Override
    public void run(AutoTestReport report) {
        report.check("pig present", () ->
                require(RegistryHelper.getEntityTypes().contains(PIG), "missing " + PIG));

        report.check("plausible entity type count", () -> {
            int count = RegistryHelper.getEntityTypes().size();
            require(count >= MIN_VANILLA_COUNT, "only " + count + " entity types, expected at least " + MIN_VANILLA_COUNT);
        });

        report.check("name is translated", () ->
                requireTranslated(RegistryHelper.entityComponent(PIG), PIG.toString()));

        report.check("spawn egg naming convention (pig -> pig_spawn_egg)", () ->
                require(!DisplayItemStack.getStackSimple(RegistryHelper.build("minecraft", "pig_spawn_egg")).isEmpty(),
                        "minecraft:pig_spawn_egg doesn't exist - entity suggestion icons won't show"));

        report.section("AVAILABLE ENTITY TYPES", () -> RegistryHelper.getEntityTypes().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .map(id -> String.format("%-40s %s", id, RegistryHelper.entityComponent(id).getString()))
                .toList());
    }
}
