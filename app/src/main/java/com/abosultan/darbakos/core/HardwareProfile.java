package com.abosultan.darbakos.core;

/**
 * Replaceable head-unit assumptions used while the production unit is not yet available.
 *
 * These values are development defaults, not measured hardware facts. Code may use them for
 * layout/test planning, but OEM-specific integration must remain disabled until P11 captures the
 * exact production device and a measured profile replaces this default.
 */
public final class HardwareProfile {
    public enum Provenance { ASSUMED, MEASURED }
    public enum AccBehavior { UNKNOWN_OEM_SLEEP_WAKE }
    public enum CanBusBehavior { UNKNOWN_OPTIONAL }
    public enum RecoveryState { UNKNOWN }

    public static final HardwareProfile DEFAULT = new HardwareProfile(
            "default-modern-head-unit",
            Provenance.ASSUMED,
            37,
            25,
            1920,
            1080,
            4096,
            8192,
            true,
            true,
            true,
            AccBehavior.UNKNOWN_OEM_SLEEP_WAKE,
            CanBusBehavior.UNKNOWN_OPTIONAL,
            RecoveryState.UNKNOWN);

    public final String id;
    public final Provenance provenance;
    public final int productionApi;
    public final int legacyFloorApi;
    public final int assumedDisplayWidthPx;
    public final int assumedDisplayHeightPx;
    public final int assumedMinimumRamMb;
    public final int preferredRamMb;
    public final boolean removableUsbExpected;
    public final boolean internalGnssExpected;
    public final boolean standardAndroidAudioExpected;
    public final AccBehavior accBehavior;
    public final CanBusBehavior canBusBehavior;
    public final RecoveryState recoveryState;

    private HardwareProfile(String id,
                            Provenance provenance,
                            int productionApi,
                            int legacyFloorApi,
                            int assumedDisplayWidthPx,
                            int assumedDisplayHeightPx,
                            int assumedMinimumRamMb,
                            int preferredRamMb,
                            boolean removableUsbExpected,
                            boolean internalGnssExpected,
                            boolean standardAndroidAudioExpected,
                            AccBehavior accBehavior,
                            CanBusBehavior canBusBehavior,
                            RecoveryState recoveryState) {
        this.id = id;
        this.provenance = provenance;
        this.productionApi = productionApi;
        this.legacyFloorApi = legacyFloorApi;
        this.assumedDisplayWidthPx = assumedDisplayWidthPx;
        this.assumedDisplayHeightPx = assumedDisplayHeightPx;
        this.assumedMinimumRamMb = assumedMinimumRamMb;
        this.preferredRamMb = preferredRamMb;
        this.removableUsbExpected = removableUsbExpected;
        this.internalGnssExpected = internalGnssExpected;
        this.standardAndroidAudioExpected = standardAndroidAudioExpected;
        this.accBehavior = accBehavior;
        this.canBusBehavior = canBusBehavior;
        this.recoveryState = recoveryState;
    }

    public boolean isLandscape() {
        return assumedDisplayWidthPx > assumedDisplayHeightPx;
    }

    /** Unsafe platform behavior never becomes enabled by an assumed profile. */
    public boolean allowGenericBootReceiver() {
        return false;
    }

    /** Destructive platform work requires measured recovery/backup evidence, never defaults. */
    public boolean allowDestructivePlatformChanges() {
        return false;
    }

    public boolean requiresPhysicalCommissioning() {
        return provenance != Provenance.MEASURED;
    }
}
