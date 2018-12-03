/*
 *  Bank Info
 *
 *  Stores information about currencies to be displayed in
 *  the recyclerview
 *
 */
package com.example.s1636469.coinz;

public class BankInfo {
    private String currency;
    private double value;
    private double change;

    public BankInfo(String currency, double value, double change) {
        this.currency = currency;
        this.value = value;
        this.change = change;
    }

    public String getCurrency() {
        return currency;
    }

    public double getValue() {
        return value;
    }

    public double getChange() {
        return change;
    }
}
