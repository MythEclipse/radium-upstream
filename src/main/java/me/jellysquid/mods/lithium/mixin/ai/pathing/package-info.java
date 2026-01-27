@MixinConfigOption(
        description = """
                A faster code path is used for determining what kind of path-finding node type is associated with a
                given block. Additionally, a faster chunk cache will be used for accessing blocks while evaluating
                paths.
                """,
        depends = @MixinConfigDependency(
                dependencyPath = "mixin.util.chunk_access"
        )
)
package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigDependency;
import me.jellysquid.mods.lithium.common.config.caffeine.MixinConfigOption;