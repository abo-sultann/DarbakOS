package com.abosultan.darbakos.core;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;

/**
 * P4 continuous position/trip runtime with an API25 legacy floor and modern foreground mode.
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

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enterForegroundApi26();
        workerThread = new HandlerThread("DarbakTripRuntime");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        synchronized (TripRuntimeService.class) { currentRuntime = this; }
        worker.post(() -> {
            File directory = TripStorageLocator.locate(this);
            if (directory != null) recorder = new TripAutoRecorder(directory);
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
                if (currentRuntime == this) PositionStore.get().publish(fix);
            }
            record(fix);
        });
    }

    @Override public void onUnavailable() {
        enqueue(() -> {
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
                    try {
                        if (gps != null) gps.stop();
                        if (recorder != null) recorder.close();
                    } catch (IOException ignored) {
                        // Already committed chunks remain readable after a failed final save.
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

    private void record(PositionFix fix) {
        if (recorder == null) return;
        try {
            recorder.accept(fix);
        } catch (IOException ignored) {
            // Position remains live even if trip storage temporarily fails.
        }
    }
}
