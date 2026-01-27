/**
 * This package includes a patch that optimizes the redstone wire power level calculation by avoiding duplicate accesses
 * of the blocks around the redstone wire.
 */
@MixinConfigOption(description = "Redstone wire power calculations avoid duplicate block accesses")
package me.jellysquid.mods.lithium.mixin.block.redstone_wire;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;
