package com.ed.geospatial.core.versioning.aerospike;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import com.ed.geospatial.core.shared.ConfUtils;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAerospikeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAerospikeClient.class);

    public static final String CONFIG_FILE_NAME = "aerospike-connection.json";

    public final AerospikeClient client;

    public final String namespace;
    public final String set;

    public AbstractAerospikeClient() {
        this.client = connect();

        final Config config = ConfUtils.load(CONFIG_FILE_NAME);
        this.namespace = config.getString("aNamespace");
        this.set = config.getString("aSet");
    }

    private AerospikeClient connect() {
        LOGGER.debug("Start Aerospike connection init");
        final Config config = ConfUtils.load(CONFIG_FILE_NAME);

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
}
