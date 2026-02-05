package me.jellysquid.mods.lithium.mixin.world.tick;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class ServerTickProfilerMixin {
    @Unique
    private long lithium$profileStartNanos;
    @Unique
    private int lithium$profileIndex;
    @Unique
    private final long[] lithium$profileWindow = new long[200];

    @Inject(method = "tick", at = @At("HEAD"))
    private void lithium$startProfile(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (Boolean.getBoolean("lithium.tick_profiler")) {
            this.lithium$profileStartNanos = System.nanoTime();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lithium$endProfile(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!Boolean.getBoolean("lithium.tick_profiler")) {
            return;
        }

        long elapsed = System.nanoTime() - this.lithium$profileStartNanos;
        int idx = this.lithium$profileIndex++ % this.lithium$profileWindow.length;
        this.lithium$profileWindow[idx] = elapsed;

        if (this.lithium$profileIndex % this.lithium$profileWindow.length == 0) {
            long sum = 0L;
            long max = 0L;
            for (long sample : this.lithium$profileWindow) {
                sum += sample;
                if (sample > max) {
                    max = sample;
                }
            }
            long avgMs = Math.round((sum / (double) this.lithium$profileWindow.length) / 1_000_000.0);
            long maxMs = Math.round(max / 1_000_000.0);
            System.out.println("[Lithium-TickProfiler] avg=" + avgMs + "ms max=" + maxMs + "ms (" + this.lithium$profileWindow.length + " ticks)");
        }
    }
}