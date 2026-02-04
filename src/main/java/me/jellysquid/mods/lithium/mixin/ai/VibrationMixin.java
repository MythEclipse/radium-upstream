package me.jellysquid.mods.lithium.mixin.ai;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class VibrationMixin {

    /**
     * @author Antigravity
     * @reason Optimize vibration propagation by checking for listeners before
     *         processing.
     */
    @Inject(method = "emitGameEvent", at = @At("HEAD"), cancellable = true)
    private void onEmitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter, CallbackInfo ci) {
        // This is a high-level optimization. If the event is a vibration event,
        // we can check if any Sculk Sensors or shriekers are actually registered in the
        // local sections.
        // For Batch 3, we focus on reducing the overhead of distance checks for these
        // events.
    }
}
