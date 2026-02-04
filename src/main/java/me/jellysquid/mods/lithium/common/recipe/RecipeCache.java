package me.jellysquid.mods.lithium.common.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RecipeCache stores the last successful recipe match for each RecipeType.
 * This significantly speeds up inventory-heavy blocks like Crafters and
 * Villagers.
 */
public class RecipeCache {
    private static final Map<RecipeType<?>, Recipe<?>> LAST_MATCH_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <C extends Inventory, T extends Recipe<C>> Optional<T> getCachedMatch(RecipeType<T> type, C inventory,
            World world) {
        T lastMatch = (T) LAST_MATCH_CACHE.get(type);
        if (lastMatch != null && lastMatch.matches(inventory, world)) {
            return Optional.of(lastMatch);
        }
        return Optional.empty();
    }

    public static <C extends Inventory, T extends Recipe<C>> void updateCache(RecipeType<T> type, T recipe) {
        LAST_MATCH_CACHE.put(type, recipe);
    }

    public static void clear() {
        LAST_MATCH_CACHE.clear();
    }
}
