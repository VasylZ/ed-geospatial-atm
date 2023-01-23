package com.ed.geospatial.writer.modification;

import com.ed.geospatial.core.persistence.AreaDao;
import com.ed.geospatial.core.persistence.AtmDto;
import com.ed.geospatial.core.persistence.BuildingDto;
import com.ed.geospatial.core.persistence.StreetDao;
import com.ed.geospatial.core.persistence.model.Area;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.Street;
import com.ed.geospatial.core.versioning.AtmState;
import com.ed.geospatial.core.versioning.AtmVersioningService;
import com.ed.geospatial.processing.AtmEvent;
import com.ed.geospatial.processing.kafka.produce.ProduceToProcess;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class AtmModificationServiceImpl implements AtmModificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtmModificationServiceImpl.class);

    private final Supplier<String> ID_GEN = () -> UUID.randomUUID().toString();

    @Inject
    private ProduceToProcess produceToProcess;
    @Inject
    private AtmVersioningService versioningService;
    @Inject
    private AtmDto atmDto;
//    @Inject
//    private AreaDao areaDao;
//    @Inject
//    private StreetDao streetDao;
//    @Inject
//    private BuildingDto buildingDto;

    @Override
    public String create(Atm mo) {
        if (!isEmpty(mo.getId())) {
            throw new IllegalArgumentException("Id parameter must be empty while creation");
        }
        try {
            mo.setId(ID_GEN.get());

            fillAddressInfo(mo);

            versioningService.save(mo.getId(), 0L);

            final AtomicReference<Exception> produceException = new AtomicReference<>();
            versioningService.initUpdateIfMatchVersion(mo.getId(), 0, () -> {
                try {
                    produceToProcess.produce(AtmEvent.builder()
                            .createAtmEvent(mo)
                            .build());
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Fail to create atm cause {}", e.getMessage(), e);
                    produceException.set(e);
                    return false;
                }
            });
            if (produceException.get() != null) {
                throw produceException.get();
            }
            return mo.getId();
        } catch (Exception e) {
            LOGGER.error("Fail to create atm cause {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Atm mo) {
        AtmState atmState = versioningService.get(mo.getId());
        if (atmState == null) {
            // TODO unusual situation. think how to handle
            versioningService.save(mo.getId(), atmDto.get(mo.getId()).getVersion());
        }
        try {
            fillAddressInfo(mo);

            final AtomicReference<Exception> produceException = new AtomicReference<>();
            versioningService.initUpdateIfMatchVersion(mo.getId(), mo.getVersion(), () -> {
                try {
                    produceToProcess.produce(AtmEvent.builder()
                            .updateAtmEvent(mo)
                            .build());
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Fail to update atm cause {}", e.getMessage(), e);
                    produceException.set(e);
                    return false;
                }
            });
            if (produceException.get() != null) {
                throw produceException.get();
            }
        } catch (Exception e) {
            LOGGER.error("Fail to update atm cause {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void fillAddressInfo(Atm mo) {
//        Area area = areaDao.find(mo.getPoint());
//        if (area == null) {
//            throw new IllegalArgumentException("Point area not supported");
//        }
//        mo.setArea(area);
//
//        Street street = streetDao.find(mo.getPoint());
//        if (street == null) {
//            throw new IllegalArgumentException("Point street not supported");
//        }
//        mo.setStreet(street);
//
//        mo.setBuilding(buildingDto.find(mo.getPoint()));
    }
}
