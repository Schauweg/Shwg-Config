package dev.shwg.shwgconfig.api.hud;

import com.mojang.blaze3d.platform.Window;
import dev.shwg.shwgconfig.ShwgConfig;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.*;

/*? if fabric { */
/*? if >=1.21.6 { */
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
/*?} else if >=1.21.5 {*/
/*import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
*//*?} else {*/
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*//*?} */
/*?} */

/*? if neoforge { */
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
*//*? } */

/*? if forge { */
/*import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
/^? if 1.21.5 || >= 1.21.9 { ^/
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
/^?} ^/
/^? if < 1.20.5 { ^/
/^import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
^//^?} ^/
/^? if 1.20.6 || (>= 1.21 && < 1.21.5) || (>= 1.21.6 && < 1.21.9) { ^/
/^import net.minecraft.client.gui.GuiGraphics;
 ^//^?} ^/
*//*?} */

public class HudRenderDispatcher {

    private static final Map<Identifier, RegisteredHudLayer> HUD_LAYERS = new LinkedHashMap<>();

    public static void register(Identifier id, HudLayer layer, HudOptions options) {
        if (HUD_LAYERS.containsKey(id)) {
            throw new IllegalStateException("Hud layer " + id + " is already registered!");
        }
        HUD_LAYERS.put(id, new  RegisteredHudLayer(layer, options));

    }

    public static void register(Identifier id, HudLayer layer) {
        register(id, layer, HudOptions.create());
    }

    public static void register(String namespace, String path, HudLayer layer, HudOptions options) {
        register(RegistryHelper.build(namespace, path), layer, options);
    }

    public static void register(String namespace, String path, HudLayer layer){
        register(namespace, path, layer, HudOptions.create());
    }

    private static void renderAll(GenericGraphics graphics, float partialTick) {
        /*~ if >=26.2 '.options.hideGui' -> '.gui.hud.isHidden()' */
        if (Minecraft.getInstance().options.hideGui) return;
        Window window = Minecraft.getInstance().getWindow();
        int standardScale = (int) window.getGuiScale();
        int hudScale = hudScale();
        boolean scaled = hudScale > 0 && hudScale < standardScale;

        boolean debugScreenOpen = isDebugScreenOpen();
        graphics.pushPose();
        int width;
        int height;
        if (scaled) {
            graphics.scale((float) hudScale / standardScale, (float) hudScale / standardScale);
            width = window.getWidth() / hudScale;
            height = window.getHeight() / hudScale;
        } else {
            width = window.getGuiScaledWidth();
            height = window.getGuiScaledHeight();
        }
        HudRenderContext context = new HudRenderContext(graphics, width, height, partialTick, debugScreenOpen);
        Iterator<Map.Entry<Identifier, RegisteredHudLayer>> iterator = HUD_LAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Identifier, RegisteredHudLayer> entry = iterator.next();
            if (debugScreenOpen && entry.getValue().options.isHiddenWithDebugScreen()) continue;
            graphics.pushPose();
            try {
                entry.getValue().layer.render(context);
            } catch (Exception e) {
                ShwgConfig.LOGGER.error("HUD layer {} threw an exception and has been disabled", entry.getKey(), e);
                iterator.remove();
            } finally {
                graphics.popPose();
            }
        }
        graphics.popPose();
    }

    /*? if fabric { */
    /*? if >=1.21.6 { */
    public static void fabricInit() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, RegistryHelper.build(ShwgConfig.MOD_ID, "hud"), (context, tickCounter) -> {
            GenericGraphics graphics = ((GraphicsAccessor)context).shwgConfigs$getGraphics();
            renderAll(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
        });
    }
    /*?} else if >=1.21.5 { */
    /*public static void fabricInit() {
        HudLayerRegistrationCallback.EVENT.register((event) -> event.attachLayerAfter(IdentifiedLayer.SUBTITLES, RegistryHelper.build(ShwgConfig.MOD_ID, "hud"),
                (context, tickCounter) -> {
                    GenericGraphics graphics = ((GraphicsAccessor)context).shwgConfigs$getGraphics();
                    renderAll(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
                }));
    }
    *//*?} else if >= 1.20 { */
    /*public static void fabricInit() {
        /^~ if >= 1.21 'partialTick' -> 'tickCounter' ^/
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            GenericGraphics graphics = ((GraphicsAccessor) context).shwgConfigs$getGraphics();
            graphics.pushPose();
            /^~ if >= 1.20.6 '250' -> '650' ^/
            graphics.translate(0, 0, 650);
            /^~ if >= 1.21 'partialTick' -> 'tickCounter.getGameTimeDeltaPartialTick(false)'^/
            renderAll(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
            graphics.popPose();
        });
    }
    *//*?} else {*/
    /*public static void fabricInit() {
        HudRenderCallback.EVENT.register((poseStack, partialTick) -> {
            GenericGraphics graphics = ((GraphicsAccessor) poseStack).shwgConfigs$getGraphics();
            renderAll(graphics, partialTick);
        });
    }
    *//*?} */
    /*?} */

    /*? if neoforge { */
    /*public static void neoForgeInit(IEventBus modEventBus) {
        modEventBus.addListener(HudRenderDispatcher::onRegisterGuiLayers);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        /^~ if >= 1.21 'partialTick' -> 'tickCounter' ^/
        event.registerAbove(VanillaGuiLayers.SUBTITLE_OVERLAY, RegistryHelper.build(ShwgConfig.MOD_ID, "hud"), (context, tickCounter) -> {
            GenericGraphics graphics = ((GraphicsAccessor) context).shwgConfigs$getGraphics();
            /^~ if >= 1.21 'partialTick' -> 'tickCounter.getGameTimeDeltaPartialTick(false)'^/
            renderAll(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
        });
    }
    *//*?} */

    /*? if forge { */
    /*public static void forgeInit(FMLJavaModLoadingContext context) {
        /^? if >= 1.21.9 { ^/
        AddGuiOverlayLayersEvent.BUS.addListener(HudRenderDispatcher::onAddGuiLayers);
         /^?} else if 1.21.5 { ^/
        /^context.getModEventBus().addListener(HudRenderDispatcher::onAddGuiLayers);
        ^//^?} else if < 1.20.5 { ^/
        /^context.getModEventBus().addListener(HudRenderDispatcher::onRegisterOverlays);
         ^//^?} ^/
    }

    /^? if 1.21.5 || >= 1.21.9 { ^/
    private static void onAddGuiLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().addAbove(ForgeLayeredDraw.POST_SLEEP_STACK, RegistryHelper.build(ShwgConfig.MOD_ID, "hud"), ForgeLayeredDraw.SUBTITLE_OVERLAY,
                (guiGraphics, deltaTracker) -> {
                    GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
                    renderAll(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
                });
    }
    /^?} ^/

    /^? if 1.20.6 || (>= 1.21 && < 1.21.5) || (>= 1.21.6 && < 1.21.9) { ^/
    /^public static void renderFromMixin(GuiGraphics guiGraphics, float partialTick) {
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        graphics.pushPose();
        /^? if < 1.21.6 ^/
        //graphics.translate(0, 0, 650);
        renderAll(graphics, partialTick);
        graphics.popPose();
    }
    ^//^?} ^/

    /^? if < 1.20.5 { ^/
    /^private static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.SUBTITLES.id(), "hud", (forgeGui, context, partialTick, screenWidth, screenHeight) -> {
            GenericGraphics graphics = ((GraphicsAccessor) context).shwgConfigs$getGraphics();
            graphics.pushPose();
            graphics.translate(0, 0, 250);
            renderAll(graphics, partialTick);
            graphics.popPose();
        });
    }
    ^//^?} ^/
    *//*?} */


    private static boolean isDebugScreenOpen() {
        Minecraft mc = Minecraft.getInstance();
        /*? if >= 1.21.11 { */
        return mc.debugEntries.isOverlayVisible();
        /*? } else if >= 1.21.9 { */
        /*return mc.debugEntries.isF3Visible();
        *//*? } else if >= 1.20.2 { */
        /*return mc.getDebugOverlay().showDebugScreen();
        *//*? } else { */
        /*return mc.options.renderDebug;
        *//*? }  */
    }

    private static int hudScale() {
        Minecraft mc = Minecraft.getInstance();
        int max = mc.getWindow().calculateScale(0, mc.isEnforceUnicode());
        return max / 2;
    }
}
