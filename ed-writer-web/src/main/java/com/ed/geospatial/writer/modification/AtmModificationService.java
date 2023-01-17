package com.ed.geospatial.writer.modification;

import com.ed.geospatial.core.persistence.model.Atm;

public interface AtmModificationService {

    String create(Atm mo);

    void update(Atm mo);
}
