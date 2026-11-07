package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.RegistryHelper;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.resources.Identifier;

import static dev.shwg.testmod.test.AutoTestReport.require;

public final class SoundRegistryChecks implements CheckSuite {

    private static final Identifier PLING = RegistryHelper.build("minecraft", "block.note_block.pling");

    // Vanilla has well over 1000 sound events on every supported version.
    private static final int MIN_VANILLA_COUNT = 800;

    @Override
    public String name() {
        return "registry-sounds";
    }

    @Override
    public void run(AutoTestReport report) {
        report.check("note block pling present", () ->
                require(RegistryHelper.getSounds().contains(PLING), "missing " + PLING));

        report.check("plausible sound count", () -> {
            int count = RegistryHelper.getSounds().size();
            require(count >= MIN_VANILLA_COUNT, "only " + count + " sounds, expected at least " + MIN_VANILLA_COUNT);
        });

        report.check("name is the plain id", () -> {
            String name = RegistryHelper.soundComponent(PLING).getString();
            require(name.equals(PLING.toString()), "expected '" + PLING + "' but got '" + name + "'");
        });

        // Sorted, so autotest.py can show which sounds each version adds or removes.
        report.section("AVAILABLE SOUNDS", () -> RegistryHelper.getSounds().stream()
                .map(Identifier::toString)
                .sorted()
                .toList());
    }
}
