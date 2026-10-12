package com.abosultan.darbakos;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.abosultan.darbakos.core.TpmsSourceAdapter;
import com.abosultan.darbakos.core.TpmsSourceSnapshot;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class TpmsSourceContractTest {
    @Test public void disconnectedSourceNeverLooksLive() {
        TpmsSourceSnapshot snapshot = TpmsSourceSnapshot.disconnected();
        assertEquals(TpmsSourceSnapshot.State.DISCONNECTED, snapshot.state(1000L, 500L));
        assertFalse(snapshot.connected());
        assertTrue(snapshot.readings().isEmpty());
    }

    @Test public void connectedWithoutMeasurementsIsExplicit() {
        TpmsSourceSnapshot.Reading identityOnly = new TpmsSourceSnapshot.Reading(
                TpmsSourceSnapshot.Wheel.FRONT_LEFT, "sensor-a", null, null, 900L);
        TpmsSourceSnapshot snapshot = TpmsSourceSnapshot.connected(identityOnly);
        assertEquals(TpmsSourceSnapshot.State.CONNECTED_NO_DATA, snapshot.state(1000L, 500L));
        assertNull(snapshot.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).pressurePsi);
        assertNull(snapshot.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).temperatureC);
    }

    @Test public void freshSampleIsLiveAndKeepsOptionalFields() {
        TpmsSourceSnapshot.Reading reading = new TpmsSourceSnapshot.Reading(
                TpmsSourceSnapshot.Wheel.REAR_RIGHT, null, 34.5, null, 900L);
        TpmsSourceSnapshot snapshot = TpmsSourceSnapshot.connected(reading);
        assertEquals(TpmsSourceSnapshot.State.LIVE, snapshot.state(1000L, 500L));
        assertEquals(34.5, snapshot.reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT).pressurePsi, 0.001);
        assertNull(snapshot.reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT).temperatureC);
        assertNull(snapshot.reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT).sensorId);
    }

    @Test public void staleAndFutureSamplesNeverLookLive() {
        TpmsSourceSnapshot stale = TpmsSourceSnapshot.connected(new TpmsSourceSnapshot.Reading(
                TpmsSourceSnapshot.Wheel.SPARE, null, 30.0, 25.0, 100L));
        assertEquals(TpmsSourceSnapshot.State.STALE, stale.state(1000L, 500L));

        TpmsSourceSnapshot future = TpmsSourceSnapshot.connected(new TpmsSourceSnapshot.Reading(
                TpmsSourceSnapshot.Wheel.SPARE, null, 30.0, null, 1100L));
        assertEquals(TpmsSourceSnapshot.State.STALE, future.state(1000L, 500L));
    }

    @Test public void sourceErrorOverridesLastKnownReading() {
        TpmsSourceSnapshot snapshot = TpmsSourceSnapshot.error(new TpmsSourceSnapshot.Reading(
                TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "sensor-b", 33.0, 24.0, 990L));
        assertEquals(TpmsSourceSnapshot.State.ERROR, snapshot.state(1000L, 500L));
        assertTrue(snapshot.faulted());
        assertFalse(snapshot.connected());
    }

    @Test public void newestSampleWinsPerWheelWithoutInventingOtherWheels() {
        TpmsSourceSnapshot snapshot = TpmsSourceSnapshot.connected(
                new TpmsSourceSnapshot.Reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT,
                        "old", 30.0, null, 800L),
                new TpmsSourceSnapshot.Reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT,
                        "new", 35.0, 26.0, 950L));
        assertEquals("new", snapshot.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).sensorId);
        assertEquals(35.0, snapshot.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).pressurePsi, 0.001);
        assertNull(snapshot.reading(TpmsSourceSnapshot.Wheel.REAR_LEFT));
    }

    @Test public void adapterBoundaryDoesNotAssumeTransport() {
        TpmsSourceAdapter adapter = new TpmsSourceAdapter() {
            public String id() { return "proven-source"; }
            public TpmsSourceSnapshot snapshot() { return TpmsSourceSnapshot.disconnected(); }
        };
        assertEquals("proven-source", adapter.id());
        assertEquals(TpmsSourceSnapshot.State.DISCONNECTED, adapter.snapshot().state(1000L, 500L));
    }
}
