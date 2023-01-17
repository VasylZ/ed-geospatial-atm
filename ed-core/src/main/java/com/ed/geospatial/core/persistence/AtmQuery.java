package com.ed.geospatial.core.persistence;


import com.vividsolutions.jts.geom.Point;

public class AtmQuery {
    private Point point;
    private int radius;
    private int limit;
    private int offset;

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Point point;
        private int radius = 100;
        private int limit;
        private int offset;

        private Builder() {
        }

        public Builder point(Point point) {
            this.point = point;
            return this;
        }

        public Builder radius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public AtmQuery build() {
            AtmQuery atmQuery = new AtmQuery();
            atmQuery.setPoint(point);
            atmQuery.setRadius(radius);
            atmQuery.setLimit(limit);
            atmQuery.setOffset(offset);
            return atmQuery;
        }
    }
}
