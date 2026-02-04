package me.jellysquid.mods.lithium.common.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeaconOptimization handles tracking of block changes in beacon pyramid
 * volumes.
 */
public class BeaconOptimization {
    private static final Map<BlockPos, Long> BEACON_LAST_STABLE_TIME = new ConcurrentHashMap<>();
    private static final long STABLE_WINDOW_MS = 50L;

    private BeaconOptimization() {
    }

    public static boolean isPyramidDirty(World world, BlockPos pos, int level) {
        if (world == null) {
            return true;
        }
        int clampedLevel = Math.max(level, 0);
        long windowMs = STABLE_WINDOW_MS + clampedLevel * 2L;
        Long lastStable = BEACON_LAST_STABLE_TIME.get(pos);
        if (lastStable == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return now - lastStable > windowMs;
    }

    public static void markClean(BlockPos pos) {
        BEACON_LAST_STABLE_TIME.put(pos.toImmutable(), System.currentTimeMillis());
    }
}
