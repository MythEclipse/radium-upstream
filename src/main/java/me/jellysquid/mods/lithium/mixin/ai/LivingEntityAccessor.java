package me.jellysquid.mods.lithium.mixin.ai;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("despawnCounter")
    int getDespawnCounter();

    @Accessor("despawnCounter")
    void setDespawnCounter(int value);
}
