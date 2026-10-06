package com.abosultan.darbakos.core;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;

/**
 * Continuous position/trip runtime with an API25 regression floor and modern foreground mode.
 * One worker owns GPS callbacks and trip persistence so disk I/O never blocks Darbak UI.
 */
public final class TripRuntimeService extends Service implements AndroidGpsSource.Callback {
    private static final String CHANNEL_ID = "darbak_trip_runtime";
    private static final int NOTIFICATION_ID = 2026;
    // A retiring worker must not invalidate a replacement runtime's current position.
    private static TripRuntimeService currentRuntime;
    private HandlerThread workerThread;
    private Handler worker;
    private AndroidGpsSource gps;
    private TripAutoRecorder recorder;
    private boolean closing;

    private final Runnable staleFixExpiry = () -> {
        synchronized (TripRuntimeService.class) {
            if (currentRuntime == this) {
                PositionStore.get().expireIfStale(SystemClock.elapsedRealtime());
            }
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enterForegroundApi26();
        workerThread = new HandlerThread("DarbakTripRuntime");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        synchronized (TripRuntimeService.class) { currentRuntime = this; }
        TripStorageState.get().reset();
        worker.post(() -> {
            File directory = TripStorageLocator.locate(this);
            if (directory != null) {
                recorder = new TripAutoRecorder(directory);
                TripStorageState.get().publish(TripStorageLocator.isInternal(this, directory)
                        ? TripStorageState.Status.INTERNAL_READY
                        : TripStorageState.Status.EXTERNAL_READY);
            } else {
                TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
            }
            gps = new AndroidGpsSource(this, this);
            gps.start(workerThread.getLooper());
        });
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        enqueue(() -> {
            if (gps != null && !gps.started()) gps.start(workerThread.getLooper());
        });
        return START_STICKY;
    }

    @Override public void onFix(final PositionFix fix) {
        enqueue(() -> {
            synchronized (TripRuntimeService.class) {
                if (currentRuntime == this) {
                    PositionStore.get().publish(fix);
                    scheduleExpiry(fix);
                }
            }
            record(fix);
        });
    }

    @Override public void onUnavailable() {
        enqueue(() -> {
            if (worker != null) worker.removeCallbacks(staleFixExpiry);
            synchronized (TripRuntimeService.class) {
                if (currentRuntime == this) PositionStore.get().publishUnavailable();
            }
        });
    }

    @Override public void onDestroy() {
        synchronized (this) {
            if (!closing && worker != null) {
                closing = true;
                // Enqueue atomically after all accepted fixes; never flush or join on the UI.
                worker.post(() -> {
                    worker.removeCallbacks(staleFixExpiry);
                    try {
                        if (gps != null) gps.stop();
                        closeRecorderWithFallback();
                    } finally {
                        synchronized (TripRuntimeService.class) {
                            if (currentRuntime == this) {
                                PositionStore.get().publishUnavailable();
                                currentRuntime = null;
                            }
                        }
                        workerThread.quitSafely();
                    }
                });
            }
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @TargetApi(Build.VERSION_CODES.O)
    private void enterForegroundApi26() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Darbak Trip", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Continuous vehicle position and trip recording");
            manager.createNotificationChannel(channel);
        }
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(com.abosultan.darbakos.R.drawable.ic_darbak)
                .setContentTitle("Darbak")
                .setContentText("GPS and trip recording active")
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private synchronized void enqueue(Runnable action) {
        if (!closing && worker != null) worker.post(action);
    }

    private void scheduleExpiry(PositionFix fix) {
        if (worker == null || fix == null) return;
        worker.removeCallbacks(staleFixExpiry);
        long deadline = fix.monotonicMs + PositionQualityPolicy.MAX_FIX_AGE_MS + 1L;
        long delay = Math.max(1L, deadline - SystemClock.elapsedRealtime());
        worker.postDelayed(staleFixExpiry, delay);
    }

    private void record(PositionFix fix) {
        if (recorder == null) {
            TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
            return;
        }
        try {
            recorder.accept(fix);
            return;
        } catch (IOException firstFailure) {
            // The recorder retains its uncommitted buffer. Switch writer only, then retry safely.
        }

        if (!fallbackToInternal()) {
            TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
            return;
        }
        try {
            // Flush any retained full chunk first; accept() then either appends this fix or ignores
            // it if the failed write happened after the same fix was already staged.
            recorder.flush();
            recorder.accept(fix);
        } catch (IOException secondFailure) {
            TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
        }
    }

    private boolean fallbackToInternal() {
        if (recorder == null || TripStorageLocator.isInternal(this, recorder.directory())) {
            return false;
        }
        File internal = TripStorageLocator.internal(this);
        if (!TripStorageLocator.prepare(internal)) return false;
        recorder.switchStorage(internal);
        TripStorageState.get().publish(TripStorageState.Status.INTERNAL_FALLBACK);
        return true;
    }

    private void closeRecorderWithFallback() {
        if (recorder == null) return;
        try {
            recorder.close();
            return;
        } catch (IOException firstFailure) {
            // Retain buffered points and attempt the same explicit external -> internal fallback.
        }
        if (!fallbackToInternal()) {
            TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
            return;
        }
        try {
            recorder.close();
        } catch (IOException secondFailure) {
            TripStorageState.get().publish(TripStorageState.Status.WRITE_FAILED);
        }
    }
}
