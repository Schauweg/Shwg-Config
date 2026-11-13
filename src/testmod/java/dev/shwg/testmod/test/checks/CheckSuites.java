package dev.shwg.testmod.test.checks;


import dev.shwg.testmod.test.AutoTestReport;
import dev.shwg.testmod.test.AutoTestSelection;
import java.util.List;

/** Every {@link CheckSuite}, and running the selected ones. */
public final class CheckSuites {

    public static final List<CheckSuite> ALL = List.of(
            new EnchantmentRegistryChecks(),
            new BiomeRegistryChecks(),
            new SoundRegistryChecks(),
            new EntityRegistryChecks(),
            new SuggestionProviderChecks(),
            new SuggestionHelperChecks(),
            new MobEffectTextureChecks(),
            new ModLoaderChecks()
    );

    private CheckSuites() {}

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
