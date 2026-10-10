package com.abosultan.darbakos.core;

/**
 * Single switch point between the development default and the measured production head unit.
 *
 * The process-wide store starts on HardwareProfile.DEFAULT. P11 may replace it only with a profile
 * whose provenance is MEASURED, keeping the rest of the app independent from concrete head-unit
 * values. Tests may still create isolated store instances directly.
 */
public final class HardwareProfileStore {
    private static final HardwareProfileStore ACTIVE = new HardwareProfileStore();

    private HardwareProfile current = HardwareProfile.DEFAULT;

    public static HardwareProfileStore get() {
        return ACTIVE;
    }

    public synchronized HardwareProfile current() {
        return current;
    }

    public synchronized boolean replaceWithMeasured(HardwareProfile measured) {
        if (measured == null || measured.provenance != HardwareProfile.Provenance.MEASURED) {
            return false;
        }
        current = measured;
        return true;
    }
}
