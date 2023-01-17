package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Area;
import com.vividsolutions.jts.geom.Point;

public interface AreaDao {

    Area find(Point point);
}
