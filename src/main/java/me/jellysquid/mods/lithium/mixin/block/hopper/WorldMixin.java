package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.UpdateReceiver;
import me.jellysquid.mods.lithium.common.util.DirectionConstants;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(World.class)
public class WorldMixin {
    // Verified: Handles update suppression edge case for hoppers when block updates are suppressed.
    // See: https://www.youtube.com/watch?v=QVOONJ1OY44 for update suppression behavior reference.

    @Inject(method = "markAndNotifyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V")

    )
    private void updateHopperOnUpdateSuppression(BlockPos pos, WorldChunk worldChunk, BlockState blockState,
            BlockState blockState2, int flags, int k, CallbackInfo ci) {
        if ((flags & Block.NOTIFY_NEIGHBORS) == 0) {
            this.updateHoppersOnSuppressedUpdate(pos, worldChunk, blockState, blockState2);
        }
    }

    private void updateHoppersOnSuppressedUpdate(BlockPos pos, WorldChunk worldChunk, BlockState blockState, BlockState blockState2) {
        // No block updates were sent. We need to update nearby hoppers to avoid outdated inventory caches being used
        if (blockState == blockState2) {
            return;
        }

        Map<BlockPos, BlockEntity> blockEntities = WorldHelper.areNeighborsWithinSameChunk(pos)
                ? worldChunk.getBlockEntities()
                : null;

        if (blockEntities == null && !WorldHelper.areNeighborsWithinSameChunk(pos)) {
            return;
        }

        for (Direction direction : DirectionConstants.ALL) {
            BlockPos offsetPos = pos.offset(direction);
            BlockEntity hopper = blockEntities != null ? blockEntities.get(offsetPos)
                    : ((BlockEntityGetter) this).getLoadedExistingBlockEntity(offsetPos);
            if (hopper instanceof UpdateReceiver updateReceiver) {
                updateReceiver.invalidateCacheOnNeighborUpdate(direction == Direction.DOWN);
            }
        }
    }
}
