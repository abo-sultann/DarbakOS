package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.OsmAndBridge;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class OsmAndVisibilityIntegrationTest {
    @Test public void launcherQueryAndOsmAndBridgeSeeInstalledFixture() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PackageManager packages = context.getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = packages.queryIntentActivities(launcher, 0);
        boolean visible = false;
        for (ResolveInfo info : resolved) {
            if (info.activityInfo != null && "net.osmand".equals(info.activityInfo.packageName)) {
                visible = true;
                break;
            }
        }
        assertTrue("launcher package visibility must include the installed fixture", visible);

        OsmAndBridge bridge = new OsmAndBridge(context);
        assertEquals("net.osmand", bridge.resolvedPackage());
        assertEquals(OsmAndBridge.Availability.LAUNCHABLE, bridge.availability());
        assertTrue(bridge.externalApiAvailable());
    }

    @Test public void mainActivityReceivesNavigationDataFromInstalledFixture() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.nav_map).performClick();
                View refresh = activity.findViewById(R.id.map_refresh_button);
                assertTrue(refresh.isEnabled());
                refresh.performClick();
            });
            Thread.sleep(500L);
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.map_route_title);
                TextView detail = activity.findViewById(R.id.map_route_detail);
                assertEquals("Fixture turn", title.getText().toString());
                assertTrue(detail.getText().toString().contains("4.2 كم"));
            });
        }
    }
}
