package com.ed.geospatial.processing.kafka.produce;

import com.ed.geospatial.processing.AtmEvent;

public interface ProduceToProcess {

    void produce(AtmEvent event) throws Exception;
}
