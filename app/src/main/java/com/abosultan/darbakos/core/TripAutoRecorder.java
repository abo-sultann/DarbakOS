package com.abosultan.darbakos.core;

import java.io.File;
import java.io.IOException;

/**
 * Automatic trip session state machine. Call from one serialized worker.
 * It starts after two credible moving fixes and closes after sustained stationary time.
 */
public final class TripAutoRecorder {
    public enum State { IDLE, RECORDING }

    public static final float DEFAULT_START_SPEED_MPS = 2.0f;
    public static final float DEFAULT_STOP_SPEED_MPS = 1.0f;
    public static final float DEFAULT_MAX_ACCURACY_METERS = 50f;
    public static final long DEFAULT_STATIONARY_TIMEOUT_MS = 5 * 60 * 1000L;
    public static final int DEFAULT_CHUNK_POINTS = 60;
    /** Worst-case normally uncommitted window at the nominal 1 Hz GPS cadence. */
    public static final int MAX_UNCOMMITTED_POINTS = DEFAULT_CHUNK_POINTS;

    private TripRecorderStore store;
    private File directory;
    private final int chunkPoints;
    private final float startSpeed;
    private final float stopSpeed;
    private final float maxAccuracy;
    private final long stationaryTimeoutMs;

    private TripRecorderBuffer buffer;
    private State state = State.IDLE;
    private PositionFix pendingStart;
    private String sessionId;
    private long chunkIndex;
    private long stationarySince = -1L;
    private long lastMonotonicMs = -1L;

    public TripAutoRecorder(File directory) {
        this(directory, DEFAULT_CHUNK_POINTS, DEFAULT_START_SPEED_MPS,
                DEFAULT_STOP_SPEED_MPS, DEFAULT_MAX_ACCURACY_METERS,
                DEFAULT_STATIONARY_TIMEOUT_MS);
    }

    TripAutoRecorder(File directory, int chunkPoints, float startSpeed, float stopSpeed,
                     float maxAccuracy, long stationaryTimeoutMs) {
        if (directory == null || chunkPoints <= 1 || startSpeed <= stopSpeed
                || stopSpeed < 0f || maxAccuracy <= 0f || stationaryTimeoutMs <= 0L) {
            throw new IllegalArgumentException("Trip recorder configuration");
        }
        this.directory = directory;
        this.store = new TripRecorderStore(new TripChunkWriter(directory));
        this.chunkPoints = chunkPoints;
        this.startSpeed = startSpeed;
        this.stopSpeed = stopSpeed;
        this.maxAccuracy = maxAccuracy;
        this.stationaryTimeoutMs = stationaryTimeoutMs;
    }

    public State state() { return state; }
    public String sessionId() { return sessionId; }
    public long nextChunkIndex() { return chunkIndex; }
    public File directory() { return directory; }
    public int uncommittedPointCount() { return buffer == null ? 0 : buffer.size(); }

    /** Switches future chunk writes while retaining the current session and in-memory points. */
    public void switchStorage(File nextDirectory) {
        if (nextDirectory == null) throw new IllegalArgumentException("nextDirectory");
        directory = nextDirectory;
        store = new TripRecorderStore(new TripChunkWriter(nextDirectory));
    }

    public void accept(PositionFix fix) throws IOException {
        if (!credible(fix)) return;
        if (fix.monotonicMs <= lastMonotonicMs) return;
        if (lastMonotonicMs >= 0L
                && fix.monotonicMs - lastMonotonicMs > PositionQualityPolicy.MAX_FIX_AGE_MS) {
            // A new logical session is a durable gap boundary in the existing chunk format.
            if (state == State.RECORDING) finishSession();
            else pendingStart = null;
        }
        if (state == State.IDLE) {
            lastMonotonicMs = fix.monotonicMs;
            considerStart(fix);
            return;
        }

        // A failed full chunk stays in memory. Retry it before accepting another point.
        if (buffer.isFull()) flush();
        if (!buffer.append(fix)) return;
        lastMonotonicMs = fix.monotonicMs;
        if (buffer.isFull()) flush();

        if (fix.speedMetersPerSecond <= stopSpeed) {
            if (stationarySince < 0L) stationarySince = fix.monotonicMs;
            else if (fix.monotonicMs - stationarySince >= stationaryTimeoutMs) finishSession();
        } else {
            stationarySince = -1L;
        }
    }

    public void flush() throws IOException {
        if (state != State.RECORDING || buffer == null || buffer.size() == 0) return;
        File committed = store.flush(buffer, sessionId, chunkIndex);
        if (committed != null) chunkIndex++;
    }

    /** Best-effort clean shutdown: persists current points and ends the logical session. */
    public void close() throws IOException {
        if (state == State.RECORDING) flush();
        resetSession();
    }

    private void considerStart(PositionFix fix) throws IOException {
        if (fix.speedMetersPerSecond < startSpeed) {
            pendingStart = null;
            return;
        }
        if (pendingStart == null) {
            pendingStart = fix;
            return;
        }
        if (fix.monotonicMs <= pendingStart.monotonicMs) return;

        state = State.RECORDING;
        buffer = new TripRecorderBuffer(chunkPoints);
        sessionId = makeSessionId(pendingStart);
        chunkIndex = 0L;
        stationarySince = -1L;
        buffer.append(pendingStart);
        buffer.append(fix);
        pendingStart = null;
        if (buffer.isFull()) flush();
    }

    private void finishSession() throws IOException {
        flush();
        resetSession();
    }

    private void resetSession() {
        state = State.IDLE;
        buffer = null;
        pendingStart = null;
        sessionId = null;
        chunkIndex = 0L;
        stationarySince = -1L;
    }

    private boolean credible(PositionFix fix) {
        return fix != null && PositionQualityPolicy.isUsable(fix, fix.monotonicMs)
                && fix.accuracyMeters <= maxAccuracy;
    }

    private static String makeSessionId(PositionFix fix) {
        if (fix.wallTimeMs > 0L) return "t" + fix.wallTimeMs;
        return "m" + fix.monotonicMs;
    }
}
