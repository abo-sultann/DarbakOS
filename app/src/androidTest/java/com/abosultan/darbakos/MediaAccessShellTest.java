package com.abosultan.darbakos;

import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Runs only after the focused P5 runner enables GDN's notification-listener access. */
@RunWith(AndroidJUnit4.class)
public final class MediaAccessShellTest {
    @Test public void grantedAccessWithNoSessionStaysIdleAndNeverAutoplays() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.nav_media).performClick();
                assertEquals(View.VISIBLE, activity.findViewById(R.id.media_panel).getVisibility());
                assertEquals("لا يوجد مقطع محدد",
                        ((TextView) activity.findViewById(R.id.media_now_title)).getText().toString());
                assertEquals("متوقف",
                        ((TextView) activity.findViewById(R.id.media_now_status)).getText().toString());
                assertEquals(View.GONE, activity.findViewById(R.id.media_access_button).getVisibility());
                assertFalse(activity.findViewById(R.id.media_play_pause_button).isEnabled());
                assertFalse(activity.findViewById(R.id.media_previous_button).isEnabled());
                assertFalse(activity.findViewById(R.id.media_next_button).isEnabled());
                assertEquals("لا يوجد مقطع محدد",
                        ((TextView) activity.findViewById(R.id.media_track)).getText().toString());
                assertEquals("متوقف",
                        ((TextView) activity.findViewById(R.id.media_state)).getText().toString());
            });
        }
    }
}
