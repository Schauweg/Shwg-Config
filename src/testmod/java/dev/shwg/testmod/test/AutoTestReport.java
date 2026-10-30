package dev.shwg.testmod.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects PASS/FAIL results for one test group and writes them to
 * {@code SHWG_AUTOTEST_REPORT_DIR/<name>.txt}. The first line is always
 * {@code RESULT PASS} or {@code RESULT FAIL} so Gradle can check it cheaply.
 */
public final class AutoTestReport {

    @FunctionalInterface
    public interface Check {
        void run() throws Exception;
    }

    private final String name;
    private final List<String> lines = new ArrayList<>();
    private final List<String> sections = new ArrayList<>();
    private boolean failed = false;

    public AutoTestReport(String name) {
        this.name = name;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Runs one check. Catches Throwable on purpose: a wrong Stonecutter branch
     * usually shows up as NoSuchMethodError / NoSuchFieldError at runtime,
     * and that should be a FAIL line in the report, not a crashed game.
     */
    public void check(String checkName, Check check) {
        try {
            check.run();
            lines.add("PASS " + checkName);
        } catch (AssertionError e) {
            failed = true;
            lines.add("FAIL " + checkName + ": " + e.getMessage());
        } catch (Throwable t) {
            failed = true;
            lines.add("FAIL " + checkName + ": threw " + t);
        }
    }

    /**
     * Adds an informational block after the check results, e.g. a list of what was found.
     * Never affects PASS/FAIL. If building the content throws, the error is written instead.
     */
    public void section(String title, SectionContent content) {
        sections.add("");
        try {
            List<String> body = content.lines();
            sections.add(title + " (" + body.size() + ")");
            for (String line : body) sections.add("  " + line);
        } catch (Throwable t) {
            sections.add(title + " (could not be listed: " + t + ")");
        }
    }

    @FunctionalInterface
    public interface SectionContent {
        List<String> lines() throws Exception;
    }

    /** Records a failure that happened outside {@link #check}, e.g. a suite crashing between checks. */
    public void fail(String checkName, Throwable t) {
        failed = true;
        lines.add("FAIL " + checkName + ": threw " + t);
    }

    public boolean hasFailed() {
        return failed;
    }

    /**
     * Where reports and screenshots go: {@code autotest-report/<project>/} in the project root, set by
     * gradle/autotest.gradle.kts. The fallback (relative to the run directory) is only for runs started
     * some other way.
     */
    public static Path reportDir() {
        String reportDir = System.getenv("SHWG_AUTOTEST_REPORT_DIR");
        return reportDir != null && !reportDir.isBlank() ? Path.of(reportDir) : Path.of("autotest-report");
    }

    /**
     * Writes plain data next to the reports, e.g. a list of IDs other Gradle tasks read.
     * Use an extension other than .txt - every .txt file there is checked as a report.
     */
    public static void writeData(String fileName, List<String> lines) {
        Path dir = reportDir();
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), lines);
        } catch (IOException e) {
            System.err.println("[autotest] could not write " + fileName + ": " + e);
        }
    }

    public void write() {
        List<String> out = new ArrayList<>();
        out.add("RESULT " + (failed ? "FAIL" : "PASS"));
        out.addAll(lines);
        out.addAll(sections);

        Path dir = reportDir();
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(name + ".txt"), out);
            System.out.println("[autotest] " + name + ": " + (failed ? "FAIL" : "PASS") + " -> " + dir.toAbsolutePath());
        } catch (IOException e) {
            // No report file means the Gradle check fails too, so this can't pass silently.
            System.err.println("[autotest] could not write report " + name + ": " + e);
        }
    }
}
