package com.abosultan.darbakos.core;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists selection only. Restoring state never starts playback. */
public final class LocalMediaState {
    private static final String PREFS = "darbak_local_media";
    private static final String KEY_IDENTITY = "selected_identity";
    private static final String LEGACY_KEY_PATH = "selected_path";
    private final SharedPreferences prefs;

    public LocalMediaState(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void remember(LocalMediaTrack track) {
        SharedPreferences.Editor edit = prefs.edit();
        if (track == null) edit.remove(KEY_IDENTITY);
        else edit.putString(KEY_IDENTITY, track.identity());
        edit.remove(LEGACY_KEY_PATH).apply();
    }

    public String selectedIdentity() {
        String identity = prefs.getString(KEY_IDENTITY, "");
        if (identity == null || identity.isEmpty()) {
            identity = prefs.getString(LEGACY_KEY_PATH, "");
        }
        return identity == null ? "" : identity;
    }

    /** Legacy accessor retained for focused tests and migration compatibility. */
    public String selectedPath() { return selectedIdentity(); }

    public int restoreSelection(LocalMediaQueue queue) {
        String wanted = selectedIdentity();
        if (wanted.isEmpty() || queue == null) return -1;
        for (int i = 0; i < queue.tracks().size(); i++) {
            LocalMediaTrack track = queue.tracks().get(i);
            if (track != null && wanted.equals(track.identity())) {
                queue.select(i);
                return i;
            }
        }
        return -1;
    }
}
