package com.abosultan.darbakos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.abosultan.darbakos.core.CoreStateStore;
import com.abosultan.darbakos.core.LocalMediaIndex;
import com.abosultan.darbakos.core.LocalMediaLibrary;
import com.abosultan.darbakos.core.LocalMediaPlayer;
import com.abosultan.darbakos.core.LocalMediaQueue;
import com.abosultan.darbakos.core.LocalMediaState;
import com.abosultan.darbakos.core.LocalMediaTrack;
import com.abosultan.darbakos.core.LocationPermissionPolicy;
import com.abosultan.darbakos.core.MediaPermissionPolicy;
import com.abosultan.darbakos.core.MediaSessionBridge;
import com.abosultan.darbakos.core.MediaSnapshot;
import com.abosultan.darbakos.core.OsmAndBridge;
import com.abosultan.darbakos.core.OsmAndNavigationSnapshot;
import com.abosultan.darbakos.core.PositionFix;
import com.abosultan.darbakos.core.PositionStore;
import com.abosultan.darbakos.core.RecoveryReadiness;
import com.abosultan.darbakos.core.StartupCoordinator;
import com.abosultan.darbakos.core.TripStorageState;
import com.abosultan.darbakos.core.UpdatePackageInspector;
import com.abosultan.darbakos.core.VehicleDataStore;
import com.abosultan.darbakos.core.VehicleSnapshot;
import com.abosultan.darbakos.core.VehicleValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Darbak OS shell for continuous position/trip, OsmAnd and user-triggered external media control. */
public final class MainActivity extends Activity {
    private static final String STATE_SECTION = "section";
    private static final String USER_PREFS = "user_settings";
    private static final String PREF_SHOW_SPEED = "show_speed";
    private static final int REQUEST_LOCATION = 40;
    private static final int REQUEST_OSMAND_INFO = 41;
    private static final int REQUEST_MEDIA_STORAGE = 42;
    private static final long NAVIGATION_SNAPSHOT_FRESH_MS = 60_000L;
    private static final long VEHICLE_SNAPSHOT_FRESH_MS = 30_000L;
    private final VehicleDataStore vehicleDataStore = new VehicleDataStore();

    private static final int[] BUTTONS = {
        R.id.nav_home, R.id.nav_map, R.id.nav_media, R.id.nav_vehicle, R.id.nav_apps,
        R.id.settings_button
    };
    private static final int[] TITLES = {
        R.string.home, R.string.map, R.string.media, R.string.vehicle, R.string.apps,
        R.string.settings
    };
    private static final int[] DETAILS = {
        R.string.placeholder_note, R.string.map_detail, R.string.media_detail,
        R.string.vehicle_detail, R.string.apps_detail, R.string.settings_detail
    };

    private final PositionStore.Listener positionListener = new PositionStore.Listener() {
        @Override public void onPosition(final PositionFix fix) {
            runOnUiThread(() -> {
                showLiveSpeed(fix);
                if (section == 0) renderNavigationState();
                else if (section == 1) {
                    refreshMapLocationAction();
                    renderNavigationState();
                }
            });
        }

        @Override public void onUnavailable() {
            runOnUiThread(() -> {
                showSpeedUnavailable(R.string.gps_unavailable);
                if (section == 0) renderNavigationState();
                else if (section == 1) {
                    refreshMapLocationAction();
                    renderNavigationState();
                }
            });
        }
    };

    private final TripStorageState.Listener tripStorageListener = status ->
            runOnUiThread(this::renderActionableAlert);

    private int section;
    private boolean standby;
    private boolean permissionRequested;
    private boolean activityStarted;
    private boolean refreshRouteWhenResumed;
    private boolean infoRequestInFlight;
    private boolean osmandLaunchable;
    private boolean osmandExternalApi;
    private OsmAndBridge osmandBridge;
    private OsmAndNavigationSnapshot navigationSnapshot = OsmAndNavigationSnapshot.unknown(0L);
    private MediaSessionBridge mediaBridge;
    private MediaSnapshot mediaSnapshot = MediaSnapshot.accessUnavailable();
    private final LocalMediaQueue localQueue = new LocalMediaQueue();
    private final LocalMediaLibrary localLibrary = new LocalMediaLibrary();
    private LocalMediaPlayer localPlayer;
    private LocalMediaState localState;
    private LocalMediaIndex localIndex;
    private boolean localPlaying;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        osmandBridge = new OsmAndBridge(this);
        localState = new LocalMediaState(this);
        localIndex = new LocalMediaIndex(this);
        List<LocalMediaTrack> cachedTracks = localIndex.restoreValid();
        localQueue.replace(cachedTracks);
        localState.restoreSelection(localQueue);
        localPlayer = new LocalMediaPlayer(this, (track, playing, error) -> runOnUiThread(() -> {
            localPlaying = playing;
            if (track != null) localState.remember(track);
            renderLocalMediaState(track, error);
        }));
        refreshOsmAndCapabilities();
        mediaBridge = new MediaSessionBridge(this, snapshot -> runOnUiThread(() -> {
            mediaSnapshot = snapshot == null ? MediaSnapshot.idle() : snapshot;
            if (mediaSnapshot.playing && localPlaying) localPlayer.pauseForExternalPlayback();
            renderMediaState();
        }));

        // PositionStore belongs to the process/runtime, not this Activity instance.
        if (state == null) CoreStateStore.get().resetForColdBoot();

        for (int i = 0; i < BUTTONS.length; i++) {
            final int destination = i;
            findViewById(BUTTONS[i]).setOnClickListener(v -> showSection(destination));
        }
        findViewById(R.id.vehicle_back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.apps_manage).setOnClickListener(v -> openAppsSettings());
        findViewById(R.id.settings_back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.settings_speed_button).setOnClickListener(v -> toggleSpeedCard());
        findViewById(R.id.settings_standby_button).setOnClickListener(v -> enterStandby());
        findViewById(R.id.settings_title).setOnLongClickListener(v -> { showAdmin(); return true; });
        findViewById(R.id.admin_back_settings).setOnClickListener(v -> hideAdmin());
        findViewById(R.id.admin_inspect_update).setOnClickListener(v -> inspectLocalUpdate());
        findViewById(R.id.standby_exit).setOnLongClickListener(v -> { exitStandby(); return true; });
        findViewById(R.id.map_back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.media_back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.vehicle_back_home).setOnClickListener(v -> showSection(0));
        findViewById(R.id.quick_map).setOnClickListener(v -> showSection(1));
        findViewById(R.id.quick_media).setOnClickListener(v -> showSection(2));
        findViewById(R.id.quick_vehicle).setOnClickListener(v -> showSection(3));

        findViewById(R.id.map_open_button).setOnClickListener(v -> openOsmAnd());
        findViewById(R.id.map_location_button).setOnClickListener(v -> openCurrentLocation());
        findViewById(R.id.map_refresh_button).setOnClickListener(v -> requestNavigationInfo());
        findViewById(R.id.map_search_button).setOnClickListener(v -> searchDestination());
        ((EditText) findViewById(R.id.map_search_input)).setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDestination();
                return true;
            }
            return false;
        });

        findViewById(R.id.media_access_button).setOnClickListener(v -> openMediaAccessSettings());
        findViewById(R.id.media_play_pause_button).setOnClickListener(v -> {
            if (externalMediaActive()) {
                mediaBridge.playPause();
            } else if (localPlayer.currentTrack() != null) {
                // Toggle the existing player so a paused track resumes at its current position.
                localPlayer.playPause();
            } else if (localQueue.current() != null) {
                localPlayer.play(localQueue.current());
            }
        });
        findViewById(R.id.media_previous_button).setOnClickListener(v -> {
            if (externalMediaActive()) mediaBridge.previous();
            else {
                LocalMediaTrack track = localQueue.previous();
                if (track != null) localPlayer.play(track);
            }
        });
        findViewById(R.id.media_next_button).setOnClickListener(v -> {
            if (externalMediaActive()) mediaBridge.next();
            else {
                LocalMediaTrack track = localQueue.next();
                if (track != null) localPlayer.play(track);
            }
        });
        findViewById(R.id.media_local_scan_button).setOnClickListener(v -> scanLocalMedia());

        applyUserSettings();
        renderActionableAlert();
        showSection(state == null ? 0 : state.getInt(STATE_SECTION, 0));
        renderMediaState();
        enterFullscreen();
    }

    @Override protected void onStart() {
        super.onStart();
        activityStarted = true;
        PositionStore.get().addListener(positionListener);
        TripStorageState.get().addListener(tripStorageListener);
        if (mediaBridge != null) mediaBridge.start();

        LocationPermissionPolicy.Access access = locationAccess();
        if (access == LocationPermissionPolicy.Access.PRECISE) {
            showSpeedUnavailable(R.string.gps_waiting);
            startTripRuntime();
        } else {
            StartupCoordinator.stopPortableRuntime(this);
            showSpeedUnavailable(access == LocationPermissionPolicy.Access.APPROXIMATE
                    ? R.string.gps_precise_permission_needed : R.string.gps_permission_needed);
            if (!permissionRequested) {
                permissionRequested = true;
                requestPermissions(LocationPermissionPolicy.requestPermissions(), REQUEST_LOCATION);
            }
        }
        renderActionableAlert();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshOsmAndCapabilities();
        if (section == 1) renderMapPanel();
        else if (section == 0) renderNavigationState();
        else if (section == 2) renderMediaState();
        if (refreshRouteWhenResumed && !infoRequestInFlight) {
            refreshRouteWhenResumed = false;
            requestNavigationInfo();
        }
    }

    @Override protected void onDestroy() {
        localLibrary.close();
        if (localPlayer != null) localPlayer.release();
        super.onDestroy();
    }

    @Override protected void onStop() {
        activityStarted = false;
        PositionStore.get().removeListener(positionListener);
        TripStorageState.get().removeListener(tripStorageListener);
        if (mediaBridge != null) mediaBridge.stop();
        super.onStop();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                      int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MEDIA_STORAGE) {
            String permission = MediaPermissionPolicy.requiredPermission(Build.VERSION.SDK_INT);
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) scanLocalMedia();
            return;
        }
        if (requestCode != REQUEST_LOCATION) return;

        LocationPermissionPolicy.Access access = locationAccess();
        renderActionableAlert();
        if (access == LocationPermissionPolicy.Access.PRECISE) {
            showSpeedUnavailable(R.string.gps_waiting);
            startTripRuntime();
        } else {
            StartupCoordinator.stopPortableRuntime(this);
            showSpeedUnavailable(access == LocationPermissionPolicy.Access.APPROXIMATE
                    ? R.string.gps_precise_permission_needed : R.string.gps_permission_needed);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_OSMAND_INFO) return;
        infoRequestInFlight = false;
        if (resultCode == RESULT_OK) {
            navigationSnapshot = OsmAndBridge.parseNavigationInfo(
                    data, SystemClock.elapsedRealtime());
        } else {
            navigationSnapshot = OsmAndNavigationSnapshot.unknown(SystemClock.elapsedRealtime());
        }
        renderNavigationState();
        if (section == 1) refreshMapLocationAction();
    }

    private LocationPermissionPolicy.Access locationAccess() {
        boolean coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return LocationPermissionPolicy.evaluate(coarse, fine);
    }

    private void startTripRuntime() {
        boolean started = StartupCoordinator.startPortableRuntime(
                this,
                StartupCoordinator.Trigger.USER_LAUNCH,
                activityStarted,
                locationAccess() == LocationPermissionPolicy.Access.PRECISE);
        if (!started) showSpeedUnavailable(R.string.gps_unavailable);
    }

    private void openOsmAnd() {
        if (osmandBridge.open()) {
            refreshRouteWhenResumed = true;
            setMapFeedback("");
        } else {
            setMapFeedback(getString(R.string.map_osmand_required));
        }
    }

    private void openCurrentLocation() {
        PositionFix fix = PositionStore.get().available() ? PositionStore.get().latest() : null;
        if (fix == null) {
            setMapFeedback(getString(R.string.map_location_unavailable));
            return;
        }
        if (osmandBridge.openLocation(fix)) {
            refreshRouteWhenResumed = true;
            setMapFeedback("");
        } else {
            setMapFeedback(getString(R.string.map_osmand_required));
        }
    }

    private void searchDestination() {
        EditText search = (EditText) findViewById(R.id.map_search_input);
        String query = search.getText() == null ? "" : search.getText().toString().trim();
        if (query.length() == 0) {
            setMapFeedback(getString(R.string.map_search_empty));
            return;
        }
        PositionFix around = PositionStore.get().available() ? PositionStore.get().latest() : null;
        if (osmandBridge.openSearch(query, around)) {
            refreshRouteWhenResumed = true;
            setMapFeedback("");
        } else {
            setMapFeedback(getString(R.string.map_osmand_required));
        }
    }

    private void requestNavigationInfo() {
        if (infoRequestInFlight) return;
        Intent info = osmandBridge.navigationInfoIntent();
        if (info == null) {
            navigationSnapshot = OsmAndNavigationSnapshot.unknown(SystemClock.elapsedRealtime());
            renderNavigationState();
            if (section == 1) refreshMapLocationAction();
            return;
        }
        try {
            infoRequestInFlight = true;
            startActivityForResult(info, REQUEST_OSMAND_INFO);
        } catch (RuntimeException ignored) {
            infoRequestInFlight = false;
            navigationSnapshot = OsmAndNavigationSnapshot.unknown(SystemClock.elapsedRealtime());
            renderNavigationState();
            if (section == 1) refreshMapLocationAction();
        }
    }

    private void openMediaAccessSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (RuntimeException ignored) { }
    }

    private void showLiveSpeed(PositionFix fix) {
        TextView speed = (TextView) findViewById(R.id.speed_value);
        speed.setText(String.valueOf(fix.speedKmh()));
        speed.setContentDescription(getString(R.string.speed) + " " + fix.speedKmh()
                + " " + getString(R.string.kmh));
        ((TextView) findViewById(R.id.speed_source)).setText(R.string.gps_live);
    }

    private void showSpeedUnavailable(int statusText) {
        TextView speed = (TextView) findViewById(R.id.speed_value);
        speed.setText(R.string.speed_empty);
        speed.setContentDescription(getString(R.string.speed_accessibility));
        ((TextView) findViewById(R.id.speed_source)).setText(statusText);
    }

    private void showSection(int destination) {
        section = destination >= 0 && destination < TITLES.length ? destination : 0;
        boolean home = section == 0;
        boolean map = section == 1;
        boolean media = section == 2;
        boolean vehicle = section == 3;
        boolean settings = section == 5;
        findViewById(R.id.home_panel).setVisibility(home ? View.VISIBLE : View.GONE);
        findViewById(R.id.map_panel).setVisibility(map ? View.VISIBLE : View.GONE);
        findViewById(R.id.media_panel).setVisibility(media ? View.VISIBLE : View.GONE);
        findViewById(R.id.vehicle_panel).setVisibility(vehicle ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_panel).setVisibility(settings ? View.VISIBLE : View.GONE);
        findViewById(R.id.admin_panel).setVisibility(View.GONE);
        findViewById(R.id.section_panel).setVisibility(
                !home && !map && !media && !vehicle && !settings ? View.VISIBLE : View.GONE);
        findViewById(R.id.apps_preview).setVisibility(section == 4 ? View.VISIBLE : View.GONE);

        if (!home && !map && !media && !vehicle && !settings) {
            ((TextView) findViewById(R.id.section_title)).setText(TITLES[section]);
            ((TextView) findViewById(R.id.section_detail)).setText(DETAILS[section]);
        }
        if (map) {
            refreshOsmAndCapabilities();
            renderMapPanel();
        }
        if (media) renderMediaState();
        if (vehicle) renderVehicleState();
        if (section == 4) renderAppsState();
        if (settings) renderSettingsState();
        if (home) renderNavigationState();

        for (int i = 0; i < BUTTONS.length; i++) {
            findViewById(BUTTONS[i]).setSelected(i == section);
        }
    }

    private void renderActionableAlert() {
        renderActionableAlertState(locationAccess(), TripStorageState.get().status());
    }

    void renderActionableAlertState(boolean locationGranted) {
        renderActionableAlertState(
                locationGranted ? LocationPermissionPolicy.Access.PRECISE
                        : LocationPermissionPolicy.Access.DENIED,
                TripStorageState.Status.UNINITIALIZED);
    }

    void renderActionableAlertState(LocationPermissionPolicy.Access access,
                                    TripStorageState.Status storageStatus) {
        TextView alert = (TextView) findViewById(R.id.actionable_alert);
        int message = 0;
        if (access == LocationPermissionPolicy.Access.DENIED) {
            message = R.string.alert_location_permission;
        } else if (access == LocationPermissionPolicy.Access.APPROXIMATE) {
            message = R.string.alert_location_precise;
        } else if (storageStatus == TripStorageState.Status.WRITE_FAILED) {
            message = R.string.alert_trip_storage_failed;
        } else if (storageStatus == TripStorageState.Status.INTERNAL_FALLBACK) {
            message = R.string.alert_trip_storage_fallback;
        }
        if (message == 0) {
            alert.setText("");
            alert.setVisibility(View.GONE);
        } else {
            alert.setText(message);
            alert.setVisibility(View.VISIBLE);
        }
    }

    private void showAdmin() {
        findViewById(R.id.settings_panel).setVisibility(View.GONE);
        findViewById(R.id.admin_panel).setVisibility(View.VISIBLE);
        renderAdminDiagnostics();
    }

    private void hideAdmin() {
        findViewById(R.id.admin_panel).setVisibility(View.GONE);
        findViewById(R.id.settings_panel).setVisibility(View.VISIBLE);
    }

    private void renderAdminDiagnostics() {
        String versionName = "unknown";
        int versionCode = 0;
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = info.versionName == null ? "unknown" : info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) { }
        String model = android.os.Build.MODEL == null ? "unknown" : android.os.Build.MODEL;
        ((TextView) findViewById(R.id.admin_app)).setText(
                getString(R.string.admin_app, versionName, versionCode));
        ((TextView) findViewById(R.id.admin_device)).setText(
                getString(R.string.admin_device, android.os.Build.VERSION.RELEASE,
                        android.os.Build.VERSION.SDK_INT, model));
        ((TextView) findViewById(R.id.admin_update)).setText(R.string.admin_update_locked);
        ((TextView) findViewById(R.id.admin_recovery)).setText(R.string.admin_recovery_locked);
        renderRecoveryReadiness();
        ((TextView) findViewById(R.id.admin_update_inspection)).setText(R.string.admin_update_none);
    }

    private void renderRecoveryReadiness() {
        RecoveryReadiness readiness = RecoveryReadiness.evaluate("", "", false);
        int text;
        switch (readiness.state) {
            case LOCKED_NO_HASH: text = R.string.admin_recovery_no_hash; break;
            case LOCKED_PATH_UNVERIFIED: text = R.string.admin_recovery_path_unverified; break;
            case ELIGIBLE: text = R.string.admin_recovery_eligible; break;
            default: text = R.string.admin_recovery_no_backup;
        }
        ((TextView) findViewById(R.id.admin_recovery_readiness)).setText(text);
    }

    private void inspectLocalUpdate() {
        File candidate = new File(getExternalFilesDir(null), "DarbakOS-update.apk");
        TextView view = (TextView) findViewById(R.id.admin_update_inspection);
        if (!candidate.isFile()) { view.setText(R.string.admin_update_none); return; }
        UpdatePackageInspector.Result result = UpdatePackageInspector.inspect(this, candidate);
        String shortHash = result.sha256.length() >= 12
                ? result.sha256.substring(0, 12) : result.sha256;
        if (result.state == UpdatePackageInspector.State.COMPATIBLE) {
            view.setText(getString(R.string.admin_update_ok,
                    result.versionName.length() == 0
                            ? String.valueOf(result.versionCode) : result.versionName,
                    result.size, shortHash));
        } else if (result.state == UpdatePackageInspector.State.INCOMPATIBLE) {
            view.setText(getString(R.string.admin_update_bad, result.packageName, shortHash));
        } else view.setText(R.string.admin_update_unreadable);
    }

    private void enterStandby() {
        standby = true;
        findViewById(R.id.top_bar).setVisibility(View.GONE);
        findViewById(R.id.navigation).setVisibility(View.GONE);
        findViewById(R.id.home_panel).setVisibility(View.GONE);
        findViewById(R.id.map_panel).setVisibility(View.GONE);
        findViewById(R.id.media_panel).setVisibility(View.GONE);
        findViewById(R.id.vehicle_panel).setVisibility(View.GONE);
        findViewById(R.id.settings_panel).setVisibility(View.GONE);
        findViewById(R.id.admin_panel).setVisibility(View.GONE);
        findViewById(R.id.section_panel).setVisibility(View.GONE);
        findViewById(R.id.standby_panel).setVisibility(View.VISIBLE);
    }

    private void exitStandby() {
        if (!standby) return;
        standby = false;
        findViewById(R.id.standby_panel).setVisibility(View.GONE);
        findViewById(R.id.top_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.navigation).setVisibility(View.VISIBLE);
        showSection(0);
    }

    private SharedPreferences userPrefs() {
        return getSharedPreferences(USER_PREFS, MODE_PRIVATE);
    }

    private void applyUserSettings() {
        boolean showSpeed = userPrefs().getBoolean(PREF_SHOW_SPEED, true);
        findViewById(R.id.speed_card).setVisibility(showSpeed ? View.VISIBLE : View.GONE);
        renderSettingsState();
    }

    private void renderSettingsState() {
        View button = findViewById(R.id.settings_speed_button);
        if (!(button instanceof TextView)) return;
        boolean showSpeed = userPrefs().getBoolean(PREF_SHOW_SPEED, true);
        ((TextView) button).setText(
                showSpeed ? R.string.settings_speed_show : R.string.settings_speed_hide);
    }

    private void toggleSpeedCard() {
        boolean showSpeed = !userPrefs().getBoolean(PREF_SHOW_SPEED, true);
        userPrefs().edit().putBoolean(PREF_SHOW_SPEED, showSpeed).apply();
        findViewById(R.id.speed_card).setVisibility(showSpeed ? View.VISIBLE : View.GONE);
        renderSettingsState();
    }

    private void renderAppsState() {
        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = pm.queryIntentActivities(launcher, 0);
        List<String> labels = new ArrayList<>();
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null
                    || getPackageName().equals(info.activityInfo.packageName)) continue;
            CharSequence label = info.loadLabel(pm);
            labels.add(label == null ? info.activityInfo.packageName : label.toString());
        }
        Collections.sort(labels, String.CASE_INSENSITIVE_ORDER);
        TextView detail = (TextView) findViewById(R.id.section_detail);
        detail.setText(labels.isEmpty()
                ? getString(R.string.apps_none)
                : getString(R.string.apps_found, labels.size(), joinAppLabels(labels, 4)));
        View recent = findViewById(R.id.apps_recent);
        View favorite = findViewById(R.id.apps_favorite);
        recent.setEnabled(false);
        favorite.setEnabled(false);
        recent.setAlpha(0.55f);
        favorite.setAlpha(0.55f);
        View manage = findViewById(R.id.apps_manage);
        manage.setEnabled(true);
        manage.setAlpha(1f);
    }

    private String joinAppLabels(List<String> labels, int limit) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < labels.size() && i < limit; i++) {
            if (i > 0) out.append(" • ");
            out.append(labels.get(i));
        }
        return out.toString();
    }

    private void openAppsSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        } catch (RuntimeException ignored) { }
    }

    private void renderVehicleState() {
        VehicleSnapshot s = vehicleDataStore.snapshot(
                System.currentTimeMillis(), VEHICLE_SNAPSHOT_FRESH_MS);
        ((TextView) findViewById(R.id.vehicle_pressure)).setText(
                getString(R.string.vehicle_pressure_label, vehicleText(s.tirePressure)));
        ((TextView) findViewById(R.id.vehicle_tire_temperature)).setText(
                getString(R.string.vehicle_tire_temperature_label, vehicleText(s.tireTemperature)));
        ((TextView) findViewById(R.id.vehicle_fridge_temperature)).setText(
                getString(R.string.vehicle_fridge_temperature_label, vehicleText(s.fridgeTemperature)));
        VehicleValue v = s.tirePressure.available() ? s.tirePressure
                : (s.tireTemperature.available() ? s.tireTemperature : s.fridgeTemperature);
        ((TextView) findViewById(R.id.vehicle_source)).setText(v.available()
                ? getString(R.string.vehicle_source_label, v.source.name())
                : getString(R.string.vehicle_source_none));
    }

    private String vehicleText(VehicleValue v) {
        if (v == null || !v.available()) return getString(R.string.vehicle_unavailable);
        return String.format(Locale.US, "%.1f %s", v.value, v.unit);
    }

    private void renderMediaState() {
        MediaSnapshot snapshot = mediaSnapshot == null ? MediaSnapshot.idle() : mediaSnapshot;
        TextView homeTrack = (TextView) findViewById(R.id.media_track);
        TextView homeState = (TextView) findViewById(R.id.media_state);
        TextView title = (TextView) findViewById(R.id.media_now_title);
        TextView artist = (TextView) findViewById(R.id.media_now_artist);
        TextView status = (TextView) findViewById(R.id.media_now_status);
        TextView playPause = (TextView) findViewById(R.id.media_play_pause_button);
        View previous = findViewById(R.id.media_previous_button);
        View next = findViewById(R.id.media_next_button);
        View access = findViewById(R.id.media_access_button);

        if (snapshot.state == MediaSnapshot.State.ACCESS_UNAVAILABLE) {
            homeTrack.setText(R.string.media_no_session);
            homeState.setText(R.string.media_access_needed);
            title.setText(R.string.media_access_needed);
            artist.setText(R.string.media_access_detail);
            status.setText(R.string.media_position_test);
            playPause.setText(R.string.media_play);
            playPause.setEnabled(false);
            previous.setEnabled(false);
            next.setEnabled(false);
            access.setVisibility(View.VISIBLE);
            return;
        }

        access.setVisibility(View.GONE);
        if (snapshot.state == MediaSnapshot.State.IDLE) {
            homeTrack.setText(R.string.media_no_session);
            homeState.setText(R.string.media_position_test);
            title.setText(R.string.media_no_session);
            artist.setText(R.string.media_no_session_detail);
            status.setText(R.string.media_position_test);
            playPause.setText(R.string.media_play);
            playPause.setEnabled(false);
            previous.setEnabled(false);
            next.setEnabled(false);
            return;
        }

        String track = snapshot.title.length() == 0
                ? getString(R.string.media_unknown_track) : snapshot.title;
        String by = snapshot.artist.length() == 0
                ? getString(R.string.media_unknown_artist) : snapshot.artist;
        int stateText = snapshot.playing ? R.string.media_playing : R.string.media_paused;
        homeTrack.setText(track);
        homeState.setText(stateText);
        title.setText(track);
        artist.setText(by);
        status.setText(stateText);
        playPause.setText(snapshot.playing ? R.string.media_pause : R.string.media_play);
        playPause.setEnabled(snapshot.canPlayPause);
        previous.setEnabled(snapshot.canPrevious);
        next.setEnabled(snapshot.canNext);
    }

    private boolean externalMediaActive() {
        return mediaSnapshot != null && mediaSnapshot.state == MediaSnapshot.State.ACTIVE
                && (mediaSnapshot.playing || !hasLocalTrack());
    }

    private boolean hasLocalTrack() { return localQueue.current() != null; }

    private void scanLocalMedia() {
        String permission = MediaPermissionPolicy.requiredPermission(Build.VERSION.SDK_INT);
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { permission }, REQUEST_MEDIA_STORAGE);
            return;
        }
        ((TextView) findViewById(R.id.media_local_summary)).setText(R.string.media_local_scanning);
        findViewById(R.id.media_local_scan_button).setEnabled(false);
        localLibrary.scanSharedAudio(this, tracks -> runOnUiThread(() -> {
            if (tracks.isEmpty() && localPlayer.currentTrack() != null) localPlayer.stop();
            localQueue.replace(tracks);
            localIndex.save(tracks);
            localState.restoreSelection(localQueue);
            TextView summary = (TextView) findViewById(R.id.media_local_summary);
            if (tracks.isEmpty()) summary.setText(R.string.media_local_empty);
            else summary.setText(getString(R.string.media_local_count, tracks.size()));
            findViewById(R.id.media_local_scan_button).setEnabled(true);
            renderLocalMediaState(localQueue.current(), false);
        }));
    }

    private void renderLocalMediaState(LocalMediaTrack track, boolean error) {
        if (track == null || externalMediaActive()) { renderLocalControls(); return; }
        ((TextView) findViewById(R.id.media_track)).setText(track.title);
        ((TextView) findViewById(R.id.media_state)).setText(error
                ? R.string.media_local_error
                : localPlaying ? R.string.media_playing : R.string.media_stopped);
        ((TextView) findViewById(R.id.media_now_title)).setText(track.title);
        ((TextView) findViewById(R.id.media_now_artist)).setText(track.artist.length() == 0
                ? getString(R.string.media_local_source) : track.artist);
        ((TextView) findViewById(R.id.media_now_status)).setText(error
                ? R.string.media_local_error
                : localPlaying ? R.string.media_playing : R.string.media_stopped);
        renderLocalControls();
    }

    private void renderLocalControls() {
        if (externalMediaActive()) return;
        boolean hasTrack = localQueue.current() != null;
        TextView playPause = (TextView) findViewById(R.id.media_play_pause_button);
        playPause.setText(localPlaying ? R.string.media_pause : R.string.media_play);
        playPause.setEnabled(hasTrack);
        findViewById(R.id.media_previous_button).setEnabled(localQueue.size() > 1);
        findViewById(R.id.media_next_button).setEnabled(localQueue.size() > 1);
    }

    private void refreshOsmAndCapabilities() {
        osmandLaunchable = osmandBridge != null
                && osmandBridge.availability() == OsmAndBridge.Availability.LAUNCHABLE;
        osmandExternalApi = osmandLaunchable && osmandBridge.externalApiAvailable();
    }

    private void renderMapPanel() {
        ((TextView) findViewById(R.id.map_engine_state)).setText(
                !osmandLaunchable ? R.string.map_engine_missing
                        : osmandExternalApi ? R.string.map_engine_ready : R.string.map_engine_limited);

        findViewById(R.id.map_open_button).setEnabled(osmandLaunchable);
        findViewById(R.id.map_search_button).setEnabled(osmandLaunchable);
        findViewById(R.id.map_search_input).setEnabled(osmandLaunchable);
        findViewById(R.id.map_refresh_button).setEnabled(osmandExternalApi);
        refreshMapLocationAction();
        renderNavigationState();
        setMapFeedback("");
    }

    private void refreshMapLocationAction() {
        findViewById(R.id.map_location_button).setEnabled(
                osmandLaunchable && PositionStore.get().available()
                        && PositionStore.get().latest() != null);
    }

    private void renderNavigationState() {
        TextView homeInstruction = (TextView) findViewById(R.id.navigation_instruction);
        TextView homeDetail = (TextView) findViewById(R.id.navigation_eta);
        TextView homeState = (TextView) findViewById(R.id.navigation_state);
        TextView mapTitle = (TextView) findViewById(R.id.map_route_title);
        TextView mapDetail = (TextView) findViewById(R.id.map_route_detail);

        if (!osmandLaunchable) {
            homeInstruction.setText(R.string.map_route_unavailable);
            homeDetail.setText(R.string.map_osmand_required);
            homeState.setText(R.string.navigation_state);
            mapTitle.setText(R.string.map_route_unavailable);
            mapDetail.setText(R.string.map_osmand_required);
            return;
        }

        if (!osmandExternalApi) {
            homeInstruction.setText(R.string.map_route_unavailable);
            homeDetail.setText(R.string.map_route_limited_detail);
            homeState.setText(R.string.navigation_state);
            mapTitle.setText(R.string.map_route_unavailable);
            mapDetail.setText(R.string.map_route_limited_detail);
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (navigationSnapshot.state == OsmAndNavigationSnapshot.State.UNKNOWN
                || navigationSnapshot.stale(now, NAVIGATION_SNAPSHOT_FRESH_MS)) {
            homeInstruction.setText(R.string.map_route_unknown);
            homeDetail.setText(R.string.map_route_unknown_detail);
            homeState.setText(R.string.navigation_state_unknown);
            mapTitle.setText(R.string.map_route_unknown);
            mapDetail.setText(R.string.map_route_unknown_detail);
            return;
        }

        if (navigationSnapshot.state == OsmAndNavigationSnapshot.State.IDLE) {
            homeInstruction.setText(R.string.map_route_idle);
            homeDetail.setText(R.string.map_route_idle_detail);
            homeState.setText(R.string.navigation_state_ready);
            mapTitle.setText(R.string.map_route_idle);
            mapDetail.setText(R.string.map_route_idle_detail);
            return;
        }

        String title = navigationSnapshot.turnName.length() > 0
                ? navigationSnapshot.turnName : getString(R.string.map_route_active);
        String detail = formatRouteDetail(navigationSnapshot);
        homeInstruction.setText(title);
        homeDetail.setText(detail);
        homeState.setText(R.string.navigation_state_active);
        mapTitle.setText(title);
        mapDetail.setText(detail);
    }

    private String formatRouteDetail(OsmAndNavigationSnapshot snapshot) {
        StringBuilder value = new StringBuilder();
        if (snapshot.nextTurnDistanceMeters >= 0) {
            value.append("بعد ").append(formatDistance(snapshot.nextTurnDistanceMeters));
        }
        if (snapshot.distanceLeftMeters >= 0) {
            if (value.length() > 0) value.append(" • ");
            value.append("متبقي ").append(formatDistance(snapshot.distanceLeftMeters));
        }
        if (snapshot.timeLeftSeconds >= 0) {
            if (value.length() > 0) value.append(" • ");
            value.append(formatDuration(snapshot.timeLeftSeconds));
        }
        return value.length() == 0 ? getString(R.string.map_route_active) : value.toString();
    }

    private String formatDistance(int meters) {
        if (meters >= 1000) return String.format(Locale.US, "%.1f كم", meters / 1000f);
        return Math.max(0, meters) + " م";
    }

    private String formatDuration(int seconds) {
        int minutes = Math.max(1, (seconds + 59) / 60);
        if (minutes < 60) return minutes + " د";
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return remainingMinutes == 0
                ? hours + " س" : hours + " س " + remainingMinutes + " د";
    }

    private void setMapFeedback(String message) {
        TextView feedback = (TextView) findViewById(R.id.map_feedback);
        String value = message == null ? "" : message.trim();
        feedback.setText(value);
        feedback.setVisibility(value.length() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt(STATE_SECTION, section);
        super.onSaveInstanceState(state);
    }

    @Override public void onBackPressed() {
        if (standby) return;
        if (section != 0) showSection(0);
        else super.onBackPressed();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterFullscreen();
    }

    private void enterFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
