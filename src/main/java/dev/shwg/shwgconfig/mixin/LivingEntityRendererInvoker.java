package dev.shwg.shwgconfig.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/*? if <1.21.2 {*/
//import net.minecraft.world.entity.LivingEntity;
 /*?} else {*/
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
/*?}*/

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    /*? if <1.21.2 {*/
    /*@Invoker("scale")
    void shwgconfig$scale(LivingEntity entity, PoseStack poseStack, float partialTick);
    *//*?} else {*/
    @Invoker("scale")
    void shwgconfig$scale(LivingEntityRenderState state, PoseStack poseStack);
    /*?}*/
}
