@MixinConfigOption(
        description = "BlockEntity Inventories update their listeners when a comparator is placed near them",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.block_entity_retrieval")
        }
)
package me.jellysquid.mods.lithium.mixin.util.inventory_comparator_tracking;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;