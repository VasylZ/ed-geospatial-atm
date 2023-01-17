package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.ResultLine;

import java.util.List;

public interface AtmDto {

    Atm get(String id);

    List<ResultLine<Atm>> find(AtmQuery query);

    long count(AtmQuery query);

    Atm create(Atm dto);

    Atm update(Atm dto);

    void remove(String id);
}
