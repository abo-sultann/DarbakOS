package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.PositionFix;
import com.abosultan.darbakos.core.TripAutoRecorder;
import com.abosultan.darbakos.core.TripChunkWriter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public final class TripStorageFailoverTest {
    private PositionFix fix(long time, double latitude) {
        PositionFix fix = PositionFix.createStamped(latitude, 46.6753, 4f, 10f,
                time, 1700000000000L + time);
        assertNotNull(fix);
        return fix;
    }

    @Test public void failedChunkRemainsBufferedAndCanCommitToFallback() throws Exception {
        File root = new File(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getCacheDir(), "trip-failover-" + System.nanoTime());
        assertTrue(root.mkdirs());
        File blocked = new File(root, "blocked-as-file");
        assertTrue(blocked.createNewFile());
        File fallback = new File(root, "fallback");

        TripAutoRecorder recorder = new TripAutoRecorder(blocked);
        IOException failure = null;
        for (int i = 1; i <= TripAutoRecorder.DEFAULT_CHUNK_POINTS; i++) {
            try {
                recorder.accept(fix(i * 1000L, 24.70 + i * 0.00001));
            } catch (IOException expected) {
                failure = expected;
                break;
            }
        }
        if (failure == null) fail("blocked storage must fail at the first full chunk");
        assertEquals(TripAutoRecorder.DEFAULT_CHUNK_POINTS, recorder.uncommittedPointCount());
        assertEquals(TripAutoRecorder.State.RECORDING, recorder.state());

        recorder.switchStorage(fallback);
        recorder.flush();
        assertEquals(0, recorder.uncommittedPointCount());
        File[] committed = fallback.listFiles(TripChunkWriter::isCompleteFile);
        assertNotNull(committed);
        assertEquals(1, committed.length);
    }

    @Test public void powerLossWindowIsExplicitlyBoundedByChunkSize() {
        assertEquals(TripAutoRecorder.DEFAULT_CHUNK_POINTS,
                TripAutoRecorder.MAX_UNCOMMITTED_POINTS);
        assertEquals(60, TripAutoRecorder.MAX_UNCOMMITTED_POINTS);
    }
}
