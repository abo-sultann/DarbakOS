package com.abosultan.darbakos.core;

/**
 * Process-local runtime access point for TPMS data.
 *
 * Consumers may inspect the raw merged snapshot for diagnostics, but user-facing/live behavior
 * should use liveReading(...) so stale, future, disconnected, or faulted samples never look live.
 * Transport selection remains outside this class.
 */
public final class TpmsRuntime {
    private static final TpmsRuntime INSTANCE = new TpmsRuntime(new TpmsDataStore());

    private final TpmsDataStore store;

    public TpmsRuntime(TpmsDataStore store) {
        if (store == null) throw new IllegalArgumentException("store required");
        this.store = store;
    }

    public static TpmsRuntime get() { return INSTANCE; }

    public void registerSource(TpmsSourceAdapter adapter) { store.register(adapter); }
    public void unregisterSource(String id) { store.unregister(id); }

    public TpmsSourceSnapshot snapshot() { return store.snapshot(); }

    public TpmsSourceSnapshot.State state(long nowMs, long maxAgeMs) {
        return snapshot().state(nowMs, maxAgeMs);
    }

    public TpmsSourceSnapshot.Reading liveReading(TpmsSourceSnapshot.Wheel wheel,
                                                   long nowMs,
                                                   long maxAgeMs) {
        if (wheel == null) return null;
        TpmsSourceSnapshot merged = snapshot();
        if (!merged.connected() || merged.faulted()) return null;
        TpmsSourceSnapshot.Reading reading = merged.reading(wheel);
        return reading != null && reading.fresh(nowMs, maxAgeMs) ? reading : null;
    }
}
