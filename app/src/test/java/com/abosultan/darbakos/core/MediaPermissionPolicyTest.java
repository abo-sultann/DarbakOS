package com.abosultan.darbakos.core;

import static org.junit.Assert.assertEquals;

import android.Manifest;

import org.junit.Test;

public final class MediaPermissionPolicyTest {
    @Test public void api25UsesLegacyExternalStoragePermission() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE,
                MediaPermissionPolicy.requiredPermission(25));
    }

    @Test public void api32StillUsesLegacyExternalStoragePermission() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE,
                MediaPermissionPolicy.requiredPermission(32));
    }

    @Test public void api33AndModernUseReadMediaAudio() {
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO,
                MediaPermissionPolicy.requiredPermission(33));
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO,
                MediaPermissionPolicy.requiredPermission(37));
    }
}
