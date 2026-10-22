package dev.shwg.testmod.test;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Unattended run of the {@link RenderTestScreen}: opens it once the game has finished loading, shows
 * every page, saves a screenshot of each and quits the game.
 *
 * <p>Only active when the environment variable {@code SHWG_AUTOTEST=1} is set (the Gradle autotest
 * tasks do that). Screenshots go to {@code autotest/<SHWG_AUTOTEST_ID>/page-N-name.png}, relative to
 * the game's working directory (the run directory in dev). The id is the Gradle project name,
 * e.g. {@code 1.21.1-fabric}.</p>
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
        log("enabled, id=" + id() + ", output=" + outputDir().toAbsolutePath());
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
            File file = outputDir().resolve("page-" + (page + 1) + "-" + screen.pageSlug(page) + ".png").toFile();
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
            log("done: " + screen.pageCount() + " screenshots in " + outputDir().toAbsolutePath());
            finished = true;
            mc.stop();
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
    private static void grab(Minecraft mc, File file, Consumer<String> done) {
        /*? if >=26.2 { */
        //Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> save(image, file, done));
        /*?} else if >=1.21.5 { */
        Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> save(image, file, done));
        /*?} else { */
        //save(Screenshot.takeScreenshot(mc.getMainRenderTarget()), file, done);
        /*?} */
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

    private static Path outputDir() {
        return Paths.get("autotest", id());
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
}
