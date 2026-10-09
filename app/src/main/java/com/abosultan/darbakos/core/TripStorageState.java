package com.abosultan.darbakos.core;

import java.util.ArrayList;

/** Process-local storage health for the trip runtime; contains no destructive recovery action. */
public final class TripStorageState {
    public enum Status {
        UNINITIALIZED,
        EXTERNAL_READY,
        INTERNAL_READY,
        INTERNAL_FALLBACK,
        WRITE_FAILED
    }

    public interface Listener { void onTripStorageStatus(Status status); }

    private static final TripStorageState INSTANCE = new TripStorageState();
    public static TripStorageState get() { return INSTANCE; }

    private final ArrayList<Listener> listeners = new ArrayList<>();
    private Status status = Status.UNINITIALIZED;

    private TripStorageState() { }

    public synchronized Status status() { return status; }

    public void publish(Status next) {
        if (next == null) return;
        ArrayList<Listener> copy;
        synchronized (this) {
            if (status == next) return;
            status = next;
            copy = new ArrayList<>(listeners);
        }
        for (Listener listener : copy) listener.onTripStorageStatus(next);
    }

    public void addListener(Listener listener) {
        if (listener == null) return;
        Status current;
        synchronized (this) {
            if (!listeners.contains(listener)) listeners.add(listener);
            current = status;
        }
        listener.onTripStorageStatus(current);
    }

    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public synchronized void reset() { status = Status.UNINITIALIZED; }
}
