package me.jellysquid.mods.lithium.mixin.experimental.spawning;

import me.jellysquid.mods.lithium.common.world.ChunkAwareEntityIterable;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;iterateEntities()Ljava/lang/Iterable;"))
    private Iterable<Entity> iterateEntitiesChunkAware(ServerWorld serverWorld) {
        @SuppressWarnings("unchecked")
        ServerEntityManagerAccessor<Entity> entityManager = (ServerEntityManagerAccessor<Entity>) ((ServerWorldAccessor) serverWorld)
                .getEntityManager();
        @SuppressWarnings("unchecked")
        ChunkAwareEntityIterable<Entity> cache = (ChunkAwareEntityIterable<Entity>) entityManager.getCache();
        return cache.lithiumIterateEntitiesInTrackedSections();
    }
}
