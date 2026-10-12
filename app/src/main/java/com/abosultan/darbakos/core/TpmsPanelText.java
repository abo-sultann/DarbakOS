package com.abosultan.darbakos.core;

import java.util.Locale;

/** Formats the five-wheel TPMS projection for the existing vehicle panel. */
public final class TpmsPanelText {
    private TpmsPanelText() { }

    public static String pressure(TpmsDisplayModel model) {
        if (model == null) model = TpmsDisplayModel.from(null, 0L, 0L, 25.0);
        return "ضغط الإطارات\n"
                + row(model, TpmsSourceSnapshot.Wheel.FRONT_LEFT, "أمامي يسار", true)
                + "  |  "
                + row(model, TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "أمامي يمين", true)
                + "\n"
                + row(model, TpmsSourceSnapshot.Wheel.REAR_LEFT, "خلفي يسار", true)
                + "  |  "
                + row(model, TpmsSourceSnapshot.Wheel.REAR_RIGHT, "خلفي يمين", true)
                + "\n"
                + row(model, TpmsSourceSnapshot.Wheel.SPARE, "الاسبير", true);
    }

    public static String temperature(TpmsDisplayModel model) {
        if (model == null) model = TpmsDisplayModel.from(null, 0L, 0L, 25.0);
        return "حرارة الإطارات\n"
                + row(model, TpmsSourceSnapshot.Wheel.FRONT_LEFT, "أمامي يسار", false)
                + "  |  "
                + row(model, TpmsSourceSnapshot.Wheel.FRONT_RIGHT, "أمامي يمين", false)
                + "\n"
                + row(model, TpmsSourceSnapshot.Wheel.REAR_LEFT, "خلفي يسار", false)
                + "  |  "
                + row(model, TpmsSourceSnapshot.Wheel.REAR_RIGHT, "خلفي يمين", false)
                + "\n"
                + row(model, TpmsSourceSnapshot.Wheel.SPARE, "الاسبير", false);
    }

    public static String source(TpmsDisplayModel model) {
        TpmsSourceSnapshot.State state = model == null
                ? TpmsSourceSnapshot.State.DISCONNECTED : model.sourceState;
        return "TPMS • " + sourceState(state);
    }

    private static String row(TpmsDisplayModel model,
                              TpmsSourceSnapshot.Wheel wheel,
                              String label,
                              boolean pressure) {
        TpmsDisplayModel.WheelView view = model.wheel(wheel);
        if (view == null) return label + ": --";
        Double value = pressure ? view.pressurePsi : view.temperatureC;
        String unit = pressure ? " PSI" : " °C";
        String reading = value == null ? "--" : String.format(Locale.US, "%.1f%s", value, unit);
        return label + ": " + reading + stateSuffix(view.state);
    }

    private static String stateSuffix(TpmsDisplayModel.WheelState state) {
        if (state == null) return "";
        switch (state) {
            case LOW: return " • منخفض";
            case STALE: return " • قراءة قديمة";
            case SOURCE_UNAVAILABLE: return " • المصدر غير متاح";
            case NO_DATA: return " • لا توجد قراءة";
            default: return "";
        }
    }

    private static String sourceState(TpmsSourceSnapshot.State state) {
        if (state == null) return "غير متاح";
        switch (state) {
            case LIVE: return "متصل";
            case STALE: return "القراءات قديمة";
            case CONNECTED_NO_DATA: return "متصل بدون بيانات";
            case ERROR: return "خطأ في المصدر";
            default: return "غير متصل";
        }
    }
}
