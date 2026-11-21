package dev.shwg.shwgconfig.mixin;

import org.spongepowered.asm.mixin.Mixin;

/*? if forge && (1.20.6 || (>= 1.21 && < 1.21.5) || (>= 1.21.6 && < 1.21.9)) {*/
/*import dev.shwg.shwgconfig.api.hud.HudRenderDispatcher;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/^? if >= 1.21 {^/
import net.minecraft.client.DeltaTracker;
/^?}^/

@Mixin(Gui.class)
public abstract class GuiHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    /^? if >= 1.21 {^/
    private void shwgconfig$renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        HudRenderDispatcher.renderFromMixin(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
    /^?} else {^/
    /^private void shwgconfig$renderHud(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        HudRenderDispatcher.renderFromMixin(guiGraphics, partialTick);
    }
    ^//^?}^/
}
*//*?} else {*/
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class GuiHudMixin {
}
/*?}*/
