package com.abosultan.darbakos.core;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/** Chooses app-owned trip storage, preferring writable removable/external storage. */
public final class TripStorageLocator {
    private TripStorageLocator() { }

    public static File locate(Context context) {
        if (context == null) throw new IllegalArgumentException("context");
        File[] external = context.getExternalFilesDirs("trips");
        boolean[] removable = new boolean[external == null ? 0 : external.length];
        if (external != null) {
            for (int i = 0; i < external.length; i++) {
                File candidate = external[i];
                if (candidate == null) continue;
                try {
                    removable[i] = Environment.isExternalStorageRemovable(candidate);
                } catch (RuntimeException ignored) {
                    removable[i] = false;
                }
            }
        }
        return select(external, removable, internal(context));
    }

    public static File internal(Context context) {
        if (context == null) throw new IllegalArgumentException("context");
        return new File(context.getFilesDir(), "trips");
    }

    public static boolean isInternal(Context context, File directory) {
        return same(directory, internal(context));
    }

    /** Pure ordering rule: writable removable first, then writable external, then fallback. */
    public static File select(File[] external, boolean[] removable, File fallback) {
        if (external != null) {
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < external.length; i++) {
                    File candidate = external[i];
                    boolean isRemovable = removable != null && i < removable.length && removable[i];
                    if ((pass == 0) != isRemovable) continue;
                    if (prepare(candidate)) return candidate;
                }
            }
        }
        if (prepare(fallback)) return fallback;
        return null;
    }

    public static boolean prepare(File directory) {
        if (directory == null) return false;
        try {
            if (!directory.exists() && !directory.mkdirs()) return false;
            return directory.isDirectory() && directory.canWrite();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean same(File first, File second) {
        if (first == null || second == null) return false;
        try {
            return first.getCanonicalFile().equals(second.getCanonicalFile());
        } catch (IOException ignored) {
            return first.getAbsolutePath().equals(second.getAbsolutePath());
        }
    }
}
