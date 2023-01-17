package com.ed.geospatial.core.persistence.model;

import com.ed.geospatial.core.shared.ObjectMapperHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vividsolutions.jts.geom.Point;

public class PointMapper {

    private PointMapper() {
        throw new IllegalStateException("Should not be instantiated.");
    }

    public static Point toPoint(final String geoJsonPoint) {
        try {
            return ObjectMapperHolder.HOLDER.mapper().readValue(geoJsonPoint, Point.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String pointToGeoJson(final Point point) {
        try {
            return ObjectMapperHolder.HOLDER.mapper().writeValueAsString(point);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
