package me.jellysquid.mods.lithium.mixin.recipe;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class IngredientMixin {

    @Shadow
    public abstract ItemStack[] getMatchingStacks();

    @Unique
    private IntSet lithium$matchingItemIds;

    /**
     * @author Antigravity
     * @reason Optimize ingredient matching by caching item IDs in a hash set.
     */
    @Inject(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onTest(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (this.lithium$matchingItemIds == null) {
            this.lithium$cacheMatchingItems();
        }

        cir.setReturnValue(this.lithium$matchingItemIds.contains(Item.getRawId(stack.getItem())));
    }

    @Unique
    private void lithium$cacheMatchingItems() {
        ItemStack[] stacks = this.getMatchingStacks();
        IntSet ids = new IntOpenHashSet(stacks.length);
        for (ItemStack stack : stacks) {
            ids.add(Item.getRawId(stack.getItem()));
        }
        this.lithium$matchingItemIds = ids;
    }
}
