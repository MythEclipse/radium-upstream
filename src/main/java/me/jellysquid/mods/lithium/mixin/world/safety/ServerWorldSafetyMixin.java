package me.jellysquid.mods.lithium.mixin.world.safety;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.entity.Entity;
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

@Mixin(value = ServerWorld.class, priority = 4000)
public class ServerWorldSafetyMixin {
    private static boolean loggedApplication = false;

    @Inject(method = "getEntitiesByType", at = @At("HEAD"), cancellable = true, require = 0)
    private void guardGetEntitiesByType(TypeFilter<Entity, ?> filter, Box box, Predicate<? super Entity> predicate,
            CallbackInfoReturnable<List<?>> cir) {
        if (!loggedApplication) {
            System.out.println("[Radium/Safety] ServerWorldSafetyMixin applied to getEntitiesByType");
            loggedApplication = true;
        }
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in ServerWorld.getEntitiesByType! Returning empty list.");
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Inject(method = "getEntities", at = @At("HEAD"), cancellable = true, require = 0)
    private void guardGetEntities(Entity except, Box box, Predicate<? super Entity> predicate,
            CallbackInfoReturnable<List<Entity>> cir) {
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in ServerWorld.getEntities! Returning empty list.");
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
