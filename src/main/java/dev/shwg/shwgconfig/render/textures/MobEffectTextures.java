package dev.shwg.shwgconfig.render.textures;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;
/*? if >= 1.19.3 { */
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.core.Registry;
/*? } */

import java.util.HashMap;
import java.util.Map;

/**
 * Lookup table from a {@link MobEffect} to the texture of its icon, ready for
 * {@link dev.shwg.shwgconfig.render.GenericGraphics#drawTexture(GenericTexture, int, int, int, int)}.
 *
 * <ul>
 *     <li>1.21.6+: a GUI sprite, {@code <namespace>:mob_effect/<path>} - the effect atlas was merged into the GUI atlas,
 *     which collects {@code textures/mob_effect/} under that prefix.</li>
 *     <li>Before 1.21.6: the standalone 18x18 file {@code <namespace>:textures/mob_effect/<path>.png}.</li>
 * </ul>
 *
 * Icons are looked up by the effect's ID, so modded effects work as long as they follow the vanilla layout.
 * The textures are created once per effect and cached; they reference textures by ID, so resource pack
 * reloads don't invalidate them.
 */
public final class MobEffectTextures {

    /** Size of the vanilla effect icons. */
    public static final int ICON_SIZE = 18;

    private static final Map<MobEffect, GenericTexture> CACHE = new HashMap<>();

    private MobEffectTextures() {}

    /** The icon texture of {@code effect}, or {@code null} if the effect isn't registered. */
    public static @Nullable GenericTexture get(MobEffect effect) {
        // computeIfAbsent doesn't store null results, so an unregistered effect is simply looked up again
        return CACHE.computeIfAbsent(effect, MobEffectTextures::create);
    }

    private static @Nullable GenericTexture create(MobEffect effect) {
        Identifier id = effectId(effect);
        if (id == null) return null;

        /*? if >= 1.21.6 { */
        // textures/mob_effect/<path>.png, stitched into the GUI atlas as mob_effect/<path>, drawn with blitSprite
        return GenericSprite.plain(id.getNamespace(), "mob_effect/" + id.getPath(), ICON_SIZE, ICON_SIZE);
        /*? } else { */
        //return GenericTexture.customTexture(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png", ICON_SIZE, ICON_SIZE);
        /*? } */
    }

    /**
     * The PNG file the icon is read from, e.g. {@code minecraft:textures/mob_effect/speed.png}, or {@code null}
     * if the effect isn't registered. For checks: whether a resource pack or mod actually provides it.
     *
     * <p>The file stays in {@code textures/mob_effect/} on every version. From 1.21.6 on the GUI atlas
     * collects that folder under the prefix {@code mob_effect/}, which is where the sprite ID comes from.</p>
     */
    public static @Nullable Identifier fileId(MobEffect effect) {
        Identifier id = effectId(effect);
        if (id == null) return null;
        return RegistryHelper.build(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png");
    }

    /** The registry ID of {@code effect}, or {@code null} if it isn't registered. */
    public static @Nullable Identifier effectId(MobEffect effect) {
        /*? if >= 1.19.3 { */
        return BuiltInRegistries.MOB_EFFECT.getKey(effect);
        /*? } else { */
        //return Registry.MOB_EFFECT.getKey(effect);
        /*? } */
    }
}
