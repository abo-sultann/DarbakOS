package com.abosultan.darbakos.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Single boundary for Darbak runtime startup policy.
 *
 * Portable P10 startup is deliberately limited to a visible user launch with precise location.
 * OEM ACC/boot integrations stay blocked until the exact production head unit is known and
 * validated; they must enter through this boundary rather than generic broadcast receivers.
 */
public final class StartupCoordinator {
    public enum Trigger {
        USER_LAUNCH,
        OEM_ACC,
        SYSTEM_BOOT
    }

    private StartupCoordinator() { }

    public static boolean isPortableTrigger(Trigger trigger) {
        return trigger == Trigger.USER_LAUNCH;
    }

    public static boolean canStart(Trigger trigger, boolean activityVisible,
                                   boolean preciseLocationGranted) {
        return isPortableTrigger(trigger)
                && RuntimeStartPolicy.allowActivityStart(activityVisible, preciseLocationGranted);
    }

    public static boolean startPortableRuntime(Context context, Trigger trigger,
                                               boolean activityVisible,
                                               boolean preciseLocationGranted) {
        if (context == null || !canStart(trigger, activityVisible, preciseLocationGranted)) {
            return false;
        }
        Intent runtime = new Intent(context, TripRuntimeService.class);
        try {
            if (RuntimeStartPolicy.useForegroundServiceStart(Build.VERSION.SDK_INT)) {
                startForegroundServiceApi26(context, runtime);
            } else {
                context.startService(runtime);
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void startForegroundServiceApi26(Context context, Intent runtime) {
        context.startForegroundService(runtime);
    }

    /** Used when a visible Activity observes that precise permission has been revoked. */
    public static void stopPortableRuntime(Context context) {
        if (context == null) return;
        try {
            context.stopService(new Intent(context, TripRuntimeService.class));
        } catch (RuntimeException ignored) { }
    }
}
