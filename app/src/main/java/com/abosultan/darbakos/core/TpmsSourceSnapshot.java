package com.abosultan.darbakos.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Truthful TPMS snapshot independent of the eventual ESP32/head-unit transport.
 * Connection state and sample freshness are kept separate so stale or failed sources never look live.
 */
public final class TpmsSourceSnapshot {
    public enum Wheel { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT, SPARE }
    public enum State { DISCONNECTED, CONNECTED_NO_DATA, LIVE, STALE, ERROR }

    public static final class Reading {
        public final Wheel wheel;
        public final String sensorId;
        public final Double pressurePsi;
        public final Double temperatureC;
        public final long observedAtMs;

        public Reading(Wheel wheel, String sensorId, Double pressurePsi,
                       Double temperatureC, long observedAtMs) {
            if (wheel == null) throw new IllegalArgumentException("wheel required");
            this.wheel = wheel;
            this.sensorId = sensorId == null || sensorId.trim().isEmpty() ? null : sensorId.trim();
            this.pressurePsi = pressurePsi;
            this.temperatureC = temperatureC;
            this.observedAtMs = observedAtMs;
        }

        public boolean hasMeasurement() {
            return pressurePsi != null || temperatureC != null;
        }

        public boolean fresh(long nowMs, long maxAgeMs) {
            return hasMeasurement() && observedAtMs > 0L && maxAgeMs >= 0L
                    && nowMs >= observedAtMs && nowMs - observedAtMs <= maxAgeMs;
        }
    }

    private final boolean connected;
    private final boolean faulted;
    private final Map<Wheel, Reading> readings;

    private TpmsSourceSnapshot(boolean connected, boolean faulted, Reading... values) {
        this.connected = connected;
        this.faulted = faulted;
        EnumMap<Wheel, Reading> merged = new EnumMap<>(Wheel.class);
        if (values != null) {
            for (Reading reading : values) {
                if (reading == null) continue;
                Reading current = merged.get(reading.wheel);
                if (current == null || reading.observedAtMs > current.observedAtMs) {
                    merged.put(reading.wheel, reading);
                }
            }
        }
        readings = Collections.unmodifiableMap(merged);
    }

    public static TpmsSourceSnapshot disconnected() {
        return new TpmsSourceSnapshot(false, false);
    }

    public static TpmsSourceSnapshot connected(Reading... readings) {
        return new TpmsSourceSnapshot(true, false, readings);
    }

    public static TpmsSourceSnapshot error(Reading... lastKnownReadings) {
        return new TpmsSourceSnapshot(false, true, lastKnownReadings);
    }

    public boolean connected() { return connected; }
    public boolean faulted() { return faulted; }
    public Map<Wheel, Reading> readings() { return readings; }
    public Reading reading(Wheel wheel) { return wheel == null ? null : readings.get(wheel); }

    public State state(long nowMs, long maxAgeMs) {
        if (faulted) return State.ERROR;
        if (!connected) return State.DISCONNECTED;
        boolean hasMeasurement = false;
        for (Reading reading : readings.values()) {
            if (!reading.hasMeasurement()) continue;
            hasMeasurement = true;
            if (reading.fresh(nowMs, maxAgeMs)) return State.LIVE;
        }
        return hasMeasurement ? State.STALE : State.CONNECTED_NO_DATA;
    }
}
