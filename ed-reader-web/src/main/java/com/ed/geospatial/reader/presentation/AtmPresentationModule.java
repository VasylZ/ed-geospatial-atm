package com.ed.geospatial.reader.presentation;

import com.google.inject.AbstractModule;

public class AtmPresentationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AtmPresentationService.class).to(AtmPresentationServiceImpl.class).asEagerSingleton();;
    }
}
