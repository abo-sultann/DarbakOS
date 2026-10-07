package com.abosultan.darbakos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.LocalMediaTrack;
import com.abosultan.darbakos.core.MediaStoreAudioScanner;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.OutputStream;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class MediaStoreAudioScannerTest {
    @Test public void sharedAudioIsDiscoveredRemovedAndRediscovered() throws Exception {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        MediaStoreAudioScanner scanner = new MediaStoreAudioScanner();
        String title = "Darbak-CI-" + System.nanoTime();

        Uri first = insertAudio(resolver, title);
        try {
            assertTrue("MediaStore shared audio must be discoverable",
                    containsTitle(scanner.scan(context), title));
        } finally {
            resolver.delete(first, null, null);
        }

        assertFalse("Removed shared audio must not remain live in a fresh scan",
                containsTitle(scanner.scan(context), title));

        Uri second = insertAudio(resolver, title);
        try {
            assertTrue("Re-added shared audio must be rediscovered",
                    containsTitle(scanner.scan(context), title));
        } finally {
            resolver.delete(second, null, null);
        }
    }

    private static Uri insertAudio(ContentResolver resolver, String title) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, title + ".mp3");
        values.put(MediaStore.Audio.Media.TITLE, title);
        values.put(MediaStore.Audio.Media.ARTIST, "Darbak");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/DarbakCI");
        Uri primary = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri inserted = resolver.insert(primary, values);
        assertNotNull(inserted);
        OutputStream stream = resolver.openOutputStream(inserted, "w");
        assertNotNull(stream);
        stream.write(new byte[] {0x49, 0x44, 0x33});
        stream.close();
        return inserted;
    }

    private static boolean containsTitle(List<LocalMediaTrack> tracks, String title) {
        for (LocalMediaTrack track : tracks) {
            if (title.equals(track.title) && track.contentBacked()) return true;
        }
        return false;
    }
}
