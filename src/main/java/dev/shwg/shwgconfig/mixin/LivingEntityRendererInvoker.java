package dev.shwg.shwgconfig.mixin;

/*? if <1.21.2 {*/
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
*//*?} else {*/
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
/*?}*/

/*? if <1.21.2 {*/
/*@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    @Invoker("scale")
    void shwgconfig$scale(LivingEntity entity, PoseStack poseStack, float partialTick);
}
*//*?} else {*/
@Mixin(Minecraft.class)
public interface LivingEntityRendererInvoker {
}
/*?}*/
