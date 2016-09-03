package com.elynx.pogoxmitm;

/**
 * Storage for on-off option
 */
public class BooleanOption {
    BooleanOption(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean active = false;
}
