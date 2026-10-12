package com.abosultan.darbakos;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.abosultan.darbakos.core.TpmsDataStore;
import com.abosultan.darbakos.core.TpmsRuntime;
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

    @Test public void storeMergesNewestConnectedReadingPerWheelAndKeepsSpareSeparate() {
        TpmsDataStore store = new TpmsDataStore();
        store.register(adapter("old", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, "old", 30.0, 800L),
                reading(TpmsSourceSnapshot.Wheel.SPARE, "spare", 36.0, 850L))));
        store.register(adapter("new", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, "new", 35.0, 950L),
                reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, "rr", 33.0, 940L))));

        TpmsSourceSnapshot merged = store.snapshot();
        assertEquals(TpmsSourceSnapshot.State.LIVE, merged.state(1000L, 500L));
        assertEquals("new", merged.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).sensorId);
        assertEquals(35.0, merged.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT).pressurePsi, 0.001);
        assertEquals(36.0, merged.reading(TpmsSourceSnapshot.Wheel.SPARE).pressurePsi, 0.001);
        assertEquals(33.0, merged.reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT).pressurePsi, 0.001);
    }

    @Test public void faultedLastKnownReadingNeverOverridesConnectedSource() {
        TpmsDataStore store = new TpmsDataStore();
        store.register(adapter("fault", TpmsSourceSnapshot.error(
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "fault-old", 40.0, 990L))));
        store.register(adapter("live", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "live", 32.0, 900L))));

        TpmsSourceSnapshot merged = store.snapshot();
        assertEquals(TpmsSourceSnapshot.State.LIVE, merged.state(1000L, 500L));
        assertEquals("live", merged.reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT).sensorId);
        assertEquals(32.0, merged.reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT).pressurePsi, 0.001);
    }

    @Test public void allFaultedSourcesStayErrorAndMayKeepLastKnownEvidence() {
        TpmsDataStore store = new TpmsDataStore();
        store.register(adapter("fault", TpmsSourceSnapshot.error(
                reading(TpmsSourceSnapshot.Wheel.REAR_LEFT, "last-known", 31.0, 990L))));

        TpmsSourceSnapshot merged = store.snapshot();
        assertEquals(TpmsSourceSnapshot.State.ERROR, merged.state(1000L, 500L));
        assertEquals("last-known", merged.reading(TpmsSourceSnapshot.Wheel.REAR_LEFT).sensorId);
        assertFalse(merged.connected());
        assertTrue(merged.faulted());
    }

    @Test public void throwingAdapterBecomesErrorInsteadOfDisappearing() {
        TpmsDataStore store = new TpmsDataStore();
        store.register(new TpmsSourceAdapter() {
            public String id() { return "broken"; }
            public TpmsSourceSnapshot snapshot() { throw new IllegalStateException("boom"); }
        });
        assertEquals(TpmsSourceSnapshot.State.ERROR, store.snapshot().state(1000L, 500L));
    }

    @Test public void connectedNoDataRemainsExplicitEvenWithAnotherFaultedSource() {
        TpmsDataStore store = new TpmsDataStore();
        store.register(adapter("connected", TpmsSourceSnapshot.connected(
                new TpmsSourceSnapshot.Reading[0])));
        store.register(adapter("fault", TpmsSourceSnapshot.error(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, "old", 29.0, 990L))));
        TpmsSourceSnapshot merged = store.snapshot();
        assertEquals(TpmsSourceSnapshot.State.CONNECTED_NO_DATA, merged.state(1000L, 500L));
        assertNull(merged.reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT));
    }

    @Test public void runtimeExposesFiveWheelSnapshotWithoutCollapsingSpare() {
        TpmsRuntime runtime = new TpmsRuntime(new TpmsDataStore());
        runtime.registerSource(adapter("tpms", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, "fl", 34.0, 900L),
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "fr", 35.0, 910L),
                reading(TpmsSourceSnapshot.Wheel.REAR_LEFT, "rl", 33.0, 920L),
                reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, "rr", 32.0, 930L),
                reading(TpmsSourceSnapshot.Wheel.SPARE, "sp", 36.0, 940L))));
        TpmsSourceSnapshot snapshot = runtime.snapshot();
        assertEquals(5, snapshot.readings().size());
        assertEquals(36.0, snapshot.reading(TpmsSourceSnapshot.Wheel.SPARE).pressurePsi, 0.001);
        assertEquals(TpmsSourceSnapshot.State.LIVE, runtime.state(1000L, 500L));
    }

    @Test public void runtimeLiveReadingRejectsStaleAndFutureSamples() {
        TpmsRuntime runtime = new TpmsRuntime(new TpmsDataStore());
        runtime.registerSource(adapter("tpms", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, "stale", 30.0, 100L),
                reading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "future", 31.0, 1100L))));
        assertNull(runtime.liveReading(TpmsSourceSnapshot.Wheel.FRONT_LEFT, 1000L, 500L));
        assertNull(runtime.liveReading(TpmsSourceSnapshot.Wheel.FRONT_RIGHT, 1000L, 500L));
        assertNotNull(runtime.snapshot().reading(TpmsSourceSnapshot.Wheel.FRONT_LEFT));
    }

    @Test public void runtimeLiveReadingRejectsFaultedLastKnownEvidence() {
        TpmsRuntime runtime = new TpmsRuntime(new TpmsDataStore());
        runtime.registerSource(adapter("tpms", TpmsSourceSnapshot.error(
                reading(TpmsSourceSnapshot.Wheel.REAR_LEFT, "last", 29.0, 990L))));
        assertEquals(TpmsSourceSnapshot.State.ERROR, runtime.state(1000L, 500L));
        assertNull(runtime.liveReading(TpmsSourceSnapshot.Wheel.REAR_LEFT, 1000L, 500L));
        assertNotNull(runtime.snapshot().reading(TpmsSourceSnapshot.Wheel.REAR_LEFT));
    }

    @Test public void runtimeUnregisterReturnsToDisconnected() {
        TpmsRuntime runtime = new TpmsRuntime(new TpmsDataStore());
        runtime.registerSource(adapter("tpms", TpmsSourceSnapshot.connected(
                reading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, "rr", 33.0, 990L))));
        assertNotNull(runtime.liveReading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, 1000L, 500L));
        runtime.unregisterSource("tpms");
        assertEquals(TpmsSourceSnapshot.State.DISCONNECTED, runtime.state(1000L, 500L));
        assertNull(runtime.liveReading(TpmsSourceSnapshot.Wheel.REAR_RIGHT, 1000L, 500L));
    }

    private static TpmsSourceSnapshot.Reading reading(TpmsSourceSnapshot.Wheel wheel,
                                                       String id, double psi, long observedAtMs) {
        return new TpmsSourceSnapshot.Reading(wheel, id, psi, null, observedAtMs);
    }

    private static TpmsSourceAdapter adapter(final String id, final TpmsSourceSnapshot snapshot) {
        return new TpmsSourceAdapter() {
            public String id() { return id; }
            public TpmsSourceSnapshot snapshot() { return snapshot; }
        };
    }
}
