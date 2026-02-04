package me.jellysquid.mods.lithium.mixin.recipe;

import me.jellysquid.mods.lithium.common.recipe.RecipeCache;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    /**
     * @author Antigravity
     * @reason Intercept recipe lookups to use the RecipeCache.
     */
    @Inject(method = "getFirstMatch", at = @At("HEAD"), cancellable = true)
    private <C extends Inventory, T extends Recipe<C>> void onGetFirstMatch(RecipeType<T> type, C inventory,
            World world, CallbackInfoReturnable<Optional<T>> cir) {
        Optional<T> cached = RecipeCache.getCachedMatch(type, inventory, world);
        if (cached.isPresent()) {
            cir.setReturnValue(cached);
        }
    }

    /**
     * @author Antigravity
     * @reason Update the RecipeCache when a match is found.
     */
    @Inject(method = "getFirstMatch", at = @At("RETURN"))
    private <C extends Inventory, T extends Recipe<C>> void afterGetFirstMatch(RecipeType<T> type, C inventory,
            World world, CallbackInfoReturnable<Optional<T>> cir) {
        Optional<T> result = cir.getReturnValue();
        if (result.isPresent()) {
            RecipeCache.updateCache(type, result.get());
        }
    }
}
