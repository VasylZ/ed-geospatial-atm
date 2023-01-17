package com.ed.geospatial.processing;

import com.ed.geospatial.processing.kafka.produce.GeospatialKafkaProducer;
import com.ed.geospatial.processing.kafka.produce.ProduceToProcess;
import com.google.inject.AbstractModule;

public class ProducingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProduceToProcess.class).to(GeospatialKafkaProducer.class).asEagerSingleton();
    }
}
