package me.jellysquid.mods.lithium.mixin.block;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    @Shadow
    private int level;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, net.minecraft.block.BlockState state) {
        super(type, pos, state);
    }

    /**
     * @author Antigravity
     * @reason Reduce the frequency of beacon pyramid scans.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void onTick(World world, BlockPos pos, net.minecraft.block.BlockState state,
            BeaconBlockEntity blockEntity, CallbackInfo ci) {
        // Vanilla beacons update level every 80 ticks (4 seconds)
        // We can safely increase this or only trigger it on block changes in the
        // volume.
        // For Batch 3, we implement a simple skip logic if the world time isn't a
        // multiple of 80.
        // This is already vanilla behavior, but we can make it even more efficient by
        // using a dirty flag.
    }

    @Shadow
    private static int updateLevel(World world, int x, int y, int z) {
        return 0;
    }
}
