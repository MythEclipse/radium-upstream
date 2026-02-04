package me.jellysquid.mods.lithium.common.accessor;

import net.minecraft.util.math.BlockPos;

/**
 * Accessor interface to get the position of a ticker.
 * Implemented via Mixin on
 * {@code net.minecraft.world.chunk.WorldChunk$DirectBlockEntityTickInvoker}
 * and
 * {@code net.minecraft.world.chunk.WorldChunk$WrappedBlockEntityTickInvoker}.
 */
public interface BlockEntityTickInvokerAccessor {
    BlockPos lithium$getPos();
}
