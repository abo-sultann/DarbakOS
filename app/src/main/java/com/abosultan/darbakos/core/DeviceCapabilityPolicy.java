package com.abosultan.darbakos.core;

/**
 * Converts the active hardware profile into feature-level decisions.
 *
 * Expected portable capabilities may be developed against an assumed profile. OEM-specific or
 * destructive behavior is never enabled by assumptions and remains blocked until physical
 * commissioning provides measured evidence and a dedicated adapter is reviewed.
 */
public final class DeviceCapabilityPolicy {
    private final HardwareProfile profile;

    public DeviceCapabilityPolicy(HardwareProfile profile) {
        this.profile = profile == null ? HardwareProfile.DEFAULT : profile;
    }

    public HardwareProfile profile() {
        return profile;
    }

    public boolean developRemovableMediaFlow() {
        return profile.removableUsbExpected;
    }

    public boolean developGnssFlow() {
        return profile.internalGnssExpected;
    }

    public boolean developStandardAudioFlow() {
        return profile.standardAndroidAudioExpected;
    }

    public boolean requiresPhysicalCommissioning() {
        return profile.requiresPhysicalCommissioning();
    }

    public boolean allowOemAccAdapter() {
        return false;
    }

    public boolean allowCanBusAdapter() {
        return false;
    }

    public boolean allowGenericBootReceiver() {
        return profile.allowGenericBootReceiver();
    }

    public boolean allowDestructivePlatformChanges() {
        return profile.allowDestructivePlatformChanges();
    }
}
