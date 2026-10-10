package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.CommissioningState;
import com.abosultan.darbakos.core.HardwareProfile;
import com.abosultan.darbakos.core.HardwareProfileDiagnostics;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class HardwareProfileTest {
    @Test public void defaultProfileIsModernReplaceableAndSafe() {
        HardwareProfile profile = HardwareProfile.DEFAULT;

        assertEquals(HardwareProfile.Provenance.ASSUMED, profile.provenance);
        assertEquals(37, profile.productionApi);
        assertEquals(25, profile.legacyFloorApi);
        assertEquals(1920, profile.assumedDisplayWidthPx);
        assertEquals(1080, profile.assumedDisplayHeightPx);
        assertEquals(4096, profile.assumedMinimumRamMb);
        assertEquals(8192, profile.preferredRamMb);
        assertTrue(profile.isLandscape());
        assertTrue(profile.removableUsbExpected);
        assertTrue(profile.internalGnssExpected);
        assertTrue(profile.standardAndroidAudioExpected);
        assertFalse(profile.portableMeasurementsComplete());
        assertTrue(profile.requiresPhysicalCommissioning());
        assertEquals(HardwareProfile.AccBehavior.UNKNOWN_OEM_SLEEP_WAKE, profile.accBehavior);
        assertEquals(HardwareProfile.CanBusBehavior.UNKNOWN_OPTIONAL, profile.canBusBehavior);
        assertEquals(HardwareProfile.RecoveryState.UNKNOWN, profile.recoveryState);
        assertFalse(profile.allowGenericBootReceiver());
        assertFalse(profile.allowDestructivePlatformChanges());
    }

    @Test public void measuredPortableProfileDoesNotCompletePhysicalCommissioning() {
        HardwareProfile measured = HardwareProfile.measured(
                "measured-head-unit",
                37,
                1920,
                1080,
                4096,
                8192,
                true,
                true,
                true);

        assertEquals(HardwareProfile.Provenance.MEASURED, measured.provenance);
        assertTrue(measured.portableMeasurementsComplete());
        assertTrue(measured.requiresPhysicalCommissioning());
        assertFalse(measured.allowGenericBootReceiver());
        assertFalse(measured.allowDestructivePlatformChanges());

        HardwareProfileDiagnostics diagnostics = HardwareProfileDiagnostics.from(measured);
        assertTrue(diagnostics.portableMeasurementsComplete);
        assertTrue(diagnostics.physicalCommissioningRequired);

        CommissioningState commissioning = CommissioningState.from(measured);
        assertEquals(CommissioningState.PortableState.MEASURED, commissioning.portable);
        assertEquals(CommissioningState.PhysicalState.PENDING, commissioning.physical);
        assertTrue(commissioning.portableMeasurementsComplete());
        assertFalse(commissioning.physicalCommissioningComplete());
    }

    @Test public void defaultCommissioningStateRemainsAssumedAndPhysicalPending() {
        CommissioningState commissioning = CommissioningState.from(HardwareProfile.DEFAULT);

        assertEquals(CommissioningState.PortableState.ASSUMED, commissioning.portable);
        assertEquals(CommissioningState.PhysicalState.PENDING, commissioning.physical);
        assertFalse(commissioning.portableMeasurementsComplete());
        assertFalse(commissioning.physicalCommissioningComplete());
    }

    @Test public void diagnosticsExposeOnlyCurrentPortableProfileFacts() {
        HardwareProfileDiagnostics diagnostics = HardwareProfileDiagnostics.from(HardwareProfile.DEFAULT);

        assertEquals("default-modern-head-unit", diagnostics.profileId);
        assertEquals(HardwareProfile.Provenance.ASSUMED, diagnostics.provenance);
        assertEquals(37, diagnostics.productionApi);
        assertEquals(1920, diagnostics.displayWidthPx);
        assertEquals(1080, diagnostics.displayHeightPx);
        assertEquals(4096, diagnostics.minimumRamMb);
        assertEquals(8192, diagnostics.preferredRamMb);
        assertTrue(diagnostics.removableMedia);
        assertTrue(diagnostics.gnss);
        assertTrue(diagnostics.standardAudio);
        assertFalse(diagnostics.portableMeasurementsComplete);
        assertTrue(diagnostics.physicalCommissioningRequired);
    }
}
