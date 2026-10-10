package com.abosultan.darbakos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.DeviceCapabilityPolicy;
import com.abosultan.darbakos.core.HardwareProfile;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class DeviceCapabilityPolicyTest {
    @Test public void defaultProfileAllowsPortableDevelopmentButBlocksOemAssumptions() {
        DeviceCapabilityPolicy policy = new DeviceCapabilityPolicy(HardwareProfile.DEFAULT);

        assertSame(HardwareProfile.DEFAULT, policy.profile());
        assertTrue(policy.developRemovableMediaFlow());
        assertTrue(policy.developGnssFlow());
        assertTrue(policy.developStandardAudioFlow());
        assertTrue(policy.requiresPhysicalCommissioning());
        assertFalse(policy.allowOemAccAdapter());
        assertFalse(policy.allowCanBusAdapter());
        assertFalse(policy.allowGenericBootReceiver());
        assertFalse(policy.allowDestructivePlatformChanges());
    }

    @Test public void nullProfileFallsBackToSafeDefault() {
        DeviceCapabilityPolicy policy = new DeviceCapabilityPolicy(null);
        assertSame(HardwareProfile.DEFAULT, policy.profile());
        assertFalse(policy.allowOemAccAdapter());
        assertFalse(policy.allowCanBusAdapter());
    }
}
