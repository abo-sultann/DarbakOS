package com.abosultan.darbakos;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.abosultan.darbakos.core.CommissioningState;
import com.abosultan.darbakos.core.HardwareProfileDiagnostics;
import com.abosultan.darbakos.core.TpmsAlertPolicy;
import com.abosultan.darbakos.core.TpmsDisplayModel;
import com.abosultan.darbakos.core.TpmsPanelText;
import com.abosultan.darbakos.core.TpmsRuntime;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Read-only binders for hardware commissioning diagnostics and TPMS vehicle display. */
public final class GdnApplication extends Application {
    private static final String PROFILE_MARKER = "\nProfile ";
    // Provisional display policy only; physical head-unit commissioning must validate freshness.
    private static final long TPMS_DISPLAY_FRESH_MS = 15L * 60L * 1000L;
    // Provisional user policy, not an OEM-authoritative Toyota threshold.
    private static final double TPMS_LOW_PRESSURE_PSI = 25.0;
    private static final Map<Activity, View> BOUND_ADMIN_PANELS =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Activity, View> BOUND_VEHICLE_PANELS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) { }
            @Override public void onActivityStarted(Activity activity) { bindPanels(activity); }
            @Override public void onActivityResumed(Activity activity) {
                bindPanels(activity);
                renderAdminIfVisible(activity);
                renderTpmsIfVisible(activity);
            }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
            @Override public void onActivityDestroyed(Activity activity) {
                BOUND_ADMIN_PANELS.remove(activity);
                BOUND_VEHICLE_PANELS.remove(activity);
            }
        });
    }

    private static void bindPanels(Activity activity) {
        bindAdminProfile(activity);
        bindVehicleTpms(activity);
    }

    private static void bindAdminProfile(Activity activity) {
        if (activity == null) return;
        View panel = activity.findViewById(R.id.admin_panel);
        if (panel == null) return;
        if (BOUND_ADMIN_PANELS.get(activity) == panel) return;
        BOUND_ADMIN_PANELS.put(activity, panel);
        panel.addOnLayoutChangeListener((view, left, top, right, bottom,
                                         oldLeft, oldTop, oldRight, oldBottom) ->
                renderAdminIfVisible(activity));
        renderAdminIfVisible(activity);
    }

    private static void bindVehicleTpms(Activity activity) {
        if (activity == null) return;
        View panel = activity.findViewById(R.id.vehicle_panel);
        if (panel == null) return;
        if (BOUND_VEHICLE_PANELS.get(activity) == panel) return;
        BOUND_VEHICLE_PANELS.put(activity, panel);
        panel.addOnLayoutChangeListener((view, left, top, right, bottom,
                                         oldLeft, oldTop, oldRight, oldBottom) ->
                renderTpmsIfVisible(activity));
        renderTpmsIfVisible(activity);
    }

    private static void renderAdminIfVisible(Activity activity) {
        View panel = activity.findViewById(R.id.admin_panel);
        TextView device = activity.findViewById(R.id.admin_device);
        if (panel == null || device == null || panel.getVisibility() != View.VISIBLE) return;
        device.setText(withProfile(
                device.getText(),
                HardwareProfileDiagnostics.current(),
                CommissioningState.current()));
    }

    private static void renderTpmsIfVisible(Activity activity) {
        View panel = activity.findViewById(R.id.vehicle_panel);
        TextView pressure = activity.findViewById(R.id.vehicle_pressure);
        TextView temperature = activity.findViewById(R.id.vehicle_tire_temperature);
        TextView source = activity.findViewById(R.id.vehicle_source);
        if (panel == null || pressure == null || temperature == null || source == null
                || panel.getVisibility() != View.VISIBLE) return;

        long now = System.currentTimeMillis();
        TpmsDisplayModel model = TpmsDisplayModel.from(
                TpmsRuntime.get().snapshot(),
                now,
                TPMS_DISPLAY_FRESH_MS,
                TPMS_LOW_PRESSURE_PSI);
        TpmsAlertPolicy.Result alert = TpmsAlertPolicy.evaluate(model);
        pressure.setText(TpmsPanelText.pressure(model));
        temperature.setText(TpmsPanelText.temperature(model));
        source.setText(TpmsPanelText.source(model) + alertSuffix(alert));
    }

    private static String alertSuffix(TpmsAlertPolicy.Result alert) {
        if (alert == null) return "";
        if (alert.roadWarning()) return " • تحذير: ضغط منخفض في أحد الإطارات";
        if (alert.spareVisualNotice()) return " • تنبيه بصري: افحص ضغط الاسبير";
        return "";
    }

    static String withProfile(CharSequence deviceText,
                              HardwareProfileDiagnostics profile,
                              CommissioningState commissioning) {
        String base = deviceText == null ? "" : deviceText.toString();
        int marker = base.indexOf(PROFILE_MARKER);
        if (marker >= 0) base = base.substring(0, marker);
        if (profile == null || commissioning == null) return base;
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
                commissioning.portable == CommissioningState.PortableState.MEASURED
                        ? "MEASURED" : "ASSUMED — VERIFY ON HEAD UNIT",
                commissioning.physical.name());
    }

    private static String yesNo(boolean value) { return value ? "YES" : "NO"; }
}
