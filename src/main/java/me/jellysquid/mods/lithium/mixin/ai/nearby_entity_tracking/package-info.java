@MixinConfigOption(
        description = """
                Event-based system for tracking nearby entities.
                """,
        depends = {
                @MixinConfigDependency(dependencyPath = "mixin.util.entity_section_position")
        },
        enabled = false //Disabled, because mspt increase in normal worlds has been measured consistently
)
package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;