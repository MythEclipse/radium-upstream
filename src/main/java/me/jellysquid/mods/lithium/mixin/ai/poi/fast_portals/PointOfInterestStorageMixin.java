package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

@Mixin(value = PointOfInterestStorage.class, priority = 2000)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet> {

    @Shadow
    @Final
    private LongSet preloadedChunks;

    @Unique
    private final LongSet preloadedCenterChunks = new LongOpenHashSet();
    @Unique
    private int preloadRadius = 0;

    public PointOfInterestStorageMixin(Path path, Function<Runnable, Codec<PointOfInterestSet>> codecFactory,
            Function<Runnable, PointOfInterestSet> factory, DataFixer dataFixer, DataFixTypes dataFixTypes,
            boolean dsync, DynamicRegistryManager dynamicRegistryManager, HeightLimitView world) {
        super(path, codecFactory, factory, dataFixer, dataFixTypes, dsync, dynamicRegistryManager, world);
    }

    @Inject(method = "preloadChunks", at = @At("HEAD"), cancellable = true)
    public void onPreloadChunks(WorldView worldView, BlockPos pos, int radius, CallbackInfo ci) {
        if (this.preloadRadius != radius) {
            this.preloadedCenterChunks.clear();
            this.preloadRadius = radius;
        }
        long chunkPos = ChunkPos.toLong(pos);
        if (this.preloadedCenterChunks.contains(chunkPos)) {
            ci.cancel();
            return;
        }
        int chunkX = ChunkSectionPos.getSectionCoord(pos.getX());
        int chunkZ = ChunkSectionPos.getSectionCoord(pos.getZ());

        int chunkRadius = Math.floorDiv(radius, 16);
        int maxHeight = this.world.getTopSectionCoord() - 1;
        int minHeight = this.world.getBottomSectionCoord();

        for (int x = chunkX - chunkRadius, xMax = chunkX + chunkRadius; x <= xMax; x++) {
            for (int z = chunkZ - chunkRadius, zMax = chunkZ + chunkRadius; z <= zMax; z++) {
                lithium$preloadChunkIfAnySubChunkContainsPOI(worldView, x, z, minHeight, maxHeight);
            }
        }
        this.preloadedCenterChunks.add(chunkPos);
        ci.cancel();
    }

    @Unique
    private void lithium$preloadChunkIfAnySubChunkContainsPOI(WorldView worldView, int x, int z, int minSubChunk,
            int maxSubChunk) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        long longChunkPos = chunkPos.toLong();

        if (this.preloadedChunks.contains(longChunkPos))
            return;

        for (int y = minSubChunk; y <= maxSubChunk; y++) {
            Optional<PointOfInterestSet> section = this.get(ChunkSectionPos.asLong(x, y, z));
            if (section.isPresent()) {
                boolean result = section.get().isValid();
                if (result) {
                    if (this.preloadedChunks.add(longChunkPos)) {
                        worldView.getChunk(x, z, ChunkStatus.EMPTY);
                    }
                    break;
                }
            }
        }
    }
}
