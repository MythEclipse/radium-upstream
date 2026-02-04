package me.jellysquid.mods.lithium.common.util;

import net.minecraft.registry.Registries;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class POIRegistryEntries {
        // Using a separate class, so the registry lookup happens after the registry is
        // initialized
        @SuppressWarnings("deprecation")
        public static final RegistryEntry<PointOfInterestType> NETHER_PORTAL_ENTRY = Registries.POINT_OF_INTEREST_TYPE
                        .entryOf(PointOfInterestTypes.NETHER_PORTAL);
        @SuppressWarnings("deprecation")
        public static final RegistryEntry<PointOfInterestType> HOME_ENTRY = Registries.POINT_OF_INTEREST_TYPE
                        .entryOf(PointOfInterestTypes.HOME);
}
