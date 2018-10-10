package com.example.s1636469.coinz;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

public class MapPoints {
    public static List<Coin> coins = new ArrayList<Coin>();
    public static void addMapPoints(Context context, MapboxMap mapboxMap, List<Coin> coins) {
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon ic;
        for (Coin c : coins) {
            if (c.isNearby()) {
                ic = iconFactory.fromResource(R.drawable.ic_coin_near);
            } else {
                ic = iconFactory.fromResource(R.drawable.ic_coin_not_near);
            }
            LatLng pos = new LatLng(c.getLocation());
            MarkerOptions mo = new MarkerOptions().position(pos).icon(ic);
            mapboxMap.addMarker(mo);
        }
    }
}
