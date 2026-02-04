package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.util.LithiumThreadPool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(EntityNavigation.class)
public abstract class ParallelPathfindingMixin {

    @Shadow
    protected MobEntity entity;

    @Shadow
    protected World world;

    @Invoker("findPathTo")
    protected abstract Path callFindPathTo(Set<BlockPos> targets, int range, boolean canOpenDoors, int distance);

    @Invoker("findPathToAny")
    protected abstract Path callFindPathToAny(Set<BlockPos> targets, int range, boolean canOpenDoors, int distance, float maxDistance);

    @Unique
    @SuppressWarnings("java:S3077")
    private volatile Path asyncPath;
    @Unique
    @SuppressWarnings("java:S3077")
    private volatile CompletableFuture<Path> asyncPathFuture;
    @Unique
    private volatile long lastPathfindTime;

    @Unique
    private static final long PATHFIND_COOLDOWN_MS = 100L;

    @Unique
    private Path scheduleAsyncPath(Supplier<Path> supplier) {
        if (!Boolean.getBoolean("lithium.async_pathfinding")) {
            return supplier.get();
        }
        if (this.world != null && this.world.isClient) {
            return supplier.get();
        }

        CompletableFuture<Path> future = this.asyncPathFuture;
        if (future != null && future.isDone()) {
            Path result = future.getNow(null);
            this.asyncPathFuture = null;
            if (result != null) {
                this.asyncPath = result;
            }
            return result != null ? result : this.asyncPath;
        }

        long now = System.currentTimeMillis();
        if (future == null && now - this.lastPathfindTime >= PATHFIND_COOLDOWN_MS) {
            this.lastPathfindTime = now;
            this.asyncPathFuture = CompletableFuture.supplyAsync(supplier, LithiumThreadPool.getMiscPool());
        }

        return this.asyncPath;
    }

    @Inject(method = "findPathToAny(Ljava/util/stream/Stream;I)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At("HEAD"), cancellable = true)
    private void asyncFindPathToAny(Stream<BlockPos> positions, int distance,
                                            CallbackInfoReturnable<Path> cir) {
        Set<BlockPos> targets = positions.collect(Collectors.toSet());
        cir.setReturnValue(this.scheduleAsyncPath(() -> this.callFindPathTo(targets, 8, false, distance)));
    }

    @Inject(method = "findPathTo(Ljava/util/Set;I)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At("HEAD"), cancellable = true)
    private void asyncFindPathToSet(Set<BlockPos> positions, int distance,
                                            CallbackInfoReturnable<Path> cir) {
        cir.setReturnValue(this.scheduleAsyncPath(() -> this.callFindPathTo(positions, 8, false, distance)));
    }

    @Inject(method = "findPathTo(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At("HEAD"), cancellable = true)
    private void asyncFindPathToPos(BlockPos pos, int distance,
                                            CallbackInfoReturnable<Path> cir) {
        Set<BlockPos> targets = Set.of(Objects.requireNonNull(pos, "pos"));
        cir.setReturnValue(this.scheduleAsyncPath(() -> this.callFindPathTo(targets, 8, false, distance)));
    }

    @Inject(method = "findPathTo(Lnet/minecraft/util/math/BlockPos;II)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At("HEAD"), cancellable = true)
    private void asyncFindPathToPosRange(BlockPos pos, int distance, int maxDistance,
                                                 CallbackInfoReturnable<Path> cir) {
        Set<BlockPos> targets = Set.of(Objects.requireNonNull(pos, "pos"));
        cir.setReturnValue(this.scheduleAsyncPath(
                () -> this.callFindPathToAny(targets, 8, false, distance, maxDistance)));
    }

    @Inject(method = "findPathTo(Lnet/minecraft/entity/Entity;I)Lnet/minecraft/entity/ai/pathing/Path;",
            at = @At("HEAD"), cancellable = true)
    private void asyncFindPathToEntity(Entity target, int distance,
                                               CallbackInfoReturnable<Path> cir) {
        BlockPos targetPos = Objects.requireNonNull(target.getBlockPos(), "targetPos");
        Set<BlockPos> targets = Set.of(targetPos);
        cir.setReturnValue(this.scheduleAsyncPath(() -> this.callFindPathTo(targets, 16, true, distance)));
    }
}
