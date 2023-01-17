package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.postgres.PostgresConnectionPool;
import com.google.inject.AbstractModule;

public class AtmPersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AtmDto.class).to(AtmDtoPostgresImpl.class).asEagerSingleton();
        bind(AreaDao.class).to(AreaDaoPostgresImpl.class).asEagerSingleton();
        bind(StreetDao.class).to(StreetDaoPostgresImpl.class).asEagerSingleton();
        bind(BuildingDto.class).to(BuildingDtoPostgresImpl.class).asEagerSingleton();
        bind(PostgresConnectionPool.class).toInstance(new PostgresConnectionPool());
    }
}
