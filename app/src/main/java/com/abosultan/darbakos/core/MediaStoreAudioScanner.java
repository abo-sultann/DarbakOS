package com.abosultan.darbakos.core;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Modern shared-audio discovery. VOLUME_EXTERNAL is a merged read view of attached external
 * MediaStore volumes on Android 10+, including removable media that the system has indexed.
 */
public final class MediaStoreAudioScanner {
    private static final int MAX_TRACKS = 5000;

    public List<LocalMediaTrack> scan(Context context) {
        if (context == null) return Collections.emptyList();
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Uri collection = collectionUri();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED
        };
        ArrayList<LocalMediaTrack> tracks = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(collection, projection, null, null,
                    MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
            if (cursor == null) return Collections.emptyList();
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
            while (cursor.moveToNext() && tracks.size() < MAX_TRACKS) {
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(collection, id);
                String displayName = cursor.getString(nameColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                long size = Math.max(0L, cursor.getLong(sizeColumn));
                long modifiedSeconds = Math.max(0L, cursor.getLong(modifiedColumn));
                tracks.add(new LocalMediaTrack(uri, displayName, title, artist,
                        size, modifiedSeconds * 1000L));
            }
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        } finally {
            if (cursor != null) cursor.close();
        }
        Collections.sort(tracks, Comparator.comparing(
                track -> track.title.toLowerCase(Locale.ROOT)));
        return Collections.unmodifiableList(tracks);
    }

    private static Uri collectionUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return mergedExternalUriApi29();
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private static Uri mergedExternalUriApi29() {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
    }
}
