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

    /**
     * Creates the bounded portable profile that will replace DEFAULT after P11 measures the real
     * unit. Measurement does not imply approval for ACC/CANBUS/recovery or completion of physical
     * commissioning.
     */
    public static HardwareProfile measured(String id,
                                           int productionApi,
                                           int displayWidthPx,
                                           int displayHeightPx,
                                           int minimumRamMb,
                                           int preferredRamMb,
                                           boolean removableUsbExpected,
                                           boolean internalGnssExpected,
                                           boolean standardAndroidAudioExpected) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id");
        if (productionApi < 25) throw new IllegalArgumentException("productionApi");
        if (displayWidthPx <= 0 || displayHeightPx <= 0) throw new IllegalArgumentException("display");
        if (minimumRamMb <= 0 || preferredRamMb < minimumRamMb) throw new IllegalArgumentException("ram");
        return new HardwareProfile(
                id.trim(),
                Provenance.MEASURED,
                productionApi,
                25,
                displayWidthPx,
                displayHeightPx,
                minimumRamMb,
                preferredRamMb,
                removableUsbExpected,
                internalGnssExpected,
                standardAndroidAudioExpected,
                AccBehavior.UNKNOWN_OEM_SLEEP_WAKE,
                CanBusBehavior.UNKNOWN_OPTIONAL,
                RecoveryState.UNKNOWN);
    }

    public boolean isLandscape() {
        return assumedDisplayWidthPx > assumedDisplayHeightPx;
    }

    public boolean portableMeasurementsComplete() {
        return provenance == Provenance.MEASURED;
    }

    /** Unsafe platform behavior never becomes enabled by an assumed or merely measured profile. */
    public boolean allowGenericBootReceiver() {
        return false;
    }

    /** Destructive platform work requires separately proven recovery/backup evidence. */
    public boolean allowDestructivePlatformChanges() {
        return false;
    }

    /**
     * Portable measurements are only one part of commissioning. ACC/sleep/wake, CANBUS and
     * recovery remain physical gates, so merely switching to a MEASURED profile cannot close P11.
     */
    public boolean requiresPhysicalCommissioning() {
        return true;
    }
}
