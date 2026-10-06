package com.abosultan.darbakos.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.util.List;

/** Owns the single bounded background worker used for local-media discovery. */
public final class LocalMediaLibrary {
    public interface Callback { void onScanned(List<LocalMediaTrack> tracks); }
    private final HandlerThread worker = new HandlerThread("DarbakLocalMedia");
    private Handler handler;
    private int generation;
    private boolean closed;

    public void start() {
        if (closed || handler != null) return;
        worker.start();
        handler = new Handler(worker.getLooper());
    }

    /** Legacy file-root scan retained for API25-focused regression tests. */
    public void scan(List<File> roots, Callback callback) {
        start();
        if (handler == null || callback == null) return;
        final int request = ++generation;
        handler.post(() -> {
            List<LocalMediaTrack> tracks = new LocalMediaScanner().scan(roots);
            if (!closed && request == generation) callback.onScanned(tracks);
        });
    }

    /** Primary P10 path: query user/shared/removable audio through MediaStore off the UI thread. */
    public void scanSharedAudio(Context context, Callback callback) {
        start();
        if (handler == null || context == null || callback == null) return;
        Context appContext = context.getApplicationContext();
        final int request = ++generation;
        handler.post(() -> {
            List<LocalMediaTrack> tracks = new MediaStoreAudioScanner().scan(appContext);
            if (!closed && request == generation) callback.onScanned(tracks);
        });
    }

    public void close() {
        closed = true;
        generation++;
        if (handler == null) return;
        handler.removeCallbacksAndMessages(null);
        worker.quitSafely();
        handler = null;
    }
}
