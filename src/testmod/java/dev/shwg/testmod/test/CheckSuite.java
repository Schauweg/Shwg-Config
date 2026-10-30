package dev.shwg.testmod.test;

/**
 * A group of automated checks, run on the title screen (no world loaded) during an autotest run.
 * Each suite writes its own report, {@code <name>.txt}, which Gradle checks afterwards.
 * Register new suites in {@link CheckSuites#ALL}.
 */
public interface CheckSuite {

    /**
     * Report file name and the name used to select the suite
     * ({@code -Pautotest.suites=registry-biomes}), e.g. {@code "registry-biomes"}.
     */
    String name();

    /** Runs every check with {@code report.check(...)}; anything thrown inside a check becomes a FAIL line. */
    void run(AutoTestReport report);
}
