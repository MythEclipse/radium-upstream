package me.jellysquid.mods.lithium.common.config.caffeine;

public @interface MixinConfigDependency {
    String dependencyPath();

    boolean enabled() default true;
}
