package com.ed.geospatial.processing.kafka.consume;

import com.ed.geospatial.core.shared.ConfUtils;
import com.ed.geospatial.core.shared.ObjectMapperHolder;
import com.ed.geospatial.processing.AtmEvent;
import com.ed.geospatial.processing.ProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class GeospatialKafkaConsumer implements ConsumeAndProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeospatialKafkaConsumer.class);

    public static final String KAFKA_CONNECT_CONFIG_FILE = "ed-kafka-connect.json";
    public static final String CONSUMER_CONFIG_FILE = "geospatial-mo-consumer-kafka.json";

    private final ProcessingService processingService;

    private KafkaConsumer<String, String> consumer;

    @Inject
    public GeospatialKafkaConsumer(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public void startConsuming() {
        initConsumer();

        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            try {
                if (records.isEmpty()) {
                    continue;
                }
                final List<AtmEvent> atms = new ArrayList<>();
                for (final ConsumerRecord<String, String> record : records) {
                    atms.add(deSerialise(record.value()));
                }
                processingService.process(atms);
                consumer.commitSync();
            } catch (final Exception e) {
                LOGGER.error("Fail to consume and process atms cause {}", e.getMessage(), e);
                waitToProceed();
            }
        }
    }

    private AtmEvent deSerialise(final String json) throws JsonProcessingException {
        return ObjectMapperHolder.HOLDER.mapper().readValue(json, AtmEvent.class);
    }

    private void initConsumer() {
        final Config kafkaConnect = ConfUtils.load(KAFKA_CONNECT_CONFIG_FILE);
        final Config config = ConfUtils.load(CONSUMER_CONFIG_FILE);

        final String bootstrapServersHost = ConfUtils.getStrVal(kafkaConnect, "kafkaConsumerHost");
        final int bootstrapServersPort = Integer.parseInt(ConfUtils.getStrVal(kafkaConnect, "kafkaConsumerPort"));
        final String bootstrapServers = bootstrapServersHost + ":" + bootstrapServersPort;

        final String topic = config.getString("topic");
        final String groupId = config.getString("groupId");
        final String autoOffsetReset = config.getString("autoOffsetReset");

        LOGGER.debug("Configure consumer with bootstrapServers[{}]", bootstrapServers);

        final Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(false));
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        this.consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Collections.singletonList(topic));
        LOGGER.info("Consumer subscribed for {} topic successfully", topic);
    }

    private void waitToProceed() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
