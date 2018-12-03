/*
 *  Coin
 *
 *  Container class for holding information about Coins
 *
 */
package com.example.s1636469.coinz;

import android.location.Location;

public class Coin {

    private String id;
    private String currency;
    private Location location;
    private double value;
    Coin(String id, String currency, double value, Location location) {
        this.id = id;
        this.currency = currency;
        this.location = location;
        this.value = value;
    }
    public String getCurrency() {return currency;}
    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public double getValue() {
        return value;
    }

    //Return color based on
    public int getColor() {
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
        return Config.QUID_COLOR;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == Coin.class) {
            Coin oCoin = (Coin) o;
            return (oCoin.getId().equals(this.getId()));
        } else {
            return false;
        }
    }
}
