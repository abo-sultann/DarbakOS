package com.abosultan.darbakos.core;

import android.os.SystemClock;

import java.util.ArrayList;

/** Process-local latest position publication used by the GPS runtime and final Home UI. */
public final class PositionStore {
    public interface Listener {
        void onPosition(PositionFix fix);
        void onUnavailable();
    }

    private static final PositionStore INSTANCE = new PositionStore();
    public static PositionStore get() { return INSTANCE; }

    private final PositionState state = new PositionState();
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private boolean available;

    private PositionStore() { }

    public synchronized PositionFix latest() { return state.latest(); }

    public synchronized boolean available() {
        return availableAt(SystemClock.elapsedRealtime());
    }

    /** Pure-time overload used by focused tests and the runtime expiry check. */
    public synchronized boolean availableAt(long nowMonotonicMs) {
        return available && state.available()
                && PositionQualityPolicy.isFresh(state.latest(), nowMonotonicMs);
    }

    public void addListener(Listener listener) {
        if (listener == null) return;
        PositionFix current;
        boolean currentAvailable;
        synchronized (this) {
            if (!listeners.contains(listener)) listeners.add(listener);
            current = state.latest();
            currentAvailable = available && PositionQualityPolicy.isFresh(
                    current, SystemClock.elapsedRealtime());
        }
        if (currentAvailable) listener.onPosition(current);
        else listener.onUnavailable();
    }

    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void publish(PositionFix fix) {
        ArrayList<Listener> copy;
        synchronized (this) {
            if (!state.publish(fix)) return;
            available = true;
            copy = new ArrayList<>(listeners);
        }
        for (Listener listener : copy) listener.onPosition(fix);
    }

    public void publishUnavailable() {
        setUnavailableIfNeeded();
    }

    /**
     * Expires a source that stopped producing callbacks while the GPS provider remains enabled.
     * Returns true only when this call changed the public state to unavailable.
     */
    public boolean expireIfStale(long nowMonotonicMs) {
        synchronized (this) {
            if (!available || PositionQualityPolicy.isFresh(state.latest(), nowMonotonicMs)) {
                return false;
            }
        }
        return setUnavailableIfNeeded();
    }

    private boolean setUnavailableIfNeeded() {
        ArrayList<Listener> copy;
        synchronized (this) {
            if (!available) return false;
            available = false;
            copy = new ArrayList<>(listeners);
        }
        for (Listener listener : copy) listener.onUnavailable();
        return true;
    }

    public synchronized void resetForColdBoot() {
        state.resetForColdBoot();
        available = false;
    }
}
