package dev.shwg.shwgconfig.mixin;

import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.shwgconfig.render.GenericGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*? if >= 26 { */
import net.minecraft.client.renderer.state.gui.GuiRenderState;
/*? } else if >= 1.21.6 { */
//import net.minecraft.client.gui.render.state.GuiRenderState;
/*? } */

/*? if >= 1.21.6 { */
import org.joml.Matrix3x2fStack;
/*? } else { */
//import com.mojang.blaze3d.vertex.PoseStack;
/*? } */

/*? if < 1.21.6 && >= 1.20 { */
//import net.minecraft.client.renderer.MultiBufferSource;
/*? } */

/*? if >= 1.20 { */
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
/*? } */

/*? if >= 1.20 { */
@Mixin(GuiGraphicsExtractor.class)
/*? } else { */
//@Mixin(PoseStack.class)
/*? } */
public class GraphicsMixin implements GraphicsAccessor {

    @Unique
    GenericGraphics shwgConfigs$genericGraphics;

    /*? if >= 26 { */
    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Lorg/joml/Matrix3x2fStack;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;II)V", at = @At("RETURN"))
    public void onInit(Minecraft minecraft, Matrix3x2fStack pose, GuiRenderState guiRenderState, int mouseX, int mouseY, CallbackInfo ci) {
        this.shwgConfigs$genericGraphics = new GenericGraphics((GuiGraphicsExtractor)(Object)this);
    }
    /*? } else if >= 1.21.11 { */
    /*@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Lorg/joml/Matrix3x2fStack;Lnet/minecraft/client/gui/render/state/GuiRenderState;II)V", at = @At("RETURN"))
    public void onInit(Minecraft minecraft, Matrix3x2fStack pose, GuiRenderState guiRenderState, int mouseX, int mouseY, CallbackInfo ci) {
        this.shwgConfigs$genericGraphics = new GenericGraphics((GuiGraphicsExtractor)(Object)this);
    }
    *//*? } else if >= 1.21.6 { */
    /*@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Lorg/joml/Matrix3x2fStack;Lnet/minecraft/client/gui/render/state/GuiRenderState;)V", at = @At("RETURN"))
    public void onInit(Minecraft minecraft, Matrix3x2fStack pose, GuiRenderState guiRenderState, CallbackInfo ci) {
        this.shwgConfigs$genericGraphics = new GenericGraphics((GuiGraphicsExtractor)(Object)this);
    }
    *//*? } else if >= 1.20 { */
    /*@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V", at = @At("RETURN"))
    public void onInit(Minecraft minecraft, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        this.shwgConfigs$genericGraphics = new GenericGraphics((GuiGraphicsExtractor)(Object)this);
    }
    *//*? } else { */
    /*@Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci){
        this.shwgConfigs$genericGraphics = new GenericGraphics((PoseStack)(Object)this);
    }
    *//*? } */

    @Override
    public GenericGraphics shwgConfigs$getGraphics() {
        return this.shwgConfigs$genericGraphics;
    }
}
