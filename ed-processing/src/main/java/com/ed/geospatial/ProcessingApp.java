package com.ed.geospatial;

import com.ed.geospatial.core.persistence.AtmPersistenceModule;
import com.ed.geospatial.core.versioning.AtmVersioningServiceModule;
import com.ed.geospatial.processing.ProcessingServiceModule;
import com.ed.geospatial.processing.kafka.consume.GeospatialKafkaConsumer;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ProcessingApp {

    public static void main(String[] args) {
        System.out.println("Start init Guice.");

        Injector injector = Guice.createInjector(
                new AtmPersistenceModule(),
                new ProcessingServiceModule(),
                new AtmVersioningServiceModule()
        );

        System.out.println("Kafka Consuming: init");
        GeospatialKafkaConsumer consumer = injector.getInstance(GeospatialKafkaConsumer.class);
        injector.injectMembers(consumer);
        consumer.startConsuming();
    }
}
