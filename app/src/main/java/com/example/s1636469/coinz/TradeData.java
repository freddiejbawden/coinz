package com.example.s1636469.coinz;

public class TradeData {

    private boolean from_user;
    private String from;
    private String to;
    private String amount;
    private String cur_type;

    public TradeData(boolean from_user, String from, String to ,String amount, String cur_type) {
        this.from_user = from_user;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.cur_type = cur_type;
    }

    public boolean getFromUser() {
        return from_user;
    }

    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public String getAmount() {
        return amount;
    }

    public String get_cur_type() {
        return cur_type;
    }
}
