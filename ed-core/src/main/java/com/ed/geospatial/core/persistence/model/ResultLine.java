package com.ed.geospatial.core.persistence.model;

public class ResultLine<T> {

    private T atm;
    private int distance;

    public T getAtm() {
        return atm;
    }

    public void setAtm(T atm) {
        this.atm = atm;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
