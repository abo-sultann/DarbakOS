package com.abosultan.darbakos;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.CommissioningState;
import com.abosultan.darbakos.core.HardwareProfile;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class CommissioningStateTest {
    @Test public void defaultProfileKeepsPhysicalCommissioningPending() {
        CommissioningState state = CommissioningState.from(HardwareProfile.DEFAULT);

        assertEquals(CommissioningState.PortableState.ASSUMED, state.portable);
        assertEquals(CommissioningState.PhysicalState.PENDING, state.physical);
        assertFalse(state.portableMeasurementsComplete());
        assertFalse(state.physicalCommissioningComplete());
    }

    @Test public void measuredPortableProfileStillKeepsPhysicalCommissioningPending() {
        HardwareProfile measured = HardwareProfile.measured(
                "measured-test",
                37,
                1920,
                1080,
                4096,
                8192,
                true,
                true,
                true);
        CommissioningState state = CommissioningState.from(measured);

        assertEquals(CommissioningState.PortableState.MEASURED, state.portable);
        assertEquals(CommissioningState.PhysicalState.PENDING, state.physical);
        assertTrue(state.portableMeasurementsComplete());
        assertFalse(state.physicalCommissioningComplete());
    }

    @Test public void nullProfileFallsBackToSafePendingState() {
        CommissioningState state = CommissioningState.from(null);

        assertEquals(CommissioningState.PortableState.ASSUMED, state.portable);
        assertEquals(CommissioningState.PhysicalState.PENDING, state.physical);
        assertFalse(state.physicalCommissioningComplete());
    }
}
