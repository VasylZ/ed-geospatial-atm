package com.ed.geospatial.processing;

import com.ed.geospatial.core.persistence.AtmDto;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.versioning.AtmVersioningService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessingServiceImpl implements ProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingServiceImpl.class);

    @Inject
    private AtmDto persistence;
    @Inject
    private AtmVersioningService versioningService;

    @Override
    public void process(List<AtmEvent> events) {
        // TODO refactor to bulk save ?
        events.forEach(atmEvent -> {
            final String atmId = atmEvent.getAtm().getId();
            switch (atmEvent.getType()) {
                case CREATE:
                    if (versioningService.get(atmId) != null) {;
                        final Atm atm = persistence.create(atmEvent.getAtm());
                        LOGGER.info("Create atm executed");
                        versioningService.commitUpdate(atmId, atm.getVersion());
                    } else {
                        LOGGER.info("Create atm can not be executed cause sate not found.");
                    }
                    break;
                case UPDATE:
                    if (versioningService.get(atmId) != null) {
                        Atm atm = persistence.update(atmEvent.getAtm());
                        LOGGER.info("Update atm executed");
                        versioningService.commitUpdate(atmId, atm.getVersion());
                    } else {
                        LOGGER.info("Update atm can not be executed cause sate not found.");
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(atmEvent.getType() + " is unsupported");
            }
        });
    }
}
