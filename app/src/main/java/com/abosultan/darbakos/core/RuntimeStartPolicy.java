package com.abosultan.darbakos.core;

import android.os.Build;

/**
 * Hardware-agnostic startup policy for the Darbak trip runtime.
 *
 * The Activity may start the runtime only while it is user-visible and location permission
 * has already been granted. OEM ACC/boot/CANBUS triggers deliberately do not live here; a
 * production head-unit adapter can be added later without changing the core runtime contract.
 */
public final class RuntimeStartPolicy {
    private RuntimeStartPolicy() { }

    public static boolean useForegroundServiceStart(int sdkInt) {
        return sdkInt >= Build.VERSION_CODES.O;
    }

    public static boolean allowActivityStart(boolean activityVisible,
                                             boolean locationPermissionGranted) {
        return activityVisible && locationPermissionGranted;
    }
}
