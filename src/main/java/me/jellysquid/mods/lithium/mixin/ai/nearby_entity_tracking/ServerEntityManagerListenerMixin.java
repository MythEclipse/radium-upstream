package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerMulti;
import me.jellysquid.mods.lithium.common.entity.nearby_tracker.NearbyEntityListenerProvider;
import me.jellysquid.mods.lithium.common.util.tuples.Range6Int;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/server/world/ServerEntityManager$Listener")
public abstract class ServerEntityManagerListenerMixin<T extends EntityLike> {
        @Shadow
        @Final
        private T entity;

        // @Final
        // @Shadow(aliases = { "manager" }, remap = false)
        // ServerEntityManager this$0;

        private static java.lang.reflect.Field managerField;

        @SuppressWarnings("unchecked")
        private ServerEntityManager<T> getManager() {
                try {
                        if (managerField == null) {
                                Class<?> clazz = this.getClass();
                                // Handle runtime class obfuscation differences or dev vs prod
                                // Try "manager" first (named), then "this$0" (synthetic inner), then
                                // "field_xxxx" if known (we don't)
                                try {
                                        managerField = clazz.getDeclaredField("manager");
                                } catch (NoSuchFieldException e) {
                                        managerField = clazz.getDeclaredField("this$0");
                                }
                                managerField.setAccessible(true);
                        }
                        return (ServerEntityManager<T>) managerField.get(this);
                } catch (Exception e) {
                        throw new RuntimeException(
                                        "Failed to access manager field in ServerEntityManager$Listener via reflection",
                                        e);
                }
        }

        @Shadow
        private long sectionPos;

        @Inject(method = "updateEntityPosition()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityTrackingSection;add(Lnet/minecraft/world/entity/EntityLike;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
        private void onUpdateEntityPosition(CallbackInfo ci, BlockPos blockPos, long newPos,
                        EntityTrackingStatus entityTrackingStatus, EntityTrackingSection<T> entityTrackingSection) {
                NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) this.entity).getListener();
                if (listener != null) {
                        Range6Int chunkRange = listener.getChunkRange();
                        @SuppressWarnings("unchecked")
                        ServerEntityManagerAccessor<T> accessor = (ServerEntityManagerAccessor<T>) this.getManager();
                        listener.updateChunkRegistrations(
                                        accessor.getCache(),
                                        ChunkSectionPos.from(this.sectionPos), chunkRange,
                                        ChunkSectionPos.from(newPos), chunkRange);
                }
        }

        @Inject(method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", at = @At(value = "HEAD"))
        private void onRemoveEntity(Entity.RemovalReason reason, CallbackInfo ci) {
                NearbyEntityListenerMulti listener = ((NearbyEntityListenerProvider) this.entity).getListener();
                if (listener != null) {
                        @SuppressWarnings("unchecked")
                        ServerEntityManagerAccessor<T> accessor = (ServerEntityManagerAccessor<T>) this.getManager();
                        listener.removeFromAllChunksInRange(
                                        accessor.getCache(),
                                        ChunkSectionPos.from(this.sectionPos));
                }
        }
}
