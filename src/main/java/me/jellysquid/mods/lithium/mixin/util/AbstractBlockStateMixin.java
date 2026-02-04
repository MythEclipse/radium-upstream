package me.jellysquid.mods.lithium.mixin.util;

import me.jellysquid.mods.lithium.common.util.TagCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    /**
     * @author Antigravity
     * @reason Use TagCache for faster tag lookups.
     */
    @Inject(method = "isIn(Lnet/minecraft/registry/tag/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void onIsIn(TagKey<Block> tag, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TagCache.isIn((net.minecraft.block.BlockState) (Object) this, tag));
    }
}
