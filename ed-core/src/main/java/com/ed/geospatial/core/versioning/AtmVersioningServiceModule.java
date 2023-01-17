package com.ed.geospatial.core.versioning;

import com.google.inject.AbstractModule;

public class AtmVersioningServiceModule extends AbstractModule {

    @Override
    protected void configure() {
//        bind(AtmVersioningService.class).to(AtmVersioningServiceInMemoryImpl.class).asEagerSingleton();
        bind(AtmVersioningService.class).to(AtmVersioningServiceAerospikeImpl.class).asEagerSingleton();
    }
}
