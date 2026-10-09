package com.abosultan.darbakos.core;

import android.Manifest;

/** Pure permission-state policy for Android's precise/approximate location model. */
public final class LocationPermissionPolicy {
    public enum Access { PRECISE, APPROXIMATE, DENIED }

    private LocationPermissionPolicy() { }

    public static Access evaluate(boolean coarseGranted, boolean fineGranted) {
        if (fineGranted) return Access.PRECISE;
        if (coarseGranted) return Access.APPROXIMATE;
        return Access.DENIED;
    }

    /** Android 12+ requires coarse and fine to be requested together for precise location. */
    public static String[] requestPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }
}
