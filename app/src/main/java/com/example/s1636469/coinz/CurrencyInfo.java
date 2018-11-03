package com.example.s1636469.coinz;

public class CurrencyInfo {

    private String type;
    private float value;
    private float trend;

    public CurrencyInfo(String type, float value, float trend) {
        this.type = type;
        this.value = value;
        this.trend = trend;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            CurrencyInfo o = (CurrencyInfo) obj;
            return this.getType().equals(o.getType());
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public String toString() {
        return this.getType() + ", " + this.value + ", " + this.trend;
    }

    public String getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public float getTrend() {
        return trend;
    }
}
