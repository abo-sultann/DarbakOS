package com.abosultan.darbakos.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * UI-ready TPMS projection for all five Toyota wheel positions.
 *
 * This class never upgrades stale, disconnected, or faulted evidence into a live value. It keeps
 * stale/last-known values available for an explicitly labelled UI while exposing a separate state
 * for each wheel. Alert policy intentionally stays outside this model, especially for the spare.
 */
public final class TpmsDisplayModel {
    public enum WheelState { NO_DATA, LIVE, LOW, STALE, SOURCE_UNAVAILABLE }

    public static final class WheelView {
        public final TpmsSourceSnapshot.Wheel wheel;
        public final WheelState state;
        public final String sensorId;
        public final Double pressurePsi;
        public final Double temperatureC;
        public final long observedAtMs;

        private WheelView(TpmsSourceSnapshot.Wheel wheel,
                          WheelState state,
                          TpmsSourceSnapshot.Reading reading) {
            this.wheel = wheel;
            this.state = state;
            this.sensorId = reading == null ? null : reading.sensorId;
            this.pressurePsi = reading == null ? null : reading.pressurePsi;
            this.temperatureC = reading == null ? null : reading.temperatureC;
            this.observedAtMs = reading == null ? 0L : reading.observedAtMs;
        }

        public boolean spare() { return wheel == TpmsSourceSnapshot.Wheel.SPARE; }
        public boolean live() { return state == WheelState.LIVE || state == WheelState.LOW; }
    }

    public final TpmsSourceSnapshot.State sourceState;
    private final Map<TpmsSourceSnapshot.Wheel, WheelView> wheels;

    private TpmsDisplayModel(TpmsSourceSnapshot.State sourceState,
                             Map<TpmsSourceSnapshot.Wheel, WheelView> wheels) {
        this.sourceState = sourceState;
        this.wheels = Collections.unmodifiableMap(wheels);
    }

    public static TpmsDisplayModel from(TpmsSourceSnapshot snapshot,
                                        long nowMs,
                                        long maxAgeMs,
                                        double lowPressurePsi) {
        TpmsSourceSnapshot resolved = snapshot == null
                ? TpmsSourceSnapshot.disconnected() : snapshot;
        TpmsSourceSnapshot.State sourceState = resolved.state(nowMs, maxAgeMs);
        EnumMap<TpmsSourceSnapshot.Wheel, WheelView> views =
                new EnumMap<>(TpmsSourceSnapshot.Wheel.class);

        for (TpmsSourceSnapshot.Wheel wheel : TpmsSourceSnapshot.Wheel.values()) {
            TpmsSourceSnapshot.Reading reading = resolved.reading(wheel);
            WheelState state;
            if (sourceState == TpmsSourceSnapshot.State.DISCONNECTED
                    || sourceState == TpmsSourceSnapshot.State.ERROR) {
                state = WheelState.SOURCE_UNAVAILABLE;
            } else if (reading == null || !reading.hasMeasurement()) {
                state = WheelState.NO_DATA;
            } else if (!reading.fresh(nowMs, maxAgeMs)) {
                state = WheelState.STALE;
            } else if (reading.pressurePsi != null && reading.pressurePsi < lowPressurePsi) {
                state = WheelState.LOW;
            } else {
                state = WheelState.LIVE;
            }
            views.put(wheel, new WheelView(wheel, state, reading));
        }
        return new TpmsDisplayModel(sourceState, views);
    }

    public WheelView wheel(TpmsSourceSnapshot.Wheel wheel) {
        return wheel == null ? null : wheels.get(wheel);
    }

    public Map<TpmsSourceSnapshot.Wheel, WheelView> wheels() { return wheels; }
}
