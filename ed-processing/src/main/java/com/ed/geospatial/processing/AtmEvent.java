package com.ed.geospatial.processing;

import com.ed.geospatial.core.persistence.model.Atm;

public class AtmEvent {
    private AtmEventType type;
    private Atm atm;

    public AtmEvent() {
    }

    public AtmEventType getType() {
        return type;
    }

    public void setType(AtmEventType type) {
        this.type = type;
    }

    public Atm getAtm() {
        return atm;
    }

    public void setAtm(Atm atm) {
        this.atm = atm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private AtmEventType type;
        private Atm atm;

        private Builder() {
        }

        public Builder createAtmEvent(Atm atm) {
            this.atm = atm;
            this.type = AtmEventType.CREATE;
            return this;
        }

        public Builder updateAtmEvent(Atm atm) {
            this.atm = atm;
            this.type = AtmEventType.UPDATE;
            return this;
        }

        public AtmEvent build() {
            AtmEvent atmEvent = new AtmEvent();
            atmEvent.setAtm(atm);
            atmEvent.setType(type);
            return atmEvent;
        }
    }
}
