package dev.shwg.testmod.mixin;

import dev.shwg.shwgconfig.util.ModLoaderHelper;
import dev.shwg.testmod.test.checks.EarlyModPresence;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin config plugin of the testmod. Mixin calls {@link #shouldApplyMixin} while it prepares the mixin configs,
 * before any mod is constructed - the typical place to apply a mixin only when another mod is present.
 *
 * <p>Two test mixins are decided here with ModLoaderHelper.isModPresentEarly, exactly like a real mod would:
 * {@link ModPresentMixin} only if shwgconfig is present (it is), {@link ModAbsentMixin} only if a mod that
 * doesn't exist is present (it isn't). ModLoaderChecks then checks which of the two really got applied.</p>
 */
public class TestMixinPlugin implements IMixinConfigPlugin {

    static final String PRESENT_MOD = "shwgconfig";
    static final String ABSENT_MOD = "shwgtest_does_not_exist";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        EarlyModPresence.record();
        if (mixinClassName.endsWith(".ModPresentMixin")) return isPresent(PRESENT_MOD);
        if (mixinClassName.endsWith(".ModAbsentMixin")) return isPresent(ABSENT_MOD);
        return true;
    }

    /** Never throws: an exception here would stop the game from starting instead of failing a check. */
    private static boolean isPresent(String modId) {
        try {
            return ModLoaderHelper.isModPresentEarly(modId);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
