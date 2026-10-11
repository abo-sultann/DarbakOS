package com.abosultan.darbakos;

import android.graphics.Rect;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class ModernHeadUnitTest {
    @Test public void modernLandscapeShellNavigationFitsActualDisplay() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                View root=activity.findViewById(R.id.shell_root);
                assertTrue(root.getWidth() >= 1280);
                assertTrue(root.getHeight() >= 600);
                Rect bounds=new Rect(0,0,root.getWidth(),root.getHeight());
                assertWithin(root,bounds);
                int[] buttons={R.id.nav_map,R.id.nav_media,R.id.nav_vehicle,R.id.nav_apps,R.id.settings_button};
                int[] panels={R.id.map_panel,R.id.media_panel,R.id.vehicle_panel,R.id.section_panel,R.id.settings_panel};
                for(int i=0;i<buttons.length;i++){
                    activity.findViewById(buttons[i]).performClick();
                    View panel=activity.findViewById(panels[i]);
                    assertEquals(View.VISIBLE,panel.getVisibility());
                    assertWithin(panel,bounds);
                    activity.onBackPressed();
                    assertEquals(View.VISIBLE,activity.findViewById(R.id.home_panel).getVisibility());
                }
            });
        }
    }

    @Test public void mediaCopyDoesNotClaimUnverifiedRemovableHardware() {
        String source = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.media_local_source);
        String notScanned = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.media_local_not_scanned);

        assertEquals("مكتبة الوسائط المشتركة", source);
        assertTrue(notScanned.contains("مكتبة الوسائط المشتركة"));
        assertFalse(source.contains("USB"));
        assertFalse(source.contains("SD"));
        assertFalse(notScanned.contains("USB"));
        assertFalse(notScanned.contains("SD"));
    }

    @Test public void modernAdminKeepsPhysicalCommissioningPending() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.settings_button).performClick();
                assertTrue(activity.findViewById(R.id.settings_title).performLongClick());
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                String text = ((TextView) activity.findViewById(R.id.admin_device)).getText().toString();
                assertTrue(text.contains("Portable hardware: ASSUMED — VERIFY ON HEAD UNIT"));
                assertTrue(text.contains("ACC/CANBUS/Boot/Recovery: PHYSICAL-LOCKED"));
                assertTrue(text.contains("Physical PENDING"));
            });
        }
    }

    private static void assertWithin(View v, Rect bounds) {
        int[] p=new int[2]; v.getLocationOnScreen(p);
        assertTrue(p[0] >= bounds.left && p[1] >= bounds.top);
        assertTrue(p[0]+v.getWidth() <= bounds.right);
        assertTrue(p[1]+v.getHeight() <= bounds.bottom);
    }
}
