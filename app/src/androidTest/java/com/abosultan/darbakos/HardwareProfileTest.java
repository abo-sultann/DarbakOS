package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

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
        assertTrue(profile.requiresPhysicalCommissioning());
        assertEquals(HardwareProfile.AccBehavior.UNKNOWN_OEM_SLEEP_WAKE, profile.accBehavior);
        assertEquals(HardwareProfile.CanBusBehavior.UNKNOWN_OPTIONAL, profile.canBusBehavior);
        assertEquals(HardwareProfile.RecoveryState.UNKNOWN, profile.recoveryState);
        assertFalse(profile.allowGenericBootReceiver());
        assertFalse(profile.allowDestructivePlatformChanges());
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
        assertTrue(diagnostics.physicalCommissioningRequired);
    }
}
