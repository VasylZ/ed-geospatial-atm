package com.ed.geospatial.core.versioning;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.policy.CommitLevel;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.ed.geospatial.core.versioning.aerospike.AbstractAerospikeClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AtmVersioningServiceAerospikeImpl extends AbstractAerospikeClient implements AtmVersioningService {

    private static final String ID = "gId";
    private static final String LATEST_COMMITTED_VERSION = "gV";
    private static final String IN_SYNC_VERSION = "gInSyncV";

    private final QueryPolicy queryPolicy;
    private final WritePolicy writePolicy;

    public AtmVersioningServiceAerospikeImpl() {
        this.queryPolicy = new QueryPolicy();
        this.queryPolicy.totalTimeout = 1_000;
        this.queryPolicy.maxRetries = 2;
//        this.queryPolicy.readModeSC = ReadModeSC.LINEARIZE; // ensure strong consistency // Enterprise only

        this.writePolicy = new WritePolicy();
        this.writePolicy.totalTimeout = 1_000;
        this.writePolicy.maxRetries = 2;
        this.writePolicy.expiration = -1; // newer expire
        this.writePolicy.commitLevel = CommitLevel.COMMIT_ALL;  // ensure strong consistency
//        this.writePolicy.generationPolicy = GenerationPolicy.EXPECT_GEN_EQUAL;  // ensure strong consistency // Enterprise only
        this.writePolicy.recordExistsAction = RecordExistsAction.UPDATE;
    }

    @Override
    public AtmState get(String id) {
        return toState(id, client.get(queryPolicy, toKey(id), ID, LATEST_COMMITTED_VERSION, IN_SYNC_VERSION));
    }

    @Override
    public AtmState save(String id, long currentVersion) {
        client.put(new WritePolicy(writePolicy), toKey(id), toInitialState(id, currentVersion));
        return get(id);
    }

    @Override
    public AtmState initUpdateIfMatchVersion(String id, long updateDocumentVersion, Callable<Boolean> updateFunc) {
        try {
            final AtmState state = get(id);
            if (state.getInSyncVersion() != 0 || state.getLatestCommittedVersion() != updateDocumentVersion) {
                throw new IllegalArgumentException("Can not init update. Version mismatch. Entity is stale.");
            }
            client.operate(new WritePolicy(writePolicy), toKey(id), Operation.add(new Bin(IN_SYNC_VERSION, 1))); // inc
            if (!updateFunc.call()) {
                client.operate(new WritePolicy(writePolicy), toKey(id), Operation.add(new Bin(IN_SYNC_VERSION, -1))); // roll back
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return get(id);
    }

    @Override
    public AtmState commitUpdate(String id, long currentVersion) {
        try {
            client.operate(new WritePolicy(writePolicy), toKey(id),
                    Operation.put(new Bin(LATEST_COMMITTED_VERSION, currentVersion)),
                    Operation.add(new Bin(IN_SYNC_VERSION, -1))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AtmState state = get(id);
        // TODO fix this
        if (state.getInSyncVersion() < 0) {
            client.operate(new WritePolicy(writePolicy), toKey(id),
                    Operation.put(new Bin(IN_SYNC_VERSION, 0))
            );
        }
        return state;
    }

    private Key toKey(final String id) {
        return new Key(namespace, set, id);
    }

    public static Bin[] toInitialState(final String id, long currentVersion) {
        final List<Bin> bins = new ArrayList<>();
        bins.add(new Bin(ID, id));
        bins.add(new Bin(LATEST_COMMITTED_VERSION, currentVersion));
        bins.add(new Bin(IN_SYNC_VERSION, 0));
        return bins.toArray(new Bin[0]);
    }

    private AtmState toState(final String id, final Record record) {
        if (record == null) {
            return null;
        }

        final AtmState state = new AtmState();
        state.setId(id);
        state.setLatestCommittedVersion(record.getInt(LATEST_COMMITTED_VERSION));
        state.setInSyncVersion(record.getInt(IN_SYNC_VERSION));
        return state;
    }
}
