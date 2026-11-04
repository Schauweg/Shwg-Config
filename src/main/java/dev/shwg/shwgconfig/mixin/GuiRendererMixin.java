package dev.shwg.shwgconfig.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

/*? if >=1.21.6 && (forge || (fabric && <1.21.9)) {*/
/*import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
*//*?}*/

/*? if >=26 && forge {*/
/*import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
*//*?} else if >=1.21.6 && (forge || (fabric && <1.21.9)) {*/
/*import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
*//*?}*/

/*? if >=26.2 && forge {*/
//import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
/*?} else if >=1.21.6 && (forge || (fabric && <1.21.9)) {*/
//import net.minecraft.client.renderer.MultiBufferSource;
/*?}*/

/*? if >=1.21.6 && (forge || (fabric && <1.21.9)) {*/
/*@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {

    @Shadow @Final GuiRenderState renderState;

    @Unique private final List<GuiEntityRenderer> shwgconfig$extraEntityRenderers = new ArrayList<>();
    @Unique private int shwgconfig$entityCount;

    @Inject(method = "preparePictureInPicture", at = @At("HEAD"))
    private void shwgconfig$startFrame(CallbackInfo ci) {
        shwgconfig$entityCount = 0;
    }

    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private void shwgconfig$ownRendererPerEntity(PictureInPictureRenderState state, int guiScale, CallbackInfo ci) {
        if (!(state instanceof GuiEntityRenderState entityState)) return;

        int index = shwgconfig$entityCount++;
        if (index == 0) return;

        while (shwgconfig$extraEntityRenderers.size() < index) {
            shwgconfig$extraEntityRenderers.add(shwgconfig$newEntityRenderer());
        }
        shwgconfig$prepare(shwgconfig$extraEntityRenderers.get(index - 1), entityState, guiScale);
        ci.cancel();
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void shwgconfig$closeExtraRenderers(CallbackInfo ci) {
        shwgconfig$extraEntityRenderers.forEach(GuiEntityRenderer::close);
        shwgconfig$extraEntityRenderers.clear();
    }
*//*?} else {*/
@Mixin(Minecraft.class)
public abstract class GuiRendererMixin {
/*?}*/

    /*? if >=26.2 && forge {*/
    /*@Shadow @Final private FeatureRenderDispatcher featureRenderDispatcher;

    @Unique
    private GuiEntityRenderer shwgconfig$newEntityRenderer() {
        return new GuiEntityRenderer(Minecraft.getInstance().getEntityRenderDispatcher());
    }

    @Unique
    private void shwgconfig$prepare(GuiEntityRenderer renderer, GuiEntityRenderState state, int guiScale) {
        renderer.prepare(state, renderState, featureRenderDispatcher, guiScale);
    }
    *//*?} else if >=1.21.6 && (forge || (fabric && <1.21.9)) {*/
    /*@Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Unique
    private GuiEntityRenderer shwgconfig$newEntityRenderer() {
        return new GuiEntityRenderer(bufferSource, Minecraft.getInstance().getEntityRenderDispatcher());
    }

    @Unique
    private void shwgconfig$prepare(GuiEntityRenderer renderer, GuiEntityRenderState state, int guiScale) {
        renderer.prepare(state, renderState, guiScale);
    }
*//*?}*/
}
