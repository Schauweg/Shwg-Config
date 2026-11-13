package dev.shwg.testmod.mixin;

import dev.shwg.testmod.test.checks.EarlyMixinMarkers;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

/** Changes nothing, only marks Minecraft. Applied by TestMixinPlugin only if a mod that doesn't exist is present. */
@Mixin(Minecraft.class)
public abstract class ModAbsentMixin implements EarlyMixinMarkers.Absent {
}
