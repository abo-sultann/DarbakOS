package com.abosultan.darbakos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.abosultan.darbakos.core.LocalMediaIndex;
import com.abosultan.darbakos.core.LocalMediaTrack;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class LocalMediaIndexTest {
    @Test public void deletedContentUriIsNotRestoredFromCache() throws Exception {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        LocalMediaIndex index = new LocalMediaIndex(context);
        index.invalidate();
        String title = "GDN-CI-cache-" + System.nanoTime();
        Uri uri = insertAudio(resolver, title);
        try {
            index.save(Collections.singletonList(
                    new LocalMediaTrack(uri, title + ".mp3", title, "GDN", 3L,
                            System.currentTimeMillis())));
            List<LocalMediaTrack> restored = index.restoreValid();
            assertEquals(1, restored.size());
            assertEquals(uri, restored.get(0).uri);
        } finally {
            resolver.delete(uri, null, null);
        }

        assertTrue("A removed USB/MediaStore URI must not survive cached restore",
                index.restoreValid().isEmpty());
        index.invalidate();
    }

    private static Uri insertAudio(ContentResolver resolver, String title) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, title + ".mp3");
        values.put(MediaStore.Audio.Media.TITLE, title);
        values.put(MediaStore.Audio.Media.ARTIST, "GDN");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/GDNCI");
        Uri primary = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri inserted = resolver.insert(primary, values);
        if (inserted == null) throw new AssertionError("MediaStore insert returned null");
        OutputStream stream = resolver.openOutputStream(inserted, "w");
        if (stream == null) throw new AssertionError("MediaStore output stream returned null");
        stream.write(new byte[] {0x49, 0x44, 0x33});
        stream.close();
        return inserted;
    }
}
