package me.jellysquid.mods.lithium.mixin.world.safety;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public class WorldSafetyMixin {
    @Inject(method = "getOtherEntities", at = @At("HEAD"), cancellable = true)
    private void guardGetOtherEntities(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
