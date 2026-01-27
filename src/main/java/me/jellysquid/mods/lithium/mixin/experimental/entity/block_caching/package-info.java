@MixinConfigOption(description = "Use block listening system to allow skipping stuff in entity code",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking.block_listening"))
package me.jellysquid.mods.lithium.mixin.experimental.entity.block_caching;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;