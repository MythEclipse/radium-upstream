package me.jellysquid.mods.lithium.common.block.redstone;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * AlternateCurrent provides a high-performance alternative to vanilla's
 * redstone update logic.
 * Instead of recursive isolation, it treats its neighbors as a network and
 * resolves power
 * levels using a Breadth-First Search (BFS) approach.
 *
 * Highly inspired by the Alternate Current mod by Space Walker.
 */
public class AlternateCurrent {
    private static final int MAX_POWER = 15;
    private static final int MIN_POWER = 0;

    private AlternateCurrent() {
    }

    /**
     * Resolves the power for a redstone network starting from the given position.
     * This avoids the deep recursion and redundant updates of vanilla.
     */
    public static void updateNetwork(World world, BlockPos startPos, BlockState startState) {
        if (!(startState.getBlock() instanceof RedstoneWireBlock)) {
            return;
        }
        Long2IntMap powerMap = new Long2IntOpenHashMap();
        Deque<BlockPos> queue = new ArrayDeque<>();
        LongSet visited = new LongOpenHashSet();

        // Phase 1: Discover the network and collect initial strengths
        discoverNetwork(world, startPos, powerMap, queue, visited);

        // Phase 2: BFS Power Propagation
        propagatePower(powerMap, queue);

        // Phase 3: Apply updates
        applyPowerUpdates(world, powerMap);
    }

    private static void discoverNetwork(World world, BlockPos start, Long2IntMap powerMap, Deque<BlockPos> queue,
            LongSet visited) {
        Deque<BlockPos> discoveryQueue = new ArrayDeque<>();
        discoveryQueue.add(start);
        visited.add(start.asLong());

        while (!discoveryQueue.isEmpty()) {
            BlockPos pos = discoveryQueue.poll();
            BlockState state = world.getBlockState(pos);

            if (!(state.getBlock() instanceof RedstoneWireBlock)) {
                continue;
            }

            int externalPower = getExternalPower(world, pos);
            powerMap.put(pos.asLong(), externalPower);
            queue.add(pos);

            // Explore neighbors (standard redstone connections)
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                long neighborLong = neighborPos.asLong();

                if (!visited.contains(neighborLong)) {
                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof RedstoneWireBlock) {
                        visited.add(neighborLong);
                        discoveryQueue.add(neighborPos);
                    }
                }
            }
        }
    }

    private static int getExternalPower(World world, BlockPos pos) {
        int max = 0;
        for (Direction dir : Direction.values()) {
            int power = world.getEmittedRedstonePower(pos.offset(dir), dir);
            if (power > max)
                max = power;
        }
        return Math.min(MAX_POWER, max);
    }

    private static void propagatePower(Long2IntMap powerMap, Deque<BlockPos> queue) {
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int currentPower = powerMap.get(pos.asLong());

            if (currentPower <= MIN_POWER)
                continue;

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                long neighborLong = neighborPos.asLong();

                if (powerMap.containsKey(neighborLong)) {
                    int neighborPower = powerMap.get(neighborLong);
                    int propagatedPower = currentPower - 1;

                    if (propagatedPower > neighborPower) {
                        powerMap.put(neighborLong, propagatedPower);
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }

    private static void applyPowerUpdates(World world, Long2IntMap powerMap) {
        for (Long2IntMap.Entry entry : powerMap.long2IntEntrySet()) {
            BlockPos pos = BlockPos.fromLong(entry.getLongKey());
            int newPower = entry.getIntValue();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof RedstoneWireBlock) {
                int oldPower = state.get(Properties.POWER);
                if (oldPower != newPower) {
                    world.setBlockState(pos, state.with(Properties.POWER, newPower), 2);
                }
            }
        }
    }
}
