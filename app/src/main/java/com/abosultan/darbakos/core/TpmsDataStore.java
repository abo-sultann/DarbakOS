package com.abosultan.darbakos.core;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Process-local TPMS merger. Only readings from currently connected sources may become live.
 * Faulted sources may retain last-known evidence, but that evidence never overrides a connected source.
 */
public final class TpmsDataStore {
    private final Map<String, TpmsSourceAdapter> adapters = new LinkedHashMap<>();

    public synchronized void register(TpmsSourceAdapter adapter) {
        if (adapter == null || adapter.id() == null || adapter.id().trim().isEmpty()) return;
        adapters.put(adapter.id(), adapter);
    }

    public synchronized void unregister(String id) {
        if (id != null) adapters.remove(id);
    }

    public synchronized TpmsSourceSnapshot snapshot() {
        EnumMap<TpmsSourceSnapshot.Wheel, TpmsSourceSnapshot.Reading> connectedReadings =
                new EnumMap<>(TpmsSourceSnapshot.Wheel.class);
        EnumMap<TpmsSourceSnapshot.Wheel, TpmsSourceSnapshot.Reading> faultReadings =
                new EnumMap<>(TpmsSourceSnapshot.Wheel.class);
        boolean anyConnected = false;
        boolean anyFault = false;

        for (TpmsSourceAdapter adapter : adapters.values()) {
            TpmsSourceSnapshot source;
            try {
                source = adapter.snapshot();
            } catch (RuntimeException ignored) {
                anyFault = true;
                continue;
            }
            if (source == null) {
                anyFault = true;
                continue;
            }
            if (source.connected()) {
                anyConnected = true;
                mergeNewest(connectedReadings, source);
            } else if (source.faulted()) {
                anyFault = true;
                mergeNewest(faultReadings, source);
            }
        }

        if (anyConnected) {
            return TpmsSourceSnapshot.connected(toArray(connectedReadings));
        }
        if (anyFault) {
            return TpmsSourceSnapshot.error(toArray(faultReadings));
        }
        return TpmsSourceSnapshot.disconnected();
    }

    private static void mergeNewest(
            EnumMap<TpmsSourceSnapshot.Wheel, TpmsSourceSnapshot.Reading> target,
            TpmsSourceSnapshot source) {
        for (TpmsSourceSnapshot.Reading reading : source.readings().values()) {
            if (reading == null) continue;
            TpmsSourceSnapshot.Reading current = target.get(reading.wheel);
            if (current == null || reading.observedAtMs > current.observedAtMs) {
                target.put(reading.wheel, reading);
            }
        }
    }

    private static TpmsSourceSnapshot.Reading[] toArray(
            EnumMap<TpmsSourceSnapshot.Wheel, TpmsSourceSnapshot.Reading> readings) {
        return readings.values().toArray(new TpmsSourceSnapshot.Reading[readings.size()]);
    }
}
