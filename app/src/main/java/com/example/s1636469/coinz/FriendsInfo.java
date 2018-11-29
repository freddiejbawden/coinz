package com.example.s1636469.coinz;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FriendsInfo {
    private String name;
    private Bitmap img;
    private String id;

    public FriendsInfo(String name, Bitmap img, String id) {
        this.name = name;
        this.img = img;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getId() { return id; }

    public void setImg(Bitmap b) {
        this.img = b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FriendsInfo) {
            return ((FriendsInfo) obj).getId().equals(this.id);
        }
        return false;

    }

    @Override
    public String toString() {
        return this.name + ", " + this.id;
    }
}
