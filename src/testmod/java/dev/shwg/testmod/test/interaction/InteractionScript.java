package dev.shwg.testmod.test.interaction;

import dev.shwg.testmod.test.AutoTest;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the steps of the interaction test one after another, driven by the screen's tick: an action
 * (click, key, typing), waiting a few ticks, a check, or a screenshot. Everything ends up in the report
 * {@code widget-interaction.txt}, which Gradle and autotest.py read like every other check suite.
 *
 * <p>A step that throws (e.g. a NoSuchMethodError on one Minecraft version) becomes a FAIL line, and the
 * script carries on, so one broken widget doesn't hide the results of the others.</p>
 *
 * <p>Screenshots are called {@code page-<101+>-interaction-<name>.png}: the "page-" form makes autotest.py
 * compare them across versions like the render test pages, and starting at 101 keeps them apart from those.</p>
 */
final class InteractionScript {

    static final String REPORT_NAME = "widget-interaction";
    private static final int FIRST_SCREENSHOT_NUMBER = 101;

    private final List<Runnable> steps = new ArrayList<>();
    private final AutoTestReport report = new AutoTestReport(REPORT_NAME);
    private final Runnable onDone;

    private int next;
    private int waitTicks;
    private int screenshotNumber = FIRST_SCREENSHOT_NUMBER;
    private boolean done;

    // written from the screenshot callback (which may run later on 1.21.5+), read in tick()
    private volatile boolean shotPending;
    private volatile String shotError;

    InteractionScript(Runnable onDone) {
        this.onDone = onDone;
    }

    // Building the script

    /** Runs {@code action}; if it throws, that becomes a FAIL line named {@code name}. */
    InteractionScript act(String name, Runnable action) {
        steps.add(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                t.printStackTrace();
                report.fail(name, t);
            }
        });
        return this;
    }

    /** Lets the game run {@code ticks} ticks (20 per second), so the screen renders the new state. */
    InteractionScript waitTicks(int ticks) {
        steps.add(() -> waitTicks = ticks);
        return this;
    }

    /** A PASS / FAIL line in the report, see {@link AutoTestReport#check}. */
    InteractionScript check(String name, AutoTestReport.Check check) {
        steps.add(() -> report.check(name, check));
        return this;
    }

    /** A screenshot of the last rendered frame. Put a waitTicks before it, so the new state was rendered. */
    InteractionScript screenshot(String name) {
        steps.add(() -> {
            File file = AutoTestReport.reportDir().resolve("page-" + screenshotNumber++ + "-interaction-" + name + ".png").toFile();
            shotPending = true;
            try {
                Files.createDirectories(file.getParentFile().toPath());
                AutoTest.grab(Minecraft.getInstance(), file, error -> {
                    if (error != null) shotError = name + ": " + error;
                    shotPending = false;
                });
            } catch (IOException | RuntimeException | LinkageError e) {
                e.printStackTrace();
                shotError = name + ": " + e;
                shotPending = false;
            }
        });
        return this;
    }

    // Running

    /** Called every tick by the screen. Runs steps until one has to wait, then continues on a later tick. */
    void tick() {
        if (done) return;
        if (shotError != null) {
            report.fail("screenshot " + shotError, new IllegalStateException(shotError));
            shotError = null;
        }
        if (shotPending) return;
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        while (next < steps.size()) {
            steps.get(next++).run();
            if (waitTicks > 0 || shotPending) return;
        }
        done = true;
        report.write();
        onDone.run();
    }
}
