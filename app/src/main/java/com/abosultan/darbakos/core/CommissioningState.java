package com.abosultan.darbakos.core;

/**
 * Read-only commissioning state derived from the active hardware profile.
 *
 * Portable measurements and vehicle-specific physical commissioning are intentionally separate.
 * A measured portable profile must never unlock ACC, CANBUS, boot/wake, recovery, root, or other
 * destructive platform behavior by itself.
 */
public final class CommissioningState {
    public enum PortableState { ASSUMED, MEASURED }
    public enum PhysicalState { PENDING }

    public final PortableState portable;
    public final PhysicalState physical;

    private CommissioningState(PortableState portable, PhysicalState physical) {
        this.portable = portable;
        this.physical = physical;
    }

    public static CommissioningState current() {
        return from(HardwareProfileStore.get().current());
    }

    public static CommissioningState from(HardwareProfile profile) {
        HardwareProfile resolved = profile == null ? HardwareProfile.DEFAULT : profile;
        PortableState portable = resolved.portableMeasurementsComplete()
                ? PortableState.MEASURED
                : PortableState.ASSUMED;
        return new CommissioningState(portable, PhysicalState.PENDING);
    }

    public boolean portableMeasurementsComplete() {
        return portable == PortableState.MEASURED;
    }

    public boolean physicalCommissioningComplete() {
        return false;
    }
}
