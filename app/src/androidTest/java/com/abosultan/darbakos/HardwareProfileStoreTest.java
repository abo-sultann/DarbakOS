package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.DeviceCapabilityPolicy;
import com.abosultan.darbakos.core.HardwareProfile;
import com.abosultan.darbakos.core.HardwareProfileStore;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class HardwareProfileStoreTest {
    @Test public void measuredProfileCanReplaceDefaultWithoutUnlockingUnsafeBehavior() {
        HardwareProfileStore store = new HardwareProfileStore();
        assertEquals(HardwareProfile.Provenance.ASSUMED, store.current().provenance);
        assertFalse(store.replaceWithMeasured(HardwareProfile.DEFAULT));

        HardwareProfile measured = HardwareProfile.measured(
                "production-head-unit",
                37,
                2000,
                1200,
                6144,
                8192,
                true,
                true,
                true);
        assertTrue(store.replaceWithMeasured(measured));
        assertEquals(HardwareProfile.Provenance.MEASURED, store.current().provenance);
        assertEquals(2000, store.current().assumedDisplayWidthPx);
        assertFalse(store.current().requiresPhysicalCommissioning());

        DeviceCapabilityPolicy policy = new DeviceCapabilityPolicy(store.current());
        assertTrue(policy.developRemovableMediaFlow());
        assertTrue(policy.developGnssFlow());
        assertTrue(policy.developStandardAudioFlow());
        assertFalse(policy.allowOemAccAdapter());
        assertFalse(policy.allowCanBusAdapter());
        assertFalse(policy.allowGenericBootReceiver());
        assertFalse(policy.allowDestructivePlatformChanges());
    }
}
