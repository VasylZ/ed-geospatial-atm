package com.ed.geospatial.core.persistence.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Point;

public class Atm {
    private String id;
    private boolean active;
    private String name;
    private Point point;
    private Area area;
    private String address;
    private Street street;
    private Building building;
    private long version;

    public Atm() {
    }

    public Atm(final Atm other) {
        this.id = other.id;
        this.active = other.active;
        this.name = other.name;
        this.point = other.point;
        this.area = other.area;
        this.address = other.address;
        this.street = other.street;
        this.building = other.building;
        this.version = other.version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @JsonIgnore
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private boolean active;
        private String name;
        private Point point;
        private Area area;
        private String address;
        private Street street;
        private Building building;
        private long version;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder point(Point point) {
            this.point = point;
            return this;
        }

        public Builder area(Area area) {
            this.area = area;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder street(Street street) {
            this.street = street;
            return this;
        }

        public Builder building(Building building) {
            this.building = building;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Atm build() {
            Atm atm = new Atm();
            atm.setId(id);
            atm.setActive(active);
            atm.setName(name);
            atm.setPoint(point);
            atm.setArea(area);
            atm.setAddress(address);
            atm.setStreet(street);
            atm.setBuilding(building);
            atm.setVersion(version);
            return atm;
        }
    }
}
