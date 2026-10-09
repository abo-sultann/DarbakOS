package com.abosultan.darbakos.mediafixture;

import android.app.Activity;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/** Test-only external process that behaves like a small API25 media player. */
public final class FixtureActivity extends Activity {
    private static final long ACTIONS = PlaybackState.ACTION_PLAY
            | PlaybackState.ACTION_PAUSE
            | PlaybackState.ACTION_SKIP_TO_NEXT
            | PlaybackState.ACTION_SKIP_TO_PREVIOUS;

    private MediaSession session;
    private boolean playing;
    private String title = "GDN Fixture";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView label = new TextView(this);
        label.setText("GDN external media fixture");
        label.setTextSize(24f);
        label.setPadding(32, 32, 32, 32);
        setContentView(label);

        session = new MediaSession(this, "DarbakP5Fixture");
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() {
                playing = true;
                publish();
            }

            @Override public void onPause() {
                playing = false;
                publish();
            }

            @Override public void onSkipToNext() {
                title = "GDN Fixture Next";
                publish();
            }

            @Override public void onSkipToPrevious() {
                title = "GDN Fixture Previous";
                publish();
            }
        }, new Handler(Looper.getMainLooper()));
        session.setActive(true);
        publish();
    }

    private void publish() {
        if (session == null) return;
        session.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "GDN CI Fixture")
                .build());
        session.setPlaybackState(new PlaybackState.Builder()
                .setActions(ACTIONS)
                .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                        0L, 1f)
                .build());
    }

    @Override protected void onDestroy() {
        if (session != null) {
            session.release();
            session = null;
        }
        super.onDestroy();
    }
}
