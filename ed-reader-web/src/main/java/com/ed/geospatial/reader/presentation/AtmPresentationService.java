package com.ed.geospatial.reader.presentation;

import com.ed.geospatial.core.persistence.AtmQuery;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.core.persistence.model.ResultLine;
import com.ed.geospatial.reader.shared.ResponseData;

public interface AtmPresentationService {

    AtmVO get(String id);

    ResponseData<ResultLine<AtmVO>> find(AtmQuery query);
}
