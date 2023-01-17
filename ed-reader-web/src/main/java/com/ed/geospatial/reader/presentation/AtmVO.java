package com.ed.geospatial.reader.presentation;

import com.ed.geospatial.core.persistence.model.Atm;

public class AtmVO extends Atm {

    public AtmVO() {
    }

    public AtmVO(Atm other) {
        super(other);
    }

    private boolean isStale;

    public boolean isStale() {
        return isStale;
    }

    public void setStale(boolean stale) {
        isStale = stale;
    }
}
