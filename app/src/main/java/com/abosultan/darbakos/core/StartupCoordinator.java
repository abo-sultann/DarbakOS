package com.abosultan.darbakos.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Single boundary for Darbak runtime startup policy.
 *
 * P10 intentionally enables only a foreground USER_LAUNCH. Future OEM/ACC or
 * boot integrations must enter through this boundary after the exact head unit
 * and its recovery path are verified; they must not be hidden in Activities or
 * generic broadcast receivers.
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

    public static boolean startPortableRuntime(Context context, Trigger trigger) {
        if (context == null || !isPortableTrigger(trigger)) return false;
        Intent runtime = new Intent(context, TripRuntimeService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(runtime);
            } else {
                context.startService(runtime);
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
