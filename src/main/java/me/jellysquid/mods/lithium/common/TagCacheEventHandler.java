package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.util.TagCache;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LithiumMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TagCacheEventHandler {
    private TagCacheEventHandler() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        TagCache.clear();
    }
}