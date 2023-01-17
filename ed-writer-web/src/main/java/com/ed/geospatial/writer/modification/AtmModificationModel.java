package com.ed.geospatial.writer.modification;

import com.google.inject.AbstractModule;

public class AtmModificationModel extends AbstractModule {

    @Override
    protected void configure() {
        bind(AtmModificationService.class).to(AtmModificationServiceImpl.class);
    }
}
