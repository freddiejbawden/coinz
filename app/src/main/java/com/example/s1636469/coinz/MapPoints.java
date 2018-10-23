package com.example.s1636469.coinz;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

public class MapPoints {
    public static List<Coin> coins = new ArrayList<Coin>();
    public static void addMapPoints(Context context, MapboxMap mapboxMap) {
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon ic;
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.mapbox_marker_icon_default, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        //DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.SRC_IN);
        for (Coin c : coins) {
            DrawableCompat.setTint(vectorDrawable,c.getColor());
            vectorDrawable.draw(canvas);
            ic = IconFactory.getInstance(context).fromBitmap(bitmap);

            LatLng pos = new LatLng(c.getLocation());
            MarkerOptions mo = new MarkerOptions().position(pos).icon(ic);
            mapboxMap.addMarker(mo);
        }
    }
}
