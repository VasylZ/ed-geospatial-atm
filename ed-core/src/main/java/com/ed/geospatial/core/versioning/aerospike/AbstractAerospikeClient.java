package com.ed.geospatial.core.versioning.aerospike;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Host;
import com.aerospike.client.Record;
import com.aerospike.client.ResultCode;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.IndexTask;
import com.ed.geospatial.core.shared.ConfUtils;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractAerospikeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAerospikeClient.class);

    private static final String AEROSPIKE_CONNECT_CONFIG_FILE_NAME = "aerospike-connection.json";

    public final AerospikeClient client;

    public final String namespace;
    public final String set;

    public AbstractAerospikeClient(final String namespace, final String set) {
        this.namespace = namespace;
        this.set = set;
        this.client = connect();
    }

    private AerospikeClient connect() {
        LOGGER.debug("Start Aerospike connection init");
        final Config config = ConfUtils.load(AEROSPIKE_CONNECT_CONFIG_FILE_NAME);

        final String host = ConfUtils.getStrVal(config, "aDbHost");
        final int port = config.getInt("aDbPort");

        final int maxConnsPerNode = config.getInt("aMaxConnsPerNode");
        final int connPoolsPerNode = config.getInt("aConnPoolsPerNode");

        final ClientPolicy clientPolicy = new ClientPolicy();
        clientPolicy.maxConnsPerNode = maxConnsPerNode;
        clientPolicy.connPoolsPerNode = connPoolsPerNode;

        final Host aerospikeHost = new Host(host, port);

        AerospikeClient aerospikeClient = new AerospikeClient(clientPolicy, aerospikeHost);
        LOGGER.debug("Start Aerospike connection established on [{}:{}]", host, port);
        return aerospikeClient;
    }

    // convenience function to execute a query using the input filter and print the results
    protected  <T> List<T> executeQueryAndGetResults(final QueryPolicy queryPolicy, final Filter filter, final Function<Record, T> mapper, final String... binNames) {
        return executeQueryAndGetResults(queryPolicy, filter, mapper, 0L, Long.MAX_VALUE, binNames);
    }

    protected  <T> List<T> executeQueryAndGetResults(final QueryPolicy queryPolicy, final Filter filter, final Function<Record, T> mapper, long offset, long limit, final String... binNames) {
        final Statement stmt = new Statement();
        stmt.setNamespace(namespace);
        stmt.setSetName(set);
        stmt.setFilter(filter);
        stmt.setBinNames(binNames);

        long position = 0;
        long endPosition = offset + limit;
        try (final RecordSet rs = client.query(queryPolicy, stmt)) {
            final List<T> result = new ArrayList<>();
            while (rs.next()) {
                if (position < offset) continue;
                if (position >= endPosition) break;
                position++;

                final T val = mapper.apply(rs.getRecord());
                if (val != null) {
                    result.add(val);
                }
            }
            return result;
        }
    }

    protected long executeQueryAndCount(final QueryPolicy queryPolicy, final Filter filter, final String idBeanName) {
        final Statement stmt = new Statement();
        stmt.setNamespace(namespace);
        stmt.setSetName(set);
        stmt.setFilter(filter);
        stmt.setBinNames(idBeanName);

        int result = 0;
        try (final RecordSet rs = client.query(queryPolicy, stmt)) {
            while (rs.next()) {
                result++;
            }
            return result;
        }
    }

    protected void createIndex(final IndexType indexType, final String indexName, final String binName) {
        LOGGER.info("Create {} index: ns={} set={} index={} bin={}", indexType, namespace, set, indexName, binName);

        Policy policy = new Policy();
        policy.socketTimeout = 0; // Do not timeout on index create.

        try {
            final IndexTask task = client.createIndex(policy, namespace, set, indexName, binName, indexType);
            task.waitTillComplete();
            LOGGER.info("Index {} created: ns={} set={} index={} bin={}", indexType, namespace, set, indexName, binName);
        } catch (final AerospikeException ae) {
            LOGGER.error("Fail to create {} index cause {}", indexType, ae.getMessage(), ae);
            if (ae.getResultCode() != ResultCode.INDEX_ALREADY_EXISTS) {
                throw ae;
            }
        }
    }
}
