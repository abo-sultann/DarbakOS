package com.abosultan.darbakos.core;

/** Pure P4 acceptance and freshness policy shared by live position and trip recording. */
public final class PositionQualityPolicy {
    /** A 1 Hz GPS source is considered stale after 15 seconds without a newer fix. */
    public static final long MAX_FIX_AGE_MS = 15_000L;
    private static final float MAX_ACCURACY_METERS = 100f;
    private static final float MAX_SPEED_MPS = 70f; // 252 km/h; reject implausible car fixes.

    private PositionQualityPolicy() {}

    public static boolean isFresh(PositionFix fix, long nowMonotonicMs) {
        if (fix == null || nowMonotonicMs < 0L) return false;
        if (fix.monotonicMs <= 0L || fix.monotonicMs > nowMonotonicMs) return false;
        return nowMonotonicMs - fix.monotonicMs <= MAX_FIX_AGE_MS;
    }

    public static boolean isUsable(PositionFix fix, long nowMonotonicMs) {
        return isFresh(fix, nowMonotonicMs)
                && fix.accuracyMeters <= MAX_ACCURACY_METERS
                && fix.speedMetersPerSecond <= MAX_SPEED_MPS;
    }
}
