package dev.shwg.testmod.test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What an autotest run does, from the environment variable {@code SHWG_AUTOTEST_SUITES}
 * (set by gradle/autotest.gradle.kts): a comma separated list of
 * <ul>
 *     <li>{@code checks} - every {@link CheckSuite},</li>
 *     <li>{@code screenshots} - the render test screenshots,</li>
 *     <li>suite names, e.g. {@code registry-biomes}.</li>
 * </ul>
 * Empty or not set: everything.
 */
public record AutoTestSelection(Set<String> tokens) {

    private static final String ENV_SUITES = "SHWG_AUTOTEST_SUITES";

    public static AutoTestSelection fromEnvironment() {
        String value = System.getenv(ENV_SUITES);
        if (value == null || value.isBlank()) return new AutoTestSelection(Set.of());
        return new AutoTestSelection(Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toUnmodifiableSet()));
    }

    private boolean everything() {
        return tokens.isEmpty();
    }

    /** Whether the check suite called {@code suiteName} runs. */
    public boolean includes(String suiteName) {
        return everything() || tokens.contains("checks") || tokens.contains(suiteName);
    }

    /** Whether the render test screenshots are taken. */
    public boolean screenshots() {
        return everything() || tokens.contains("screenshots");
    }

    @Override
    public String toString() {
        return everything() ? "everything" : String.join(",", tokens);
    }
}
