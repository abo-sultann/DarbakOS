package com.abosultan.darbakos.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Compact persistent local-media manifest. Restoring it never starts playback. */
public final class LocalMediaIndex {
    private static final String PREFS = "darbak_local_media";
    private static final String KEY_MANIFEST = "track_manifest";
    private final Context context;
    private final SharedPreferences prefs;

    public LocalMediaIndex(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(List<LocalMediaTrack> tracks) {
        JSONArray array = new JSONArray();
        if (tracks != null) for (LocalMediaTrack track : tracks) {
            if (track == null) continue;
            try {
                JSONObject item = new JSONObject();
                item.put("i", track.identity());
                item.put("c", track.contentBacked());
                item.put("t", track.title);
                item.put("a", track.artist);
                item.put("s", track.size);
                item.put("m", track.modified);
                array.put(item);
            } catch (Exception ignored) {}
        }
        prefs.edit().putString(KEY_MANIFEST, array.toString()).apply();
    }

    public List<LocalMediaTrack> restoreValid() {
        ArrayList<LocalMediaTrack> tracks = new ArrayList<>();
        String raw = prefs.getString(KEY_MANIFEST, "");
        if (raw == null || raw.isEmpty()) return tracks;
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                String identity = item.optString("i", item.optString("p", ""));
                if (identity.isEmpty()) continue;
                long size = item.optLong("s", -1L);
                long modified = item.optLong("m", -1L);
                String title = item.optString("t", "");
                String artist = item.optString("a", "");
                boolean content = item.optBoolean("c", identity.startsWith("content://"));
                if (content) {
                    Uri uri = Uri.parse(identity);
                    if (!contentReadable(uri)) continue;
                    tracks.add(new LocalMediaTrack(uri, title, title, artist, size, modified));
                } else {
                    File file = new File(identity);
                    if (!file.isFile() || file.length() != size || file.lastModified() != modified) {
                        continue;
                    }
                    tracks.add(new LocalMediaTrack(file, title, artist, size, modified));
                }
            }
        } catch (Exception ignored) {}
        return tracks;
    }

    private boolean contentReadable(Uri uri) {
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            return descriptor != null;
        } catch (IOException | SecurityException | RuntimeException ignored) {
            return false;
        } finally {
            if (descriptor != null) {
                try { descriptor.close(); } catch (IOException ignored) { }
            }
        }
    }

    public void invalidate() { prefs.edit().remove(KEY_MANIFEST).apply(); }
}
