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

@Mixin(value = World.class, priority = 4000)
public class WorldSafetyMixin {
    private static boolean loggedApplication = false;

    @Inject(method = "getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, require = 0)
    private void guardGetEntities(Entity except, Box box, Predicate<? super Entity> predicate,
            CallbackInfoReturnable<List<Entity>> cir) {
        if (!loggedApplication) {
            System.out.println("[Radium/Safety] WorldSafetyMixin applied to getEntities");
            loggedApplication = true;
        }
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in World.getEntities! Returning empty list.");
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
