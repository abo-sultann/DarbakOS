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
public final class TpmsRuntimeContractTest {
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
