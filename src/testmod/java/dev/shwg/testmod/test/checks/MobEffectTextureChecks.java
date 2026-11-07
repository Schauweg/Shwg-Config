package dev.shwg.testmod.test.checks;

import dev.shwg.shwgconfig.render.textures.GenericSprite;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.textures.MobEffectTextures;
import dev.shwg.testmod.test.AutoTestReport;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
/*? if >= 1.21.6 { */
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/*? } */
/*? if >= 1.21.9 */
import net.minecraft.data.AtlasIds;
/*? if >= 1.19.3 { */
import net.minecraft.core.registries.BuiltInRegistries;
/*? } else { */
//import net.minecraft.core.Registry;
/*? } */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.shwg.testmod.test.AutoTestReport.require;

public final class MobEffectTextureChecks implements CheckSuite {

    // Vanilla has 33 effects in 1.19 and more in newer versions.
    private static final int MIN_VANILLA_COUNT = 30;

    @Override
    public String name() {
        return "textures-mob-effects";
    }

    @Override
    public void run(AutoTestReport report) {
        List<MobEffect> effects = registeredEffects();

        report.check("plausible effect count", () ->
                require(effects.size() >= MIN_VANILLA_COUNT, "only " + effects.size() + " effects, expected at least " + MIN_VANILLA_COUNT));

        report.check("speed: texture of the right kind", () -> {
            GenericTexture texture = MobEffectTextures.get(speed());
            require(texture != null, "no texture");
            /*? if >= 1.21.6 { */
            require(texture instanceof GenericSprite, "expected a GUI sprite on 1.21.6+, got " + texture.getClass().getSimpleName());
            /*? } else { */
            //require(!(texture instanceof GenericSprite), "expected a plain texture before 1.21.6, got a sprite");
            /*? } */
            require(texture.regionWidth() == MobEffectTextures.ICON_SIZE && texture.regionHeight() == MobEffectTextures.ICON_SIZE,
                    "size is " + texture.regionWidth() + "x" + texture.regionHeight());
        });

        report.check("cached: same instance twice", () ->
                require(MobEffectTextures.get(speed()) == MobEffectTextures.get(speed()), "texture was created again"));

        report.check("every effect has a texture", () -> {
            List<String> missing = new ArrayList<>();
            for (MobEffect effect : effects) {
                if (MobEffectTextures.get(effect) == null) missing.add(String.valueOf(MobEffectTextures.effectId(effect)));
            }
            require(missing.isEmpty(), "no texture for " + missing);
        });

        report.check("every icon file exists", () -> {
            List<String> missing = new ArrayList<>();
            for (MobEffect effect : effects) {
                if (!fileExists(effect)) missing.add(String.valueOf(MobEffectTextures.fileId(effect)));
            }
            require(missing.isEmpty(), "missing files " + missing);
        });

        /*? if >= 1.21.6 { */
        report.check("every icon is in the GUI atlas", () -> {
            List<String> missing = new ArrayList<>();
            for (MobEffect effect : effects) {
                GenericTexture texture = MobEffectTextures.get(effect);
                if (texture == null || isMissingSprite(texture.getSprite())) missing.add(String.valueOf(MobEffectTextures.effectId(effect)));
            }
            require(missing.isEmpty(), "missing texture sprite for " + missing);
        });
        /*? } */

        report.section("MOB EFFECT ICONS", () -> effects.stream()
                .sorted(Comparator.comparing(effect -> String.valueOf(MobEffectTextures.effectId(effect))))
                .map(effect -> String.format("%-32s %-7s %s",
                        MobEffectTextures.effectId(effect),
                        fileExists(effect) ? "OK" : "MISSING",
                        MobEffectTextures.fileId(effect)))
                .toList());
    }

    private static boolean fileExists(MobEffect effect) {
        Identifier file = MobEffectTextures.fileId(effect);
        return file != null && Minecraft.getInstance().getResourceManager().getResource(file).isPresent();
    }

    /*? if >= 1.21.6 { */
    private static boolean isMissingSprite(Identifier spriteId) {
        /*? if >= 1.21.9 { */
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(spriteId);
        /*? } else { */
        //TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(spriteId);
        /*? } */
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }
    /*? } */

    private static MobEffect speed() {
        for (MobEffect effect : registeredEffects()) {
            Identifier id = MobEffectTextures.effectId(effect);
            if (id != null && id.getNamespace().equals("minecraft") && id.getPath().equals("speed")) return effect;
        }
        throw new IllegalStateException("minecraft:speed is not registered");
    }

    public static List<MobEffect> registeredEffects() {
        List<MobEffect> effects = new ArrayList<>();
        /*? if >= 1.19.3 { */
        BuiltInRegistries.MOB_EFFECT.forEach(effects::add);
        /*? } else { */
        //Registry.MOB_EFFECT.forEach(effects::add);
        /*? } */
        return effects;
    }
}
