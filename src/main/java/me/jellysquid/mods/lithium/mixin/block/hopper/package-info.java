/**
 * This package includes patches that replace the vanilla hopper code almost completely. Metadata about inventories is
 * stored in their stack lists, hopper cache various frequently used information and several update propagation systems
 * and modification counter comparisons make sure that no outdated cached information is used while avoiding quadratic
 * runtime.
 * <p>
 * <h2>Key Optimizations:</h2>
 * <ul>
 *   <li><b>LithiumStackList:</b> Replaces inventory stack lists with enhanced versions that cache signal strength,
 *       track modification counters, and store comparator update patterns.</li>
 *   <li><b>Inventory Caching:</b> Hoppers cache block entity inventories and validate via removal tracking,
 *       avoiding repeated world queries.</li>
 *   <li><b>Modification Counter Shortcuts:</b> Failed transfer attempts are skipped when both hopper and target
 *       inventory remain unchanged, with comparator side effects properly mimicked.</li>
 *   <li><b>Entity Movement Tracking:</b> Entity sections track when Inventory/ItemEntity classes move or change,
 *       allowing hoppers to skip unnecessary entity searches.</li>
 *   <li><b>Hopper Sleeping:</b> Hoppers sleep when idle and wake on relevant events (inventory changes, entity
 *       movement) via subscription-based notification.</li>
 * </ul>
 * <p>
 * See README.md in this package for detailed implementation documentation.
 */
@MixinConfigOption(
        description = "Reduces hopper lag using caching, notification systems and BlockEntity sleeping",
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.entity_movement_tracking"),
                @MixinConfigDependency(dependencyPath = "mixin.util.block_entity_retrieval"),
                @MixinConfigDependency(dependencyPath = "mixin.util.inventory_change_listening")
        },
        enabled = false
)
package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;