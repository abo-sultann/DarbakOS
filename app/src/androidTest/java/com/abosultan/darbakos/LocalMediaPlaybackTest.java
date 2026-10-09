package com.abosultan.darbakos;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.abosultan.darbakos.core.LocalMediaPlayer;
import com.abosultan.darbakos.core.LocalMediaTrack;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class LocalMediaPlaybackTest {
    @Test public void explicitPlayStartsRealApi25MediaPlayerThenPauseResumes() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File wav = new File(context.getCacheDir(), "darbak-local-playback.wav");
        writeSilentWav(wav, 8000, 2);
        LocalMediaTrack track = new LocalMediaTrack(wav, "Local Playback", "GDN",
                wav.length(), wav.lastModified());
        AtomicBoolean playing = new AtomicBoolean(false);
        AtomicBoolean error = new AtomicBoolean(false);
        LocalMediaPlayer player = new LocalMediaPlayer(context, (t, p, e) -> {
            playing.set(p); if (e) error.set(true);
        });
        try {
            assertFalse(player.isPlaying());
            player.play(track);
            await(player, true);
            assertFalse(error.get());
            assertEquals(wav.getAbsolutePath(), player.currentTrack().file.getAbsolutePath());
            player.playPause();
            await(player, false);
            assertFalse(error.get());
            player.playPause();
            await(player, true);
            assertFalse(error.get());
        } finally {
            player.release();
            wav.delete();
        }
    }

    private static void await(LocalMediaPlayer player, boolean expected) throws Exception {
        for (int i = 0; i < 80; i++) {
            if (player.isPlaying() == expected) return;
            Thread.sleep(50L);
        }
        assertEquals(expected, player.isPlaying());
    }

    private static void writeSilentWav(File file, int rate, int seconds) throws Exception {
        int data = rate * seconds * 2;
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            byte[] h = new byte[44];
            put(h,0,"RIFF"); le32(h,4,36+data); put(h,8,"WAVE"); put(h,12,"fmt ");
            le32(h,16,16); le16(h,20,1); le16(h,22,1); le32(h,24,rate);
            le32(h,28,rate*2); le16(h,32,2); le16(h,34,16); put(h,36,"data"); le32(h,40,data);
            out.write(h);
            byte[] zero = new byte[1024];
            for (int left=data; left>0; left-=Math.min(left,zero.length))
                out.write(zero,0,Math.min(left,zero.length));
        }
    }
    private static void put(byte[] b,int o,String s){ for(int i=0;i<s.length();i++) b[o+i]=(byte)s.charAt(i); }
    private static void le16(byte[] b,int o,int v){ b[o]=(byte)v; b[o+1]=(byte)(v>>8); }
    private static void le32(byte[] b,int o,int v){ b[o]=(byte)v; b[o+1]=(byte)(v>>8); b[o+2]=(byte)(v>>16); b[o+3]=(byte)(v>>24); }
}
