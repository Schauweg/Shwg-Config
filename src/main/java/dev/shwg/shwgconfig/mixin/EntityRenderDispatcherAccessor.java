package dev.shwg.shwgconfig.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {

    /**
     * The renderer of every entity type. Players aren't in here - vanilla keeps
     * their renderers separately, one per skin model.
     */
    @Accessor("renderers")
    Map<EntityType<?>, ?> shwgconfig$getRenderers();
}
