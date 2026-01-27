@MixinConfigOption(
        description = "Skips being pushed by fluids when the nearby chunk sections do not contain this fluid",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking")
        }
)
package me.jellysquid.mods.lithium.mixin.entity.collisions.fluid;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;