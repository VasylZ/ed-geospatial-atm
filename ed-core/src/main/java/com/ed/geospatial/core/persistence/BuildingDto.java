package com.ed.geospatial.core.persistence;

import com.ed.geospatial.core.persistence.model.Building;
import com.vividsolutions.jts.geom.Point;

public interface BuildingDto {

    Building find(Point point);
}
