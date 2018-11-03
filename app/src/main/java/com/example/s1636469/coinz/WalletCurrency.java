package com.example.s1636469.coinz;

public class WalletCurrency {
    private String type;
    private float value;

    public WalletCurrency(String type, float value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }


    public float getValue() {
        return value;
    }

}
