package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Shadow
    @Final
    private RegistryEntry<ChunkGeneratorSettings> settings;
    private int cachedSeaLevel = -1;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of
     * registry lookups.
     *
     * @author SuperCoder79
     * @reason avoid registry lookup
     */
    @Overwrite
    public int getSeaLevel() {
        if (this.cachedSeaLevel == -1) {
            this.cachedSeaLevel = this.settings.value().seaLevel();
        }
        return this.cachedSeaLevel;
    }
}
