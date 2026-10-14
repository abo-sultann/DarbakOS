package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.TpmsAlertPolicy;
import com.abosultan.darbakos.core.TpmsDisplayModel;
import com.abosultan.darbakos.core.TpmsSourceSnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class TpmsAlertPolicyContractTest {
    @Test public void spareLowPressureIsTheOnlySparePressureNotice() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.SPARE, 22.0, 990L)),
                1000L, 500L, 25.0);

        TpmsAlertPolicy.Result result = TpmsAlertPolicy.evaluate(model);

        assertTrue(result.spareLowPressureNotice());
        assertTrue(result.spareVisualNotice());
        assertFalse(result.roadWarning());
        assertEquals(TpmsAlertPolicy.Severity.INFO, result.severity);
    }

    @Test public void staleSpareEvidenceNeverLooksLikeLowPressureNotice() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.SPARE, 22.0, 100L)),
                1000L, 500L, 25.0);

        TpmsAlertPolicy.Result result = TpmsAlertPolicy.evaluate(model);

        assertEquals(TpmsDisplayModel.WheelState.STALE,
                result.wheel(TpmsSourceSnapshot.Wheel.SPARE).state);
        assertEquals(TpmsAlertPolicy.Severity.INFO,
                result.wheel(TpmsSourceSnapshot.Wheel.SPARE).severity);
        assertFalse(result.wheel(TpmsSourceSnapshot.Wheel.SPARE).visualOnly);
        assertFalse(result.spareLowPressureNotice());
        assertFalse(result.spareVisualNotice());
    }

    @Test public void unavailableSpareNeverLooksLikeLowPressureNotice() {
        TpmsDisplayModel model = TpmsDisplayModel.from(
                TpmsSourceSnapshot.disconnected(), 1000L, 500L, 25.0);

        TpmsAlertPolicy.Result result = TpmsAlertPolicy.evaluate(model);

        assertEquals(TpmsDisplayModel.WheelState.SOURCE_UNAVAILABLE,
                result.wheel(TpmsSourceSnapshot.Wheel.SPARE).state);
        assertFalse(result.wheel(TpmsSourceSnapshot.Wheel.SPARE).visualOnly);
        assertFalse(result.spareLowPressureNotice());
        assertFalse(result.roadWarning());
    }

    @Test public void roadWarningOutranksInformationalWheelStates() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, 24.0, 990L),
                reading(TpmsSourceSnapshot.Wheel.SPARE, 22.0, 100L)),
                1000L, 500L, 25.0);

        TpmsAlertPolicy.Result result = TpmsAlertPolicy.evaluate(model);

        assertEquals(TpmsAlertPolicy.Severity.WARNING, result.severity);
        assertTrue(result.roadWarning());
        assertFalse(result.spareLowPressureNotice());
    }

    private static TpmsSourceSnapshot.Reading reading(TpmsSourceSnapshot.Wheel wheel,
                                                       double psi,
                                                       long observedAtMs) {
        return new TpmsSourceSnapshot.Reading(wheel, wheel.name(), psi, null, observedAtMs);
    }
}
