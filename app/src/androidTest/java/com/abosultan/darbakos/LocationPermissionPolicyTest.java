package com.abosultan.darbakos;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.LocationPermissionPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class LocationPermissionPolicyTest {
    @Test public void finePermissionIsPreciseAccess() {
        assertEquals(LocationPermissionPolicy.Access.PRECISE,
                LocationPermissionPolicy.evaluate(true, true));
        assertEquals(LocationPermissionPolicy.Access.PRECISE,
                LocationPermissionPolicy.evaluate(false, true));
    }

    @Test public void coarseOnlyIsApproximateAccess() {
        assertEquals(LocationPermissionPolicy.Access.APPROXIMATE,
                LocationPermissionPolicy.evaluate(true, false));
    }

    @Test public void noPermissionIsDenied() {
        assertEquals(LocationPermissionPolicy.Access.DENIED,
                LocationPermissionPolicy.evaluate(false, false));
    }

    @Test public void preciseRequestIncludesCoarseAndFineTogether() {
        assertArrayEquals(new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, LocationPermissionPolicy.requestPermissions());
    }
}
