@MixinConfigOption(
        description = "Avoids repeatedly testing whether the BlockEntity is inside the world border by caching the test result and listening for world border changes",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.world_border_listener")
        }
)
package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.world_border;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;