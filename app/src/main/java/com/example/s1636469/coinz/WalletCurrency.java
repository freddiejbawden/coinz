/*
 *  WalletCurrency
 *
 *  Container class for the wallet recycler
 */
package com.example.s1636469.coinz;

public class WalletCurrency {
    private String type;
    private Double value;

    public WalletCurrency(String type, Double value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }


    public Double getValue() {
        return value;
    }

}
