package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.util.ModLoaderHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Records what ModLoaderHelper answers during mixin loading (called from TestMixinPlugin.shouldApplyMixin), so
 * {@link ModLoaderChecks} can check it later on the title screen.
 *
 * <p>The results go into system properties, not static fields: on Forge and NeoForge a mixin plugin can be
 * loaded by a different class loader than the later game code, so a static field could exist twice. System
 * properties belong to the whole JVM. This class imports no Minecraft classes, because it runs that early.</p>
 */
public final class EarlyModPresence {

    /*~ if >=1.19.2 '"fabric"' -> '"fabric-api"'*/
    static final String FABRIC_API_ID = "fabric-api";
    private static final String PREFIX = "shwgtest.early.";

    /*? if fabric {*/
    static final String LOADER_ID = "fabricloader";
    static final boolean FABRIC = true;
    /*?} else if neoforge {*/
    /*static final String LOADER_ID = "neoforge";
    static final boolean FABRIC = false;
    *//*?} else if forge {*/
    /*static final String LOADER_ID = "forge";
    static final boolean FABRIC = false;
    *//*?}*/

    /** The mod ids that get checked, with whether each one is expected to be present. */
    static final Map<String, Boolean> EXPECTED = new LinkedHashMap<>();

    static {
        EXPECTED.put("shwgconfig", true);
        EXPECTED.put("shwgconfigtestmod", true);
        EXPECTED.put("minecraft", true);
        EXPECTED.put(LOADER_ID, true);
        EXPECTED.put(FABRIC_API_ID, FABRIC);
        EXPECTED.put("shwgtest_does_not_exist", false);
    }

    private EarlyModPresence() {
    }

    /**
     * Called from TestMixinPlugin.shouldApplyMixin, which runs once per mixin: only the first call records.
     * Never throws: a broken check must not stop the game from starting.
     */
    public static void record() {
        if (ran()) return;
        try {
            System.setProperty(PREFIX + "ran", "true");
            for (String id : EXPECTED.keySet()) {
                String result;
                try {
                    result = String.valueOf(ModLoaderHelper.isModPresentEarly(id));
                } catch (Throwable t) {
                    result = "error: " + t;
                }
                System.setProperty(PREFIX + "present." + id, result);
            }

            // Only documented, not checked: on Forge / NeoForge the ModList behind isModLoaded doesn't exist yet.
            String late;
            try {
                late = "works, isModLoaded(\"minecraft\") = " + ModLoaderHelper.isModLoaded("minecraft");
            } catch (Throwable t) {
                late = "not available yet: " + t;
            }
            System.setProperty(PREFIX + "lateApi", late);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    static boolean ran() {
        return "true".equals(System.getProperty(PREFIX + "ran"));
    }

    /** "true", "false", "error: ..." or null if nothing was recorded for this id. */
    static String earlyResult(String id) {
        return System.getProperty(PREFIX + "present." + id);
    }

    static String lateApiResult() {
        return System.getProperty(PREFIX + "lateApi", "not recorded");
    }
}
