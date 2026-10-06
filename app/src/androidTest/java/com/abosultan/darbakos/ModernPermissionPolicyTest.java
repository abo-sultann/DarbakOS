package com.abosultan.darbakos;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.abosultan.darbakos.core.LocationPermissionPolicy;
import com.abosultan.darbakos.core.MediaPermissionPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ModernPermissionPolicyTest {
    @Test public void locationDistinguishesPreciseApproximateAndDenied() {
        assertEquals(LocationPermissionPolicy.Access.PRECISE,
                LocationPermissionPolicy.evaluate(true, true));
        assertEquals(LocationPermissionPolicy.Access.PRECISE,
                LocationPermissionPolicy.evaluate(false, true));
        assertEquals(LocationPermissionPolicy.Access.APPROXIMATE,
                LocationPermissionPolicy.evaluate(true, false));
        assertEquals(LocationPermissionPolicy.Access.DENIED,
                LocationPermissionPolicy.evaluate(false, false));
        assertArrayEquals(new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, LocationPermissionPolicy.requestPermissions());
    }

    @Test public void audioPermissionMovesToReadMediaAudioOnAndroid13Plus() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE,
                MediaPermissionPolicy.requiredPermission(25));
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE,
                MediaPermissionPolicy.requiredPermission(32));
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO,
                MediaPermissionPolicy.requiredPermission(33));
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO,
                MediaPermissionPolicy.requiredPermission(37));
    }
}
