package dev.shwg.testmod.test;

import dev.shwg.testmod.test.checks.CheckSuite;
import dev.shwg.testmod.test.interaction.InteractionTest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

    public boolean includes(String suiteName) {
        return everything() || tokens.contains("checks") || tokens.contains(suiteName);
    }

    public boolean screenshots() {
        return everything() || tokens.contains("screenshots");
    }

    public boolean interaction() {
        return everything() || tokens.contains("interaction");
    }

    @Override
    public String toString() {
        return everything() ? "everything" : String.join(",", tokens);
    }
}
