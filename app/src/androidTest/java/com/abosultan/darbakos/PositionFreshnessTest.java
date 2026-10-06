package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.PositionFix;
import com.abosultan.darbakos.core.PositionQualityPolicy;
import com.abosultan.darbakos.core.PositionStore;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PositionFreshnessTest {
    @Test public void staleFixExpiresEvenWhenProviderDidNotReportUnavailable() {
        PositionStore store = PositionStore.get();
        store.resetForColdBoot();
        final int[] positions = {0};
        final int[] unavailable = {0};
        PositionStore.Listener listener = new PositionStore.Listener() {
            @Override public void onPosition(PositionFix fix) { positions[0]++; }
            @Override public void onUnavailable() { unavailable[0]++; }
        };
        store.addListener(listener);
        unavailable[0] = 0; // Initial empty-store callback is not the expiry event under test.

        long fixTime = 1_000L;
        PositionFix fix = PositionFix.create(24.7, 46.7, 5f, 10f, fixTime);
        store.publish(fix);
        assertEquals(1, positions[0]);
        assertTrue(store.availableAt(fixTime + PositionQualityPolicy.MAX_FIX_AGE_MS));
        assertFalse(store.expireIfStale(fixTime + PositionQualityPolicy.MAX_FIX_AGE_MS));

        assertTrue(store.expireIfStale(fixTime + PositionQualityPolicy.MAX_FIX_AGE_MS + 1L));
        assertFalse(store.availableAt(fixTime + PositionQualityPolicy.MAX_FIX_AGE_MS + 1L));
        assertEquals(1, unavailable[0]);
        assertFalse(store.expireIfStale(fixTime + PositionQualityPolicy.MAX_FIX_AGE_MS + 2L));

        store.removeListener(listener);
        store.resetForColdBoot();
    }
}
