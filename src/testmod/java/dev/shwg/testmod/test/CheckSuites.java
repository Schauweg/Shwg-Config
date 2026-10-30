package dev.shwg.testmod.test;

import dev.shwg.testmod.test.checks.BiomeRegistryChecks;
import dev.shwg.testmod.test.checks.EnchantmentRegistryChecks;
import dev.shwg.testmod.test.checks.EntityRegistryChecks;
import dev.shwg.testmod.test.checks.SoundRegistryChecks;
import dev.shwg.testmod.test.checks.SuggestionHelperChecks;
import dev.shwg.testmod.test.checks.SuggestionProviderChecks;

import java.util.List;

/** Every {@link CheckSuite}, and running the selected ones. */
public final class CheckSuites {

    public static final List<CheckSuite> ALL = List.of(
            new EnchantmentRegistryChecks(),
            new BiomeRegistryChecks(),
            new SoundRegistryChecks(),
            new EntityRegistryChecks(),
            new SuggestionProviderChecks(),
            new SuggestionHelperChecks()
    );

    private CheckSuites() {}

    /** Runs every suite {@code selection} includes, each into its own report. */
    public static void run(AutoTestSelection selection) {
        for (CheckSuite suite : ALL) {
            if (!selection.includes(suite.name())) continue;
            AutoTestReport report = new AutoTestReport(suite.name());
            try {
                suite.run(report);
            } catch (Throwable t) {
                // A suite that crashes outside report.check(...) still gets a failing report.
                report.fail("suite ran to the end", t);
            }
            report.write();
        }
    }
}
