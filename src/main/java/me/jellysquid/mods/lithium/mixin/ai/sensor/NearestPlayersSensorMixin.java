package me.jellysquid.mods.lithium.mixin.ai.sensor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Comparator;
import java.util.List;

@Mixin(NearestPlayersSensor.class)
public class NearestPlayersSensorMixin {

    /**
     * @author Antigravity
     * @reason Replace stream-heavy player search with a direct iteration for
     *         maximum performance.
     */
    @Overwrite
    protected void sense(ServerWorld world, LivingEntity entity) {
        List<PlayerEntity> players = new ObjectArrayList<>();
        List<? extends PlayerEntity> worldPlayers = world.getPlayers();

        for (int i = 0; i < worldPlayers.size(); i++) {
            PlayerEntity player = worldPlayers.get(i);
            if (player.isAlive()) {
                double distSq = entity.squaredDistanceTo(player);
                if (distSq < 16384.0D) { // 128 blocks
                    players.add(player);
                }
            }
        }

        players.sort(Comparator.comparingDouble(entity::squaredDistanceTo));

        entity.getBrain().remember(MemoryModuleType.NEAREST_PLAYERS, players);
        entity.getBrain().remember(MemoryModuleType.NEAREST_VISIBLE_PLAYER, players.isEmpty() ? null : players.get(0));
    }
}
