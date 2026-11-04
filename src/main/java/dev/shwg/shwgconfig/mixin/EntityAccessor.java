package dev.shwg.shwgconfig.mixin;

/*? if <1.21.2 {*/
/*import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
*//*?} else {*/
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
/*?}*/

/*? if <1.21.2 {*/
/*@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("wasTouchingWater")
    boolean shwgconfig$getWasTouchingWater();

    @Accessor("wasTouchingWater")
    void shwgconfig$setWasTouchingWater(boolean value);
}
*//*?} else {*/
@Mixin(Minecraft.class)
public interface EntityAccessor {
}
/*?}*/
