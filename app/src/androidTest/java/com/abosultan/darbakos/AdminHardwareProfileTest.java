package com.abosultan.darbakos;

import static org.junit.Assert.assertTrue;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.HardwareProfile;
import com.abosultan.darbakos.core.HardwareProfileDiagnostics;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AdminHardwareProfileTest {
    @Test public void assumedHardwareProfileIsVisibleInTechnicalAdmin() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.settings_button).performClick();
                assertTrue(activity.findViewById(R.id.settings_title).performLongClick());
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                String text = ((TextView) activity.findViewById(R.id.admin_device)).getText().toString();
                assertTrue(text.contains("Profile default-modern-head-unit"));
                assertTrue(text.contains("ASSUMED"));
                assertTrue(text.contains("API37"));
                assertTrue(text.contains("1920x1080"));
                assertTrue(text.contains("USB YES"));
                assertTrue(text.contains("GNSS YES"));
                assertTrue(text.contains("Audio YES"));
                assertTrue(text.contains("Portable hardware: ASSUMED — VERIFY ON HEAD UNIT"));
                assertTrue(text.contains("ACC/CANBUS/Boot/Recovery: PHYSICAL-LOCKED"));
                assertTrue(text.contains("Physical PENDING"));
            });
        }
    }

    @Test public void measuredPortableProfileStillShowsPhysicalPending() {
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
        String text = GdnApplication.withProfile(
                "Android 17 • API37",
                HardwareProfileDiagnostics.from(measured));

        assertTrue(text.contains("Profile measured-head-unit"));
        assertTrue(text.contains("MEASURED"));
        assertTrue(text.contains("Portable hardware: MEASURED"));
        assertTrue(text.contains("ACC/CANBUS/Boot/Recovery: PHYSICAL-LOCKED"));
        assertTrue(text.contains("Physical PENDING"));
    }
}
