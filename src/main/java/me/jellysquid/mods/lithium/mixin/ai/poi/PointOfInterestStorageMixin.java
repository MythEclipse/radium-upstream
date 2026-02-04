package me.jellysquid.mods.lithium.mixin.ai.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.util.Distances;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestSetExtended;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import me.jellysquid.mods.lithium.common.world.interests.iterator.NearbyPointOfInterestStream;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SinglePointOfInterestTypeFilter;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SphereChunkOrderedPoiSetSpliterator;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(value = PointOfInterestStorage.class, priority = 2000)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet>
        implements PointOfInterestStorageExtended {

    @SuppressWarnings("java:S107") // Mixin constructor mirrors parent constructor
    protected PointOfInterestStorageMixin(Path path, Function<Runnable, Codec<PointOfInterestSet>> codecFactory,
            Function<Runnable, PointOfInterestSet> factory, DataFixer dataFixer, DataFixTypes dataFixTypes,
            boolean dsync, DynamicRegistryManager dynamicRegistryManager, HeightLimitView world) {
        super(path, codecFactory, factory, dataFixer, dataFixTypes, dsync, dynamicRegistryManager, world);
    }

    @Inject(method = "getNearestPosition", at = @At("HEAD"), cancellable = true)
    public void onGetNearestPosition(Predicate<RegistryEntry<PointOfInterestType>> predicate,
            Predicate<BlockPos> posPredicate, BlockPos pos, int radius,
            PointOfInterestStorage.OccupationStatus status, CallbackInfoReturnable<Optional<BlockPos>> cir) {
        cir.setReturnValue(this.getNearestPositionExtended(predicate, posPredicate, pos, radius, status));
    }

    private Optional<BlockPos> getNearestPositionExtended(Predicate<RegistryEntry<PointOfInterestType>> predicate,
            Predicate<BlockPos> posPredicate, BlockPos pos, int radius,
            PointOfInterestStorage.OccupationStatus status) {
        Stream<PointOfInterest> pointOfInterestStream = this.streamOutwards(pos, radius, status, true, false, predicate,
                posPredicate == null ? null : poi -> posPredicate.test(poi.getPos()));
        return pointOfInterestStream.map(PointOfInterest::getPos).findFirst();
    }

    @Inject(method = "count", at = @At("HEAD"), cancellable = true)
    public void onCount(Predicate<RegistryEntry<PointOfInterestType>> predicate, BlockPos pos, int radius,
            PointOfInterestStorage.OccupationStatus status, CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue((long) this.withinSphereChunkSectionSorted(predicate, pos, radius, status).size());
    }

    @Inject(method = "getInCircle", at = @At("HEAD"), cancellable = true)
    private void onGetInCircle(Predicate<RegistryEntry<PointOfInterestType>> predicate, BlockPos sphereOrigin,
            int radius,
            PointOfInterestStorage.OccupationStatus status, CallbackInfoReturnable<Stream<PointOfInterest>> cir) {
        cir.setReturnValue(this.withinSphereChunkSectionSortedStream(predicate, sphereOrigin, radius, status));
    }

    @Override
    public Optional<PointOfInterest> findNearestForPortalLogic(BlockPos origin, int radius,
            RegistryEntry<PointOfInterestType> type,
            PointOfInterestStorage.OccupationStatus status,
            Predicate<PointOfInterest> afterSortPredicate, WorldBorder worldBorder) {
        boolean worldBorderIsFarAway = worldBorder == null
                || worldBorder.getDistanceInsideBorder(origin.getX(), origin.getZ()) > radius + 3;
        Predicate<PointOfInterest> poiPredicateAfterSorting;
        if (worldBorderIsFarAway) {
            poiPredicateAfterSorting = afterSortPredicate;
        } else {
            poiPredicateAfterSorting = poi -> worldBorder != null && worldBorder.contains(poi.getPos())
                    && afterSortPredicate.test(poi);
        }
        return this.streamOutwards(origin, radius, status, true, true, new SinglePointOfInterestTypeFilter(type),
                poiPredicateAfterSorting).findFirst();
    }

    private Stream<PointOfInterest> withinSphereChunkSectionSortedStream(
            Predicate<RegistryEntry<PointOfInterestType>> predicate, BlockPos origin,
            int radius, PointOfInterestStorage.OccupationStatus status) {
        double radiusSq = (double) radius * radius;
        @SuppressWarnings("unchecked")
        RegionBasedStorageSectionExtended<PointOfInterestSet> storage = (RegionBasedStorageSectionExtended<PointOfInterestSet>) this;
        Stream<Stream<PointOfInterestSet>> stream = StreamSupport
                .stream(new SphereChunkOrderedPoiSetSpliterator(radius, origin, storage), false);
        return stream.flatMap((Stream<PointOfInterestSet> setStream) -> setStream.flatMap(
                (PointOfInterestSet set) -> set.get(predicate, status)
                        .filter(point -> Distances.isWithinCircleRadius(origin, radiusSq, point.getPos()))));
    }

    private ArrayList<PointOfInterest> withinSphereChunkSectionSorted(
            Predicate<RegistryEntry<PointOfInterestType>> predicate, BlockPos origin,
            int radius, PointOfInterestStorage.OccupationStatus status) {
        double radiusSq = (double) radius * radius;
        int minChunkX = origin.getX() - radius - 1 >> 4;
        int minChunkZ = origin.getZ() - radius - 1 >> 4;
        int maxChunkX = origin.getX() + radius + 1 >> 4;
        int maxChunkZ = origin.getZ() + radius + 1 >> 4;
        @SuppressWarnings("unchecked")
        RegionBasedStorageSectionExtended<PointOfInterestSet> storage = (RegionBasedStorageSectionExtended<PointOfInterestSet>) this;
        ArrayList<PointOfInterest> points = new ArrayList<>();
        Consumer<PointOfInterest> collector = point -> {
            if (Distances.isWithinCircleRadius(origin, radiusSq, point.getPos())) {
                points.add(point);
            }
        };
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                for (PointOfInterestSet set : storage.getInChunkColumn(x, z)) {
                    ((PointOfInterestSetExtended) set).collectMatchingPoints(predicate, status, collector);
                }
            }
        }
        return points;
    }

    private Stream<PointOfInterest> streamOutwards(BlockPos origin, int radius,
            PointOfInterestStorage.OccupationStatus status,
            boolean useSquareDistanceLimit,
            boolean preferNegativeY,
            Predicate<RegistryEntry<PointOfInterestType>> typePredicate,
            @Nullable Predicate<PointOfInterest> afterSortingPredicate) {
        @SuppressWarnings("unchecked")
        RegionBasedStorageSectionExtended<PointOfInterestSet> storage = (RegionBasedStorageSectionExtended<PointOfInterestSet>) this;
        return StreamSupport.stream(new NearbyPointOfInterestStream(typePredicate, status, useSquareDistanceLimit,
                preferNegativeY, afterSortingPredicate, origin, radius, storage), false);
    }

    @Shadow
    protected abstract void scanAndPopulate(net.minecraft.world.chunk.ChunkSection section, ChunkSectionPos sectionPos,
            java.util.function.BiConsumer<BlockPos, PointOfInterestType> entryConsumer);
}
