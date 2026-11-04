package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.resources.Identifier;

import java.util.Comparator;

import static dev.shwg.testmod.test.AutoTestReport.require;
import static dev.shwg.testmod.test.checks.CheckHelpers.requireTranslated;

/** {@link RegistryHelper} biomes without a world: the vanilla biome constants, with translated names. */
public final class BiomeRegistryChecks implements CheckSuite {

    private static final Identifier PLAINS = RegistryHelper.build("minecraft", "plains");
    private static final Identifier UNKNOWN = RegistryHelper.build("shwgtest", "does_not_exist");

    // Vanilla has ~60 biomes on every supported version.
    private static final int MIN_VANILLA_COUNT = 50;

    @Override
    public String name() {
        return "registry-biomes";
    }

    @Override
    public void run(AutoTestReport report) {
        report.check("plains present", () ->
                require(RegistryHelper.getBiomes().contains(PLAINS), "missing " + PLAINS));

        report.check("plausible biome count", () -> {
            int count = RegistryHelper.getBiomes().size();
            require(count >= MIN_VANILLA_COUNT, "only " + count + " biomes, expected at least " + MIN_VANILLA_COUNT);
        });

        report.check("name is translated", () ->
                requireTranslated(RegistryHelper.biomeComponent(PLAINS), PLAINS.toString()));

        report.check("unknown id falls back to literal id", () -> {
            String name = RegistryHelper.biomeComponent(UNKNOWN).getString();
            require(name.equals(UNKNOWN.toString()), "expected '" + UNKNOWN + "' but got '" + name + "'");
        });

        report.section("AVAILABLE BIOMES", () -> RegistryHelper.getBiomes().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .map(id -> String.format("%-40s %s", id, RegistryHelper.biomeComponent(id).getString()))
                .toList());
    }
}
