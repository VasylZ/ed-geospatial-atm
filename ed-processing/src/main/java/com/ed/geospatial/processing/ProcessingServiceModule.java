package com.ed.geospatial.processing;

import com.ed.geospatial.processing.kafka.consume.ConsumeAndProcess;
import com.ed.geospatial.processing.kafka.consume.GeospatialKafkaConsumer;
import com.google.inject.AbstractModule;

public class ProcessingServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProcessingService.class).to(ProcessingServiceImpl.class).asEagerSingleton();
        bind(ConsumeAndProcess.class).to(GeospatialKafkaConsumer.class).asEagerSingleton();
    }
}
