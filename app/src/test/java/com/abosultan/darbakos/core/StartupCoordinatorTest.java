package com.abosultan.darbakos.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class StartupCoordinatorTest {
    @Test public void onlyUserLaunchIsPortableToday() {
        assertTrue(StartupCoordinator.isPortableTrigger(StartupCoordinator.Trigger.USER_LAUNCH));
        assertFalse(StartupCoordinator.isPortableTrigger(StartupCoordinator.Trigger.OEM_ACC));
        assertFalse(StartupCoordinator.isPortableTrigger(StartupCoordinator.Trigger.SYSTEM_BOOT));
    }
}
