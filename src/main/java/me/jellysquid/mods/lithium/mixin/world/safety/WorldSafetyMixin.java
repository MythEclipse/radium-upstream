package me.jellysquid.mods.lithium.mixin.world.safety;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = World.class, priority = 10000)
public class WorldSafetyMixin {
    private static boolean loggedApplication = false;

    @ModifyVariable(method = "getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("HEAD"), ordinal = 1)
    private Box modifyBoxGetEntities(Box box) {
        if (!loggedApplication) {
            System.out.println("[Radium/Safety] WorldSafetyMixin applied to getEntities");
            loggedApplication = true;
        }
        if (!LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in World.getEntities! Replacing with empty box.");
            return new Box(0, 0, 0, 0, 0, 0);
        }
        return box;
    }
}
