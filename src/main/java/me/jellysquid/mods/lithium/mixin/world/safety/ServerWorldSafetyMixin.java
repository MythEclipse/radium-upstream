package me.jellysquid.mods.lithium.mixin.world.safety;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerWorld.class, priority = 10000)
public class ServerWorldSafetyMixin {
    private static boolean loggedApplication = false;

    @ModifyVariable(method = "getEntitiesByType", at = @At("HEAD"), ordinal = 1)
    private Box modifyBoxGetEntitiesByType(Box box) {
        if (!loggedApplication) {
            System.out.println("[Radium/Safety] ServerWorldSafetyMixin applied to getEntitiesByType");
            loggedApplication = true;
        }
        if (!me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in ServerWorld.getEntitiesByType! Replacing with empty box.");
            return new Box(0, 0, 0, 0, 0, 0);
        }
        return box;
    }

    @ModifyVariable(method = "getEntities", at = @At("HEAD"), ordinal = 1)
    private Box modifyBoxGetEntities(Box box) {
        if (!me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions.isBoxFinite(box)) {
            System.err.println(
                    "[Radium/Safety] Intercepted infinite/NaN box in ServerWorld.getEntities! Replacing with empty box.");
            return new Box(0, 0, 0, 0, 0, 0);
        }
        return box;
    }
}
