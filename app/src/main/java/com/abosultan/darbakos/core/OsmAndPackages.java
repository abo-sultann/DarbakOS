package com.abosultan.darbakos.core;

import java.util.Set;

/** Known official OsmAnd Android package variants, ordered for GDN preference. */
public final class OsmAndPackages {
    public static final String FULL = "net.osmand.plus";
    public static final String FREE = "net.osmand";
    public static final String NIGHTLY = "net.osmand.dev";

    private static final String[] ORDERED = { FULL, FREE, NIGHTLY };

    private OsmAndPackages() { }

    public static String[] ordered() {
        return ORDERED.clone();
    }

    /** Pure selection helper used by the Android bridge and focused tests. */
    public static String select(Set<String> installed) {
        if (installed == null || installed.isEmpty()) return null;
        for (String candidate : ORDERED) {
            if (installed.contains(candidate)) return candidate;
        }
        return null;
    }

    public static boolean isKnown(String packageName) {
        if (packageName == null) return false;
        for (String candidate : ORDERED) {
            if (candidate.equals(packageName)) return true;
        }
        return false;
    }
}
