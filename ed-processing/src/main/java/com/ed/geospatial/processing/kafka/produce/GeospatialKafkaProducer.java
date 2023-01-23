package com.ed.geospatial.processing.kafka.produce;

import com.ed.geospatial.core.shared.ConfUtils;
import com.ed.geospatial.core.shared.ObjectMapperHolder;
import com.ed.geospatial.processing.AtmEvent;
import com.ed.geospatial.processing.kafka.EntityPartitioner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class GeospatialKafkaProducer implements ProduceToProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeospatialKafkaProducer.class);

    public static final String KAFKA_CONNECT_CONFIG_FILE = "ed-kafka-connect.json";
    public static final String PRODUCER_CONFIG_FILE = "geospatial-mo-producer-kafka.json";

    private final String topic;
    private final KafkaProducer<String, String> producer;

    public GeospatialKafkaProducer() {
        final Config kafkaConnect = ConfUtils.load(KAFKA_CONNECT_CONFIG_FILE);
        final Config config = ConfUtils.load(PRODUCER_CONFIG_FILE);

        final String bootstrapServersHost = ConfUtils.getStrVal(kafkaConnect, "kafkaProducerHost");
        final int bootstrapServersPort = Integer.parseInt(ConfUtils.getStrVal(kafkaConnect, "kafkaProducerPort"));
        final String bootstrapServers = bootstrapServersHost + ":" + bootstrapServersPort;
        final String topic = config.getString("topic");

        LOGGER.debug("Configure producer with bootstrapServers[{}]", bootstrapServers);

        final Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG, EntityPartitioner.class.getName());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(properties);
        this.topic = topic;

        LOGGER.info("Producer connect for {} topic successfully", topic);
    }

    @Override
    public void produce(final AtmEvent atmEvent) {
        String serialised = null;
        try {
            serialised = serialise(atmEvent);
            producer.send(new ProducerRecord<>(topic, String.valueOf(atmEvent.getAtm().getId()), serialised));
        } catch (RuntimeException e) {
            LOGGER.error("Fail to produce message {} cause {}", serialised, e.getMessage(), e);
        }
    }

    private String serialise(final AtmEvent atm) {
        try {
            return ObjectMapperHolder.HOLDER.mapper().writeValueAsString(atm);
        } catch (JsonProcessingException e) {
            LOGGER.error("Fail to serialize message cause {}", e.getMessage());
            return null;
        }
    }
}
