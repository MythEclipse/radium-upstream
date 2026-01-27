@MixinConfigOption(
        description = "Entity movement uses optimized block access and optimized and delayed entity access",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.chunk_access")
)
package me.jellysquid.mods.lithium.mixin.entity.collisions.movement;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;