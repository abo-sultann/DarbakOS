package com.abosultan.darbakos.core;

/**
 * Read-only snapshot for the GDN technical diagnostics surface.
 *
 * It intentionally exposes only already-approved profile facts and capability decisions. It never
 * unlocks OEM ACC, CANBUS, boot, recovery, root, or destructive platform behavior.
 */
public final class HardwareProfileDiagnostics {
    public final String profileId;
    public final HardwareProfile.Provenance provenance;
    public final int productionApi;
    public final int displayWidthPx;
    public final int displayHeightPx;
    public final int minimumRamMb;
    public final int preferredRamMb;
    public final boolean removableMedia;
    public final boolean gnss;
    public final boolean standardAudio;
    public final boolean physicalCommissioningRequired;

    private HardwareProfileDiagnostics(HardwareProfile profile,
                                       DeviceCapabilityPolicy capabilities) {
        profileId = profile.id;
        provenance = profile.provenance;
        productionApi = profile.productionApi;
        displayWidthPx = profile.assumedDisplayWidthPx;
        displayHeightPx = profile.assumedDisplayHeightPx;
        minimumRamMb = profile.assumedMinimumRamMb;
        preferredRamMb = profile.preferredRamMb;
        removableMedia = capabilities.developRemovableMediaFlow();
        gnss = capabilities.developGnssFlow();
        standardAudio = capabilities.developStandardAudioFlow();
        physicalCommissioningRequired = capabilities.requiresPhysicalCommissioning();
    }

    public static HardwareProfileDiagnostics current() {
        return from(HardwareProfileStore.get().current());
    }

    public static HardwareProfileDiagnostics from(HardwareProfile profile) {
        HardwareProfile resolved = profile == null ? HardwareProfile.DEFAULT : profile;
        return new HardwareProfileDiagnostics(resolved, new DeviceCapabilityPolicy(resolved));
    }
}
