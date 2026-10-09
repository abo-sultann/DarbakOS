package com.abosultan.darbakos;

import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Runs only while the separate mediafixture APK owns an active external MediaSession. */
@RunWith(AndroidJUnit4.class)
public final class ExternalMediaIntegrationTest {
    @Test public void observesPausedExternalSessionWithoutAutoplayThenControlsIt() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> activity.findViewById(R.id.nav_media).performClick());

            awaitText(scenario, R.id.media_now_title, "GDN Fixture");
            awaitText(scenario, R.id.media_now_artist, "GDN CI Fixture");
            awaitText(scenario, R.id.media_now_status, "متوقف مؤقتًا");
            // Reaching PAUSED after GDN attached proves observing did not auto-start playback.
            scenario.onActivity(activity -> {
                assertTrue(activity.findViewById(R.id.media_play_pause_button).isEnabled());
                assertTrue(activity.findViewById(R.id.media_previous_button).isEnabled());
                assertTrue(activity.findViewById(R.id.media_next_button).isEnabled());
                assertEquals("متوقف مؤقتًا",
                        ((TextView) activity.findViewById(R.id.media_state)).getText().toString());
                activity.findViewById(R.id.media_play_pause_button).performClick();
            });

            awaitText(scenario, R.id.media_now_status, "قيد التشغيل");
            scenario.onActivity(activity -> activity.findViewById(R.id.media_next_button).performClick());
            awaitText(scenario, R.id.media_now_title, "GDN Fixture Next");
            scenario.onActivity(activity -> {
                assertEquals("GDN Fixture Next",
                        ((TextView) activity.findViewById(R.id.media_track)).getText().toString());
                assertEquals("قيد التشغيل",
                        ((TextView) activity.findViewById(R.id.media_state)).getText().toString());
            });
        }
    }

    private static void awaitText(ActivityScenario<MainActivity> scenario, int id, String expected)
            throws InterruptedException {
        for (int i = 0; i < 60; i++) {
            AtomicBoolean match = new AtomicBoolean(false);
            scenario.onActivity(activity -> match.set(expected.equals(
                    ((TextView) activity.findViewById(id)).getText().toString())));
            if (match.get()) return;
            Thread.sleep(50L);
        }
        scenario.onActivity(activity -> assertEquals(expected,
                ((TextView) activity.findViewById(id)).getText().toString()));
    }
}
