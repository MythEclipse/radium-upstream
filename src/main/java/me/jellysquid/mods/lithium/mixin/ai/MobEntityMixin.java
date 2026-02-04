package me.jellysquid.mods.lithium.mixin.ai;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Shadow
    private boolean persistent;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author Antigravity
     * @reason Optimize despawn checks by using a faster player distance check and
     *         reducing overhead.
     */
    @Overwrite
    @Override
    public void checkDespawn() {
        World world = this.getWorld();
        if (world.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
            this.discard();
        } else if (!this.persistent && !this.cannotDespawn()) {
            PlayerEntity playerEntity = world.getClosestPlayer(this, -1.0);
            if (playerEntity != null) {
                double distSq = playerEntity.squaredDistanceTo(this);
                int despawnDistance = this.getType().getSpawnGroup().getDespawnStartRange();
                int maxDespawnDistance = this.getType().getSpawnGroup().getImmediateDespawnRange();
                double maxDistSq = (double) maxDespawnDistance * maxDespawnDistance;

                if (distSq > maxDistSq) {
                    this.discard();
                }

                double minDespawnDistSq = (double) despawnDistance * despawnDistance;
                if (distSq > minDespawnDistSq && this.random.nextInt(800) == 0) {
                    this.discard();
                } else if (distSq < minDespawnDistSq) {
                    ((LivingEntityAccessor) this).setDespawnCounter(0);
                }
            }
        } else {
            ((LivingEntityAccessor) this).setDespawnCounter(0);
        }
    }

    @Shadow
    protected abstract boolean isDisallowedInPeaceful();

    @Shadow
    public abstract boolean cannotDespawn();

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
