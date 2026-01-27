/**
 * This package includes a patch that stores the hashcode to avoid recalculating it.
 */
@MixinConfigOption(description = "BlockNeighborGroups used in fluid code cache their hashcode")
package me.jellysquid.mods.lithium.mixin.cached_hashcode;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;