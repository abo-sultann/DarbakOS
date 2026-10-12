package com.abosultan.darbakos.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Alert policy for TPMS display state.
 *
 * Road-wheel low pressure may raise a warning. Spare-wheel low pressure is visual-only by policy.
 * Missing, stale, or unavailable data is informational so transport/data faults never masquerade as
 * a pressure emergency. Fast-leak escalation is intentionally reserved for a later history-aware layer.
 */
public final class TpmsAlertPolicy {
    public enum Severity { NONE, INFO, WARNING }

    public static final class WheelAlert {
        public final TpmsSourceSnapshot.Wheel wheel;
        public final Severity severity;
        public final boolean visualOnly;
        public final TpmsDisplayModel.WheelState state;

        private WheelAlert(TpmsSourceSnapshot.Wheel wheel,
                           Severity severity,
                           boolean visualOnly,
                           TpmsDisplayModel.WheelState state) {
            this.wheel = wheel;
            this.severity = severity;
            this.visualOnly = visualOnly;
            this.state = state;
        }

        public boolean active() { return severity != Severity.NONE; }
    }

    public static final class Result {
        public final Severity severity;
        private final Map<TpmsSourceSnapshot.Wheel, WheelAlert> alerts;

        private Result(Severity severity,
                       Map<TpmsSourceSnapshot.Wheel, WheelAlert> alerts) {
            this.severity = severity;
            this.alerts = Collections.unmodifiableMap(alerts);
        }

        public WheelAlert wheel(TpmsSourceSnapshot.Wheel wheel) {
            return wheel == null ? null : alerts.get(wheel);
        }

        public Map<TpmsSourceSnapshot.Wheel, WheelAlert> alerts() { return alerts; }

        public boolean roadWarning() {
            for (WheelAlert alert : alerts.values()) {
                if (alert.wheel != TpmsSourceSnapshot.Wheel.SPARE
                        && alert.severity == Severity.WARNING) return true;
            }
            return false;
        }

        public boolean spareVisualNotice() {
            WheelAlert spare = alerts.get(TpmsSourceSnapshot.Wheel.SPARE);
            return spare != null && spare.active() && spare.visualOnly;
        }
    }

    private TpmsAlertPolicy() { }

    public static Result evaluate(TpmsDisplayModel model) {
        EnumMap<TpmsSourceSnapshot.Wheel, WheelAlert> alerts =
                new EnumMap<>(TpmsSourceSnapshot.Wheel.class);
        Severity overall = Severity.NONE;

        for (TpmsSourceSnapshot.Wheel wheel : TpmsSourceSnapshot.Wheel.values()) {
            TpmsDisplayModel.WheelView view = model == null ? null : model.wheel(wheel);
            TpmsDisplayModel.WheelState state = view == null
                    ? TpmsDisplayModel.WheelState.SOURCE_UNAVAILABLE : view.state;
            boolean spare = wheel == TpmsSourceSnapshot.Wheel.SPARE;
            Severity severity = severityFor(state, spare);
            boolean visualOnly = spare && severity != Severity.NONE;
            alerts.put(wheel, new WheelAlert(wheel, severity, visualOnly, state));
            if (severity.ordinal() > overall.ordinal()) overall = severity;
        }

        return new Result(overall, alerts);
    }

    private static Severity severityFor(TpmsDisplayModel.WheelState state, boolean spare) {
        if (state == null) return Severity.INFO;
        switch (state) {
            case LOW:
                return spare ? Severity.INFO : Severity.WARNING;
            case STALE:
            case SOURCE_UNAVAILABLE:
            case NO_DATA:
                return Severity.INFO;
            default:
                return Severity.NONE;
        }
    }
}
