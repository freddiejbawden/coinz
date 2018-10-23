package com.example.s1636469.coinz;

import android.graphics.Color;
import android.location.Location;

public class Coin {

    private String id;
    private String currency;
    private Location location;
    private double value;
    private boolean nearby;
    public Coin(String id, String currency,double value, Location location) {
        this.id = id;
        this.currency = currency;
        this.location = location;
        this.value = value;
        this.nearby = false;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public double getValue() {
        return value;
    }
    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }
    public boolean isNearby() {
        return nearby;
    }
    @Override
    public String toString() {
        return id + ", " + isNearby();
    }
    public int getColor() {
        if (this.isNearby()) {
            return Config.NEAR_COLOR;
        } else {
            if (this.currency.equals("QUID")) {
                return Config.QUID_COLOR;
            }
            if (this.currency.equals("PENY")) {
                return Config.PENY_COLOR;
            }
            if (this.currency.equals("DOLR")) {
                return Config.DOLR_COLOR;
            }
            if (this.currency.equals("SHIL")) {
                return Config.SHIL_COLOR;
            }
            return 0;
        }
    }
}
