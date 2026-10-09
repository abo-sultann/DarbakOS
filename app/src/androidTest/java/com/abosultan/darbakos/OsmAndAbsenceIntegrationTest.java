package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.OsmAndBridge;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class OsmAndAbsenceIntegrationTest {
    @Test public void missingOsmAndIsReportedAsUnavailableWithoutCrash() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        OsmAndBridge bridge = new OsmAndBridge(context);
        assertEquals(OsmAndBridge.Availability.UNAVAILABLE, bridge.availability());
        assertNull(bridge.resolvedPackage());
        assertFalse(bridge.externalApiAvailable());
        assertNull(bridge.navigationInfoIntent());
        assertFalse(bridge.open());
    }
}
