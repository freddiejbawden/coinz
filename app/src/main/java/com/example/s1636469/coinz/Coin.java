package com.example.s1636469.coinz;

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
        return id;
    }

}
