package com.ed.geospatial.core.versioning;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AtmVersioningServiceInMemoryImpl implements AtmVersioningService {

    private static final long INITIAL_STATE_VERSION = -1;

    private static final Map<String, State> storage = new ConcurrentHashMap<>();

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public AtmState get(String id) {
        final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            return toAtmState(storage.get(id));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AtmState save(String id, long currentVersion) {
        return null;
    }

    @Override
    public AtmState initUpdateIfMatchVersion(String id, long updateDocumentVersion, Callable<Boolean> updateFunc) {
        final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            final State state = storage.getOrDefault(id, new State(id));
            if (state.version.get() != updateDocumentVersion) {
                throw new IllegalArgumentException("Can not init update. Version mismatch. Update state before modify.");
            }
            if (updateFunc.call()) {
                state.inSyncVersion.incrementAndGet();
            }
            return toAtmState(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public AtmState commitUpdate(String id, long ver) {
        final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            final State state = storage.get(id);
            if (state == null) {
                throw new IllegalArgumentException("Can not commit update. Initiate update first");
            }
            state.version.set(ver);
            state.inSyncVersion.decrementAndGet();
            return toAtmState(state);
        } finally {
            writeLock.unlock();
        }
    }

    private AtmState toAtmState(State state) {
        if (state == null) {
            return null;
        }
        AtmState res = new AtmState();
        res.setId(state.id);
        res.setLatestCommittedVersion(state.version.get());
        res.setInSyncVersion(state.inSyncVersion.get());
        return res;
    }

    private static class State {
        private final String id;
        private final AtomicLong version = new AtomicLong(INITIAL_STATE_VERSION);
        private final AtomicLong inSyncVersion = new AtomicLong(-INITIAL_STATE_VERSION);

        public State(String id) {
            this.id = id;
        }
    }
}
