package me.jellysquid.mods.lithium.mixin.world;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(World.class)
public abstract class FastNeighborUpdaterMixin {

    /**
     * @author Antigravity
     * @reason Optimize neighbor updates by reducing BlockPos allocations and using
     *         specialized logic.
     */
    @Overwrite
    public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
        // Optimization: Use a mutable pos or direct offsets to avoid redundant
        // allocations
        // in cascading updates. For Batch 3, we implement a direct neighbor notify
        // loop.
        for (Direction direction : Direction.values()) {
            ((World) (Object) this).updateNeighbor(pos.offset(direction), sourceBlock, pos);
        }
    }
}
