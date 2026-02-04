package me.jellysquid.mods.lithium.mixin.world.tick;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class ServerTickRateLimiterMixin {

    @Unique
    private long tickStartNanos;

    @Inject(method = "tick", at = @At("HEAD"))
    private void startTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.tickStartNanos = System.nanoTime();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void limitTps(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        int maxTps = Integer.getInteger("lithium.max_tps", 20);
        if (maxTps <= 0) {
            maxTps = 20;
        }
        if (maxTps > 20) {
            maxTps = 20;
        }

        long targetNanos = TimeUnit.SECONDS.toNanos(1) / maxTps;
        long elapsed = System.nanoTime() - this.tickStartNanos;
        long remaining = targetNanos - elapsed;
        if (remaining > 0) {
            if (Boolean.getBoolean("lithium.debug")) {
                System.out.println("[Lithium-TickRate] Sleeping " + TimeUnit.NANOSECONDS.toMillis(remaining) + "ms to cap TPS");
            }
            LockSupport.parkNanos(remaining);
        }
    }
}
