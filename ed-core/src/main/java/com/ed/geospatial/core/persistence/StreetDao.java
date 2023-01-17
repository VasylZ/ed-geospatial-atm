package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Street;
import com.vividsolutions.jts.geom.Point;

public interface StreetDao {

    Street find(Point point);
}
