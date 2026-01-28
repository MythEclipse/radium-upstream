package me.jellysquid.mods.lithium.mixin.world.safety;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = ClientWorld.class, priority = 2000)
public class ClientWorldSafetyMixin {
    // Guard getEntities (generic override)
    // Using aliases and optional requirements
    @Inject(method = {"getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;", "method_8335", "m_45933_"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0, expect = 0)
    private void guardGetEntities(Entity except, Box box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
