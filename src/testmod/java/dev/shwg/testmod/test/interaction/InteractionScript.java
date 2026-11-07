package dev.shwg.testmod.test.interaction;

import dev.shwg.testmod.test.AutoTest;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

    InteractionScript waitTicks(int ticks) {
        steps.add(() -> waitTicks = ticks);
        return this;
    }

    InteractionScript check(String name, AutoTestReport.Check check) {
        steps.add(() -> report.check(name, check));
        return this;
    }

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
