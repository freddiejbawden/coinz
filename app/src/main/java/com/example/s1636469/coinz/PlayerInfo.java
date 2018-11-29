package com.example.s1636469.coinz;

public class PlayerInfo {

    private String place;
    private String u_name;
    private String gold;

    public PlayerInfo(String place, String u_name, String gold) {
        this.place = place;
        this.u_name = u_name;
        this.gold = gold;
    }
    public String getPlace() {
        return place;
    }

    public String getU_name() {
        return u_name;
    }

    public String getGold() {
        return gold;
    }


    @Override
    public String toString() {
        return u_name;
    }
}
