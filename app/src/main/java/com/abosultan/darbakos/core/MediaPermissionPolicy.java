package com.abosultan.darbakos.core;

import android.Manifest;

/** Permission selection for shared audio while retaining the API25 regression floor. */
public final class MediaPermissionPolicy {
    private static final int ANDROID_13_API = 33;

    private MediaPermissionPolicy() { }

    public static String requiredPermission(int sdkInt) {
        return sdkInt >= ANDROID_13_API
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
    }
}
