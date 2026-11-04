package dev.shwg.testmod.test;

import dev.shwg.testmod.test.checks.CheckSuite;
import dev.shwg.testmod.test.checks.CheckSuites;
import dev.shwg.testmod.test.interaction.InteractionTest;
import dev.shwg.testmod.test.render.RenderTestScreen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Unattended test run: once the game has finished loading, runs the selected {@link CheckSuite}s on the
 * title screen, then (if selected) shows every page of the {@link RenderTestScreen} and saves a
 * screenshot of each, then quits the game.
 *
 * <p>Only active when the environment variable {@code SHWG_AUTOTEST=1} is set (the Gradle autotest
 * tasks do that, see gradle/autotest.gradle.kts). Reports and screenshots go to
 * {@code SHWG_AUTOTEST_REPORT_DIR} ({@code autotest-report/<project>/} in the project root).
 * {@code SHWG_AUTOTEST_SUITES} selects what runs, see {@link AutoTestSelection}.</p>
 *
 * <p>Call {@link #start()} once from the testmod's CLIENT initialisation (on every loader).
 * Never call it on a dedicated server - this class touches client-only classes.</p>
 *
 * <p>Exit codes: 0 = all screenshots written, 1 = something failed or the run timed out.</p>
 */
public final class AutoTest {

    private static final String ENV_ENABLED = "SHWG_AUTOTEST";
    private static final String ENV_ID = "SHWG_AUTOTEST_ID";

    /**
     * The GUI size the screenshots are meant to have (window 476x270 with GUI scale 1).
     */
    private static final int EXPECTED_GUI_WIDTH = 476, EXPECTED_GUI_HEIGHT = 270;

    /**
     * Ticks (20 per second) to wait after the screen opened, before the first page is shown.
     */
    private static final int WARMUP_TICKS = 20;
    /**
     * Ticks to wait after switching page, so the page was rendered a few times before the screenshot.
     */
    private static final int SETTLE_TICKS = 6;
    /**
     * If the whole run takes longer than this, the game is killed with exit code 1.
     */
    private static final long TIMEOUT_MS = 5 * 60 * 1000L;
    /**
     * After a successful finish: how long the game gets to quit on its own before it is forced.
     */
    private static final long EXIT_GRACE_MS = 20 * 1000L;

    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static volatile boolean opened;
    private static volatile boolean finished;
    private static int stableChecks; // only touched on the game thread

    private AutoTest() {
    }

    public static boolean enabled() {
        return "1".equals(System.getenv(ENV_ENABLED));
    }

    /**
     * Call once from the client initialisation. Does nothing unless {@code SHWG_AUTOTEST=1}.
     */
    public static void start() {
        if (!enabled() || !STARTED.compareAndSet(false, true)) return;
        log("enabled, id=" + id() + ", output=" + AutoTestReport.reportDir().toAbsolutePath());
        Thread supervisor = new Thread(AutoTest::supervise, "shwg-autotest");
        supervisor.setDaemon(true);
        supervisor.start();
    }

    // Supervisor thread: waits for the game to finish loading, enforces the timeout

    private static void supervise() {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!finished) {
            if (System.currentTimeMillis() > deadline) {
                fail("timed out after " + TIMEOUT_MS / 1000 + "s");
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && !opened) {
                mc.execute(AutoTest::tryOpen); // runs on the game thread
            }
            sleep(500);
        }
        // Finished: Minecraft.stop() was called. Force the exit if the game hangs while shutting down.
        sleep(EXIT_GRACE_MS);
        log("game did not exit by itself, forcing exit");
        Runtime.getRuntime().halt(0);
    }

    /**
     * Runs on the game thread, polled twice a second until it succeeds.
     */
    private static void tryOpen() {
        if (opened) return;
        Minecraft mc = Minecraft.getInstance();

        /*~ if >=26.2 'mc.getOverlay()' -> 'mc.gui.overlay()'*/
        if (mc == null || mc.getOverlay() != null) { // overlay = still loading / reloading resources
            stableChecks = 0;
            return;
        }
        if (++stableChecks < 4) return; // ~2 seconds without overlay, let the first screen settle
        opened = true;
        prepareWindow(mc);

        // Still on the title screen with no world loaded: the state the checks need.
        // Results go to report files that Gradle checks; a failing check must not stop the screenshots.
        AutoTestSelection selection = AutoTestSelection.fromEnvironment();
        log("selection: " + selection);
        try {
            CheckSuites.run(selection);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log("checks crashed: " + e);
        }

        if (!selection.screenshots()) {
            log("no screenshots selected");
            afterScreenshots(mc, selection);
            return;
        }

        try {
            logWindow(mc);
            RenderTestScreen screen = new RenderTestScreen(currentScreen(mc));
            Run run = new Run(mc, screen);
            screen.setTickHook(run::tick);
            // GenericScreen#setScreen already contains the version switch (setScreen / setScreenAndShow)
            screen.setScreen(screen);
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail("could not open the test screen: " + e);
        }
    }

    /**
     * Windowed at the size from the Gradle task, GUI scale 1 - whatever the run directory's options.txt
     * says (e.g. fullscreen left on from development). Only changed in memory, options.txt isn't saved.
     */
    private static void prepareWindow(Minecraft mc) {
        /*~ if >=26.3 '.isFullscreen()' -> '.isExclusiveFullscreen()'*/
        if (mc.getWindow().isFullscreen()) {
            /*~ if >=26.3 '.toggleFullScreen()' -> '.setFullscreen(false)'*/
            mc.getWindow().toggleFullScreen(); // back to the windowed size from --width / --height
        }
        mc.options.fullscreen().set(false);
        if (mc.options.guiScale().get() != 1) {
            mc.options.guiScale().set(1);
            /*~ if >=26 'mc.resizeDisplay()' -> 'mc.resizeGui()'*/
            mc.resizeDisplay();
        }
    }

    // The actual run: driven by RenderTestScreen#tick, so it needs no loader specific tick event

    private static final class Run {
        private enum Phase {WARMUP, SETTLE, WAIT_FOR_FILE}

        private final Minecraft mc;
        private final RenderTestScreen screen;

        private Phase phase = Phase.WARMUP;
        private int ticks;
        private int page;

        // written from the screenshot callback (which may run later on 1.21.5+), read in tick()
        private volatile boolean shotPending;
        private volatile String shotError;

        Run(Minecraft mc, RenderTestScreen screen) {
            this.mc = mc;
            this.screen = screen;
        }

        void tick() {
            if (shotError != null) fail(shotError);

            switch (phase) {
                case WARMUP -> {
                    if (++ticks >= WARMUP_TICKS) showPage(0);
                }
                case SETTLE -> {
                    if (++ticks >= SETTLE_TICKS) {
                        capture();
                        phase = Phase.WAIT_FOR_FILE;
                    }
                }
                case WAIT_FOR_FILE -> {
                    if (shotPending) return;
                    if (page + 1 < screen.pageCount()) {
                        showPage(page + 1);
                    } else {
                        finish();
                    }
                }
            }
        }

        private void showPage(int index) {
            page = index;
            screen.setPage(index);
            ticks = 0;
            phase = Phase.SETTLE;
        }

        private void capture() {
            File file = AutoTestReport.reportDir().resolve("page-" + (page + 1) + "-" + screen.pageSlug(page) + ".png").toFile();
            shotPending = true;
            try {
                Files.createDirectories(file.getParentFile().toPath());
                grab(mc, file, error -> {
                    if (error != null) shotError = error;
                    shotPending = false;
                });
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                shotError = "screenshot of page " + (page + 1) + " failed: " + e;
                shotPending = false;
            }
        }

        private void finish() {
            log(screen.pageCount() + " screenshots in " + AutoTestReport.reportDir().toAbsolutePath());
            afterScreenshots(mc, AutoTestSelection.fromEnvironment());
        }
    }

    // Version specific parts

    /**
     * Takes a screenshot of the main render target and writes it to {@code file}.
     * {@code done} is called with {@code null} on success or an error message.
     *
     * <p>Screenshot.takeScreenshot is synchronous up to 1.21.4 and asynchronous (callback) from 1.21.5.
     * Minecraft#getMainRenderTarget moved to the GameRenderer in 26.2 - that accessor name is a guess,
     * check it against the 26.2 mappings.</p>
     */
    public static void grab(Minecraft mc, File file, Consumer<String> done) {
        /*? if >=26.2 { */
        //Object target = mc.gameRenderer.mainRenderTarget();
        /*?} else { */
        Object target = mc.getMainRenderTarget();
        /*?} */
        grabAnyVersion(target, file, done);
    }

    /**
     * Picks the screenshot method at runtime, so a jar built for one version also works on the
     * additional versions: takeScreenshot(RenderTarget) returning the image before 1.21.5,
     * takeScreenshot(RenderTarget, Consumer) from 1.21.5. Found by parameter types because the
     * method names differ at runtime (intermediary / SRG).
     */
    private static void grabAnyVersion(Object target, File file, Consumer<String> done) {
        Consumer<NativeImage> callback = image -> save(image, file, done);
        try {
            for (Method method : Screenshot.class.getMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[0].isInstance(target) && params[1] == Consumer.class) {
                    method.invoke(null, target, callback);
                    return;
                }
                if (params.length == 1 && params[0].isInstance(target) && method.getReturnType() == NativeImage.class) {
                    callback.accept((NativeImage) method.invoke(null, target));
                    return;
                }
            }
            done.accept("no Screenshot.takeScreenshot method found for " + target.getClass().getName());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            e.printStackTrace();
            done.accept("screenshot failed: " + e);
        }
    }

    private static void save(NativeImage image, File file, Consumer<String> done) {
        String error = null;
        try (image) {
            image.writeToFile(file); // if this does not compile on a version, use file.toPath() instead
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            error = "could not write " + file + ": " + e;
        }
        done.accept(error);
    }

    private static Screen currentScreen(Minecraft mc) {
        /*? if >=26.2 { */
        //return mc.gui.screen();
        /*?} else { */
        return mc.screen;
        /*?} */
    }

    // Helpers

    private static String id() {
        String id = System.getenv(ENV_ID);
        return id == null || id.isBlank() ? "unknown" : id;
    }


    private static void logWindow(Minecraft mc) {
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        log("window " + mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight()
                + ", GUI " + guiWidth + "x" + guiHeight + " (scale " + mc.getWindow().getGuiScale() + ")");
        if (guiWidth != EXPECTED_GUI_WIDTH || guiHeight != EXPECTED_GUI_HEIGHT) {
            log("WARNING: expected a " + EXPECTED_GUI_WIDTH + "x" + EXPECTED_GUI_HEIGHT
                    + " GUI (476x270 window with GUI scale 1) - screenshots will not be comparable");
        }
    }

    private static void log(String message) {
        System.out.println("[autotest] " + message);
    }

    private static void fail(String reason) {
        System.err.println("[autotest] FAILED: " + reason);
        System.err.flush();
        Runtime.getRuntime().halt(1);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void markFinished() {
        try {
            Path dir = AutoTestReport.reportDir();
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("FINISHED"), "ok\n");
        } catch (IOException e) {
            e.printStackTrace();
            log("could not write the FINISHED marker: " + e);
        }
    }

    /** After the screenshots (or instead of them): the interaction test if selected, then the end of the run. */
    private static void afterScreenshots(Minecraft mc, AutoTestSelection selection) {
        if (selection.interaction()) {
            log("interaction test");
            InteractionTest.start(currentScreen(mc), () -> complete("done"));
        } else {
            complete("done");
        }
    }

    /** Ends the run normally: the FINISHED marker, then quit. */
    private static void complete(String message) {
        log(message);
        markFinished();
        finished = true;
        Minecraft.getInstance().stop();
    }
}
