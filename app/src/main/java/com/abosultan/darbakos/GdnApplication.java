package com.abosultan.darbakos;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.abosultan.darbakos.core.HardwareProfileDiagnostics;

import java.util.Locale;

/** Read-only binder for the active head-unit profile on the technical Admin surface. */
public final class GdnApplication extends Application {
    private static final String PROFILE_MARKER = "\nProfile ";

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) {
                bindAdminProfile(activity);
            }
            @Override public void onActivityResumed(Activity activity) { renderIfVisible(activity); }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
    }

    private static void bindAdminProfile(Activity activity) {
        if (activity == null) return;
        View panel = activity.findViewById(R.id.admin_panel);
        if (panel == null) return;
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
        return base + String.format(Locale.US,
                "%s%s • %s • API%d • %dx%d • RAM %d/%d MB\nUSB %s • GNSS %s • Audio %s • Physical %s",
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
                profile.physicalCommissioningRequired ? "PENDING" : "MEASURED");
    }

    private static String yesNo(boolean value) { return value ? "YES" : "NO"; }
}
