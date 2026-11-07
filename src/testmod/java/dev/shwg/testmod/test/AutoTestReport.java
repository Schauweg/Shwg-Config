package dev.shwg.testmod.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public void fail(String checkName, Throwable t) {
        failed = true;
        lines.add("FAIL " + checkName + ": threw " + t);
    }

    public boolean hasFailed() {
        return failed;
    }

    public static Path reportDir() {
        String reportDir = System.getenv("SHWG_AUTOTEST_REPORT_DIR");
        return reportDir != null && !reportDir.isBlank() ? Path.of(reportDir) : Path.of("autotest-report");
    }

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
