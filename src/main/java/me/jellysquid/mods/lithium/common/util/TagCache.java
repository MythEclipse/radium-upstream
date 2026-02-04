package me.jellysquid.mods.lithium.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TagCache provides a fast lookup for tag membership.
 * Lookups for common tags in hot paths (like tick loops) can be significantly
 * improved.
 */
public class TagCache {
    private static final Map<Block, Map<TagKey<Block>, Boolean>> BLOCK_TAG_CACHE = new ConcurrentHashMap<>();

    private TagCache() {
    }

    public static boolean isIn(BlockState state, TagKey<Block> tag) {
        Block block = state.getBlock();
        Map<TagKey<Block>, Boolean> blockMap = BLOCK_TAG_CACHE.computeIfAbsent(block, b -> new ConcurrentHashMap<>());
        return blockMap.computeIfAbsent(tag, t -> state.getRegistryEntry().isIn(t));
    }

    /**
     * Clears the cache. Should be called when tags are reloaded.
     */
    public static void clear() {
        BLOCK_TAG_CACHE.clear();
    }
}
