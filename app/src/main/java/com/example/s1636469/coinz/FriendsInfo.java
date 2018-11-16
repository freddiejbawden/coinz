package com.example.s1636469.coinz;

import android.graphics.Bitmap;

public class FriendsInfo {
    private String name;
    private Bitmap img;

    public FriendsInfo(String name, Bitmap img) {
        this.name = name;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImg() {
        return img;
    }
}
