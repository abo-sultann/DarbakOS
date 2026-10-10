package com.abosultan.darbakos;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.abosultan.darbakos.core.HardwareProfileDiagnostics;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Read-only binder for the active head-unit profile on the technical Admin surface. */
public final class GdnApplication extends Application {
    private static final String PROFILE_MARKER = "\nProfile ";
    private static final Map<Activity, View> BOUND_PANELS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) { }
            @Override public void onActivityStarted(Activity activity) { bindAdminProfile(activity); }
            @Override public void onActivityResumed(Activity activity) {
                bindAdminProfile(activity);
                renderIfVisible(activity);
            }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
            @Override public void onActivityDestroyed(Activity activity) {
                BOUND_PANELS.remove(activity);
            }
        });
    }

    private static void bindAdminProfile(Activity activity) {
        if (activity == null) return;
        View panel = activity.findViewById(R.id.admin_panel);
        if (panel == null) return;
        if (BOUND_PANELS.get(activity) == panel) return;
        BOUND_PANELS.put(activity, panel);
        panel.addOnLayoutChangeListener((view, left, top, right, bottom,
                                         oldLeft, oldTop, oldRight, oldBottom) ->
                renderIfVisible(activity));
        renderIfVisible(activity);
    }

    private static void renderIfVisible(Activity activity) {
        View panel = activity.findViewById(R.id.admin_panel);
        TextView device = activity.findViewById(R.id.admin_device);
        if (panel == null || device == null || panel.getVisibility() != View.VISIBLE) return;
        device.setText(withProfile(device.getText(), HardwareProfileDiagnostics.current()));
    }

    static String withProfile(CharSequence deviceText, HardwareProfileDiagnostics profile) {
        String base = deviceText == null ? "" : deviceText.toString();
        int marker = base.indexOf(PROFILE_MARKER);
        if (marker >= 0) base = base.substring(0, marker);
        if (profile == null) return base;
        boolean assumed = profile.physicalCommissioningRequired;
        return base + String.format(Locale.US,
                "%s%s • %s • API%d • %dx%d • RAM %d/%d MB\n"
                        + "USB %s • GNSS %s • Audio %s\n"
                        + "Portable hardware: %s\n"
                        + "ACC/CANBUS/Boot/Recovery: PHYSICAL-LOCKED\n"
                        + "Physical %s",
                PROFILE_MARKER,
                profile.profileId,
                profile.provenance.name(),
                profile.productionApi,
                profile.displayWidthPx,
                profile.displayHeightPx,
                profile.minimumRamMb,
                profile.preferredRamMb,
                yesNo(profile.removableMedia),
                yesNo(profile.gnss),
                yesNo(profile.standardAudio),
                assumed ? "ASSUMED — VERIFY ON HEAD UNIT" : "MEASURED",
                assumed ? "PENDING" : "MEASURED");
    }

    private static String yesNo(boolean value) { return value ? "YES" : "NO"; }
}
