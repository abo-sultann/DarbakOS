package com.abosultan.darbakos.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/** Single-process local player. Playback is only started by an explicit user action. */
public final class LocalMediaPlayer {
    public interface Listener { void onState(LocalMediaTrack track, boolean playing, boolean error); }

    private final Context context;
    private MediaPlayer player;
    private LocalMediaTrack current;
    private final Listener listener;
    private final AudioManager audioManager;
    private boolean preparing;
    private boolean resumeAfterTransientLoss;
    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (change == AudioManager.AUDIOFOCUS_LOSS) {
            resumeAfterTransientLoss = false;
            pauseForFocusLoss();
            abandonFocus();
        } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            resumeAfterTransientLoss = isPlaying();
            pauseForFocusLoss();
        } else if (change == AudioManager.AUDIOFOCUS_GAIN && resumeAfterTransientLoss) {
            resumeAfterTransientLoss = false;
            resumeAfterFocusGain();
        }
    };

    public LocalMediaPlayer(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void play(LocalMediaTrack track) {
        if (track == null || (track.uri == null && (track.file == null || !track.file.isFile()))) {
            publish(track, false, true);
            return;
        }
        releasePlayer();
        if (!requestFocus()) { publish(track, false, true); return; }
        MediaPlayer next = new MediaPlayer();
        player = next;
        current = track;
        preparing = true;
        try {
            next.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (track.uri != null) next.setDataSource(context, track.uri);
            else next.setDataSource(track.file.getAbsolutePath());
            next.setOnPreparedListener(mp -> {
                if (player != mp) return;
                preparing = false;
                try {
                    mp.start();
                    publish(current, true, false);
                } catch (IllegalStateException e) {
                    releasePlayer();
                    publish(track, false, true);
                }
            });
            next.setOnCompletionListener(mp -> {
                preparing = false;
                publish(current, false, false);
            });
            next.setOnErrorListener((mp, what, extra) -> {
                preparing = false;
                publish(current, false, true);
                return true;
            });
            next.prepareAsync();
            publish(current, false, false);
        } catch (IOException | RuntimeException e) {
            releasePlayer();
            publish(track, false, true);
        }
    }

    /** Toggles the existing MediaPlayer instance, preserving its current playback position. */
    public void playPause() {
        if (player == null || preparing) return;
        try {
            if (player.isPlaying()) {
                player.pause();
                abandonFocus();
            } else {
                if (!requestFocus()) return;
                player.start();
            }
            publish(current, player.isPlaying(), false);
        } catch (IllegalStateException e) { publish(current, false, true); }
    }

    public void stop() {
        abandonFocus();
        releasePlayer();
        current = null;
        publish(null, false, false);
    }

    public void release() { abandonFocus(); releasePlayer(); current = null; }

    public LocalMediaTrack currentTrack() { return current; }

    public boolean isPlaying() {
        try { return player != null && !preparing && player.isPlaying(); }
        catch (IllegalStateException e) { return false; }
    }

    public void pauseForExternalPlayback() {
        resumeAfterTransientLoss = false;
        pauseForFocusLoss();
        abandonFocus();
    }

    private boolean requestFocus() {
        return audioManager != null && audioManager.requestAudioFocus(focusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonFocus() {
        if (audioManager != null) audioManager.abandonAudioFocus(focusListener);
    }

    private void pauseForFocusLoss() {
        try {
            if (player != null && !preparing && player.isPlaying()) {
                player.pause();
                publish(current, false, false);
            }
        } catch (IllegalStateException ignored) {}
    }

    private void resumeAfterFocusGain() {
        try {
            if (player != null && !preparing) {
                player.start();
                publish(current, true, false);
            }
        } catch (IllegalStateException e) { publish(current, false, true); }
    }

    private void releasePlayer() {
        MediaPlayer old = player;
        player = null;
        preparing = false;
        if (old != null) try { old.release(); } catch (RuntimeException ignored) {}
    }

    private void publish(LocalMediaTrack track, boolean playing, boolean error) {
        if (listener != null) listener.onState(track, playing, error);
    }
}
