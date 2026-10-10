package com.abosultan.darbakos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.DeviceCapabilityPolicy;
import com.abosultan.darbakos.core.HardwareProfile;
import com.abosultan.darbakos.core.StartupCoordinator;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class StartupCoordinatorTest {
    @Test public void onlyVisiblePreciseUserLaunchCanStartPortableRuntime() {
        assertTrue(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.USER_LAUNCH, true, true));
        assertFalse(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.USER_LAUNCH, true, false));
        assertFalse(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.USER_LAUNCH, false, true));
        assertFalse(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.OEM_ACC, true, true));
        assertFalse(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.SYSTEM_BOOT, true, true));
    }

    @Test public void profileWithoutGnssBlocksPortableRuntime() {
        HardwareProfile measuredWithoutGnss = HardwareProfile.measured(
                "no-gnss-head-unit",
                37,
                1920,
                1080,
                4096,
                8192,
                true,
                false,
                true);
        DeviceCapabilityPolicy capabilities = new DeviceCapabilityPolicy(measuredWithoutGnss);

        assertFalse(StartupCoordinator.canStart(
                StartupCoordinator.Trigger.USER_LAUNCH, true, true, capabilities));
    }
}
