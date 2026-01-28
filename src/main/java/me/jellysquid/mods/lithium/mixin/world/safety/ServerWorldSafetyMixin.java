package me.jellysquid.mods.lithium.mixin.world.safety;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public class ServerWorldSafetyMixin {
    @Inject(method = "getEntitiesByType", at = @At("HEAD"), cancellable = true)
    private void guardGetEntitiesByType(TypeFilter<?, ?> filter, Box box, Predicate<?> predicate,
            CallbackInfoReturnable<List<?>> cir) {
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
