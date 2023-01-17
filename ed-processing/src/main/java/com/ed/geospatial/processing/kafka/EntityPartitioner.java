package com.ed.geospatial.processing.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

public class EntityPartitioner implements Partitioner {

    @Override
    public int partition(final String topic, final Object key, final byte[] keyBytes, final Object value, final byte[] valueBytes, final Cluster cluster) {
        final List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
        final int numPartitions = availablePartitions.size();
        if (key instanceof String) {
            String strKey = (String) key;
            return availablePartitions.get(Utils.toPositive(Utils.murmur2(strKey.getBytes())) % numPartitions).partition();
        }
        throw new UnsupportedOperationException("Partitioner by given key is not implemented");

    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void configure(Map<String, ?> map) {
        // do nothing
    }
}
