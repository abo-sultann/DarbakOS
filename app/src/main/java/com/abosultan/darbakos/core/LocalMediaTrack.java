package com.abosultan.darbakos.core;

import android.net.Uri;

import java.io.File;

/** Immutable local audio entry supporting both legacy files and modern MediaStore content URIs. */
public final class LocalMediaTrack {
    public final File file;
    public final Uri uri;
    public final String title;
    public final String artist;
    public final long size;
    public final long modified;

    public LocalMediaTrack(File file, String title, String artist, long size, long modified) {
        if (file == null) throw new IllegalArgumentException("file");
        this.file = file;
        this.uri = null;
        this.title = cleanTitle(title, file.getName());
        this.artist = cleanArtist(artist);
        this.size = size;
        this.modified = modified;
    }

    public LocalMediaTrack(Uri uri, String displayName, String title, String artist,
                           long size, long modified) {
        if (uri == null) throw new IllegalArgumentException("uri");
        this.file = null;
        this.uri = uri;
        this.title = cleanTitle(title, displayName == null ? uri.toString() : displayName);
        this.artist = cleanArtist(artist);
        this.size = size;
        this.modified = modified;
    }

    public boolean contentBacked() { return uri != null; }

    public String identity() {
        return uri != null ? uri.toString() : file.getAbsolutePath();
    }

    private static String cleanTitle(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        if (!clean.isEmpty()) return clean;
        String safeFallback = fallback == null ? "" : fallback.trim();
        return safeFallback.isEmpty() ? "Audio" : safeFallback;
    }

    private static String cleanArtist(String value) {
        return value == null ? "" : value.trim();
    }
}
