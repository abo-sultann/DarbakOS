package com.abosultan.darbakos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.TpmsDisplayModel;
import com.abosultan.darbakos.core.TpmsPanelText;
import com.abosultan.darbakos.core.TpmsSourceSnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class TpmsPanelTextContractTest {
    @Test public void pressureTextKeepsAllFiveWheelPositionsIncludingSpare() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, 32.0, 24.0, 990L),
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, 33.0, 25.0, 990L),
                reading(TpmsSourceSnapshot.Wheel.REAR_LEFT, 34.0, 26.0, 990L),
                reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, 35.0, 27.0, 990L),
                reading(TpmsSourceSnapshot.Wheel.SPARE, 36.0, 28.0, 990L)),
                1000L, 500L, 25.0);

        String text = TpmsPanelText.pressure(model);

        assertTrue(text.contains("أمامي يسار: 32.0 PSI"));
        assertTrue(text.contains("أمامي يمين: 33.0 PSI"));
        assertTrue(text.contains("خلفي يسار: 34.0 PSI"));
        assertTrue(text.contains("خلفي يمين: 35.0 PSI"));
        assertTrue(text.contains("الاسبير: 36.0 PSI"));
    }

    @Test public void staleLowEvidenceIsRenderedAsOldNotLiveLow() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.REAR_LEFT, 22.0, 23.0, 100L)),
                1000L, 500L, 25.0);

        String text = TpmsPanelText.pressure(model);

        assertTrue(text.contains("خلفي يسار: 22.0 PSI • قراءة قديمة"));
        assertFalse(text.contains("خلفي يسار: 22.0 PSI • منخفض"));
    }

    @Test public void sourceFailureStaysExplicitlyUnavailable() {
        TpmsDisplayModel model = TpmsDisplayModel.from(TpmsSourceSnapshot.error(
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, 31.0, 24.0, 990L)),
                1000L, 500L, 25.0);

        assertTrue(TpmsPanelText.pressure(model)
                .contains("أمامي يمين: 31.0 PSI • المصدر غير متاح"));
        assertTrue(TpmsPanelText.source(model).contains("خطأ في المصدر"));
    }

    @Test public void nullModelNeverInventsLiveMeasurements() {
        String pressure = TpmsPanelText.pressure(null);
        String temperature = TpmsPanelText.temperature(null);
        String source = TpmsPanelText.source(null);

        assertTrue(pressure.contains("أمامي يسار: -- • المصدر غير متاح"));
        assertTrue(pressure.contains("الاسبير: -- • المصدر غير متاح"));
        assertTrue(temperature.contains("الاسبير: -- • المصدر غير متاح"));
        assertTrue(source.contains("غير متصل"));
        assertFalse(pressure.contains(" PSI"));
        assertFalse(temperature.contains(" °C"));
    }

    private static TpmsSourceSnapshot.Reading reading(TpmsSourceSnapshot.Wheel wheel,
                                                       double psi,
                                                       double temperatureC,
                                                       long observedAtMs) {
        return new TpmsSourceSnapshot.Reading(
                wheel, wheel.name(), psi, temperatureC, observedAtMs);
    }
}
