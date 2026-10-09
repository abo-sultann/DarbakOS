package com.abosultan.darbakos.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight OsmAnd boundary for P4. GDN uses the official exported Android entry points
 * and OsmAnd external URI API without embedding/forking OsmAnd or copying its GPL AIDL sources.
 */
public final class OsmAndBridge {
    public static final String AIDL_SERVICE_ACTION = "net.osmand.aidl.OsmandAidlService";
    public static final String API_SCHEME = "osmand.api";

    private static final String API_GET_INFO = "get_info";
    private static final String API_SHOW_LOCATION = "show_location";
    private static final String API_NAVIGATE_SEARCH = "navigate_search";

    private static final String EXTRA_DESTINATION_LAT = "destination_lat";
    private static final String EXTRA_DESTINATION_LON = "destination_lon";
    private static final String EXTRA_ETA = "eta";
    private static final String EXTRA_TIME_LEFT = "time_left";
    // OsmAnd's public external API keeps this historical key name for remaining distance.
    private static final String EXTRA_DISTANCE_LEFT = "time_distance_left";
    private static final String EXTRA_CURRENT_TURN_NAME = "current_turn_name";
    private static final String EXTRA_CURRENT_TURN_TYPE = "current_turn_type";
    private static final String EXTRA_NEXT_TURN_NAME = "next_turn_name";
    private static final String EXTRA_NEXT_TURN_TYPE = "next_turn_type";
    private static final String EXTRA_NEXT_TURN_DISTANCE = "next_turn_distance";

    public enum Availability { UNAVAILABLE, LAUNCHABLE }

    private final Context context;
    private final PackageManager packages;

    public OsmAndBridge(Context context) {
        if (context == null) throw new IllegalArgumentException("context");
        this.context = context.getApplicationContext();
        this.packages = this.context.getPackageManager();
    }

    public Availability availability() {
        return resolvePackage() == null ? Availability.UNAVAILABLE : Availability.LAUNCHABLE;
    }

    public String resolvedPackage() {
        return resolvePackage();
    }

    /** Capability probe only. GDN does not vendor OsmAnd's AIDL source in this phase. */
    public boolean aidlServiceAvailable() {
        String packageName = resolvePackage();
        if (packageName == null) return false;
        Intent intent = new Intent(AIDL_SERVICE_ACTION);
        intent.setPackage(packageName);
        try {
            List<ResolveInfo> services = packages.queryIntentServices(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
            return services != null && !services.isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /** True only when the installed OsmAnd variant resolves its documented osmand.api scheme. */
    public boolean externalApiAvailable() {
        return resolveApiIntent(API_GET_INFO) != null;
    }

    /** Opens OsmAnd's normal launcher Activity only when one of the known variants resolves. */
    public boolean open() {
        String packageName = resolvePackage();
        if (packageName == null) return false;
        Intent launch = packages.getLaunchIntentForPackage(packageName);
        return startSafely(launch);
    }

    /** Opens a real validated location in OsmAnd; standard geo URI remains the compatibility fallback. */
    public boolean openLocation(PositionFix fix) {
        if (fix == null) return false;
        String packageName = resolvePackage();
        if (packageName == null) return false;

        Intent api = resolveApiIntent(API_SHOW_LOCATION,
                "lat", coordinate(fix.latitude),
                "lon", coordinate(fix.longitude));
        if (api != null && startSafely(api)) return true;

        Intent geo = new Intent(Intent.ACTION_VIEW, geoUri(fix.latitude, fix.longitude));
        geo.setPackage(packageName);
        if (geo.resolveActivity(packages) != null && startSafely(geo)) return true;
        return open();
    }

    /**
     * Opens OsmAnd destination search. With a GDN GPS fix, use the documented external API;
     * without a fix, use the standard geo search URI rather than inventing a search location.
     */
    public boolean openSearch(String query, PositionFix around) {
        String cleanQuery = query == null ? "" : query.trim();
        if (cleanQuery.length() == 0) return false;
        String packageName = resolvePackage();
        if (packageName == null) return false;

        if (around != null) {
            Intent api = resolveApiIntent(API_NAVIGATE_SEARCH,
                    "profile", "car",
                    "dest_search_query", cleanQuery,
                    "search_lat", coordinate(around.latitude),
                    "search_lon", coordinate(around.longitude),
                    "show_search_results", "true",
                    "location_permission", "false");
            if (api != null && startSafely(api)) return true;
        }

        Uri fallbackUri = Uri.parse("geo:0,0?q=" + Uri.encode(cleanQuery));
        Intent fallback = new Intent(Intent.ACTION_VIEW, fallbackUri);
        fallback.setPackage(packageName);
        return fallback.resolveActivity(packages) != null && startSafely(fallback);
    }

    /** Intent for startActivityForResult; OsmAnd get_info returns one truthful route snapshot. */
    public Intent navigationInfoIntent() {
        return resolveApiIntent(API_GET_INFO);
    }

    /** Parses only fields documented by OsmAnd's external get_info API. */
    public static OsmAndNavigationSnapshot parseNavigationInfo(Intent result,
                                                                long receivedElapsedMs) {
        if (result == null) return OsmAndNavigationSnapshot.unknown(receivedElapsedMs);
        if (!result.hasExtra(EXTRA_DESTINATION_LAT) || !result.hasExtra(EXTRA_DESTINATION_LON)) {
            return OsmAndNavigationSnapshot.idle(receivedElapsedMs);
        }

        double destinationLat = result.getDoubleExtra(EXTRA_DESTINATION_LAT, Double.NaN);
        double destinationLon = result.getDoubleExtra(EXTRA_DESTINATION_LON, Double.NaN);
        long eta = result.getLongExtra(EXTRA_ETA, 0L);
        int timeLeft = result.getIntExtra(EXTRA_TIME_LEFT, -1);
        int distanceLeft = result.getIntExtra(EXTRA_DISTANCE_LEFT, -1);
        int nextTurnDistance = result.getIntExtra(EXTRA_NEXT_TURN_DISTANCE, -1);

        String turnName = result.getStringExtra(EXTRA_CURRENT_TURN_NAME);
        String turnType = result.getStringExtra(EXTRA_CURRENT_TURN_TYPE);
        if (turnName == null || turnName.trim().length() == 0) {
            turnName = result.getStringExtra(EXTRA_NEXT_TURN_NAME);
        }
        if (turnType == null || turnType.trim().length() == 0) {
            turnType = result.getStringExtra(EXTRA_NEXT_TURN_TYPE);
        }

        return OsmAndNavigationSnapshot.active(receivedElapsedMs, destinationLat, destinationLon,
                eta, timeLeft, distanceLeft, nextTurnDistance, turnName, turnType);
    }

    static Uri geoUri(double latitude, double longitude) {
        String point = String.format(Locale.US, "%.6f,%.6f", latitude, longitude);
        return Uri.parse("geo:" + point + "?q=" + point);
    }

    private Intent resolveApiIntent(String command, String... queryPairs) {
        String packageName = resolvePackage();
        if (packageName == null) return null;
        Uri.Builder uri = new Uri.Builder().scheme(API_SCHEME).authority(command);
        if (queryPairs != null) {
            for (int i = 0; i + 1 < queryPairs.length; i += 2) {
                uri.appendQueryParameter(queryPairs[i], queryPairs[i + 1]);
            }
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri.build());
        intent.setPackage(packageName);
        try {
            return intent.resolveActivity(packages) == null ? null : intent;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String resolvePackage() {
        Set<String> launchable = new HashSet<>();
        for (String candidate : OsmAndPackages.ordered()) {
            try {
                Intent launch = packages.getLaunchIntentForPackage(candidate);
                if (launch != null && launch.resolveActivity(packages) != null) {
                    launchable.add(candidate);
                }
            } catch (RuntimeException ignored) {
                // Broken/partially installed packages remain unavailable instead of crashing GDN.
            }
        }
        return OsmAndPackages.select(launchable);
    }

    private boolean startSafely(Intent intent) {
        if (intent == null) return false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static String coordinate(double value) {
        return String.format(Locale.US, "%.7f", value);
    }
}
