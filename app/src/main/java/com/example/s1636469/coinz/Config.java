/*
 *  Config Class for the App
 */
package com.example.s1636469.coinz;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Config {

    public static final int PICK_IMAGE = 1;


    public static final int ONE_MEGABYTE = 1024*1024;
    public static final double TOTAL_COINS_PER_DAY = 25;
    public static final int CUR_VALUE_DP = 4;
    public static final int REQUEST_GPS = 1;
    public static final double distanceForCollection = 25;
    public static final int SHIL_COLOR = Color.RED;
    public static final int QUID_COLOR = Color.BLUE;
    public static final int DOLR_COLOR  = Color.GREEN;
    public static final int PENY_COLOR  = Color.CYAN;
    public static final String[] currencies = new String[] {"PENY","DOLR","QUID","SHIL"};

    public static final String mapbox_key = "pk.eyJ1IjoiZnJlZGRpZWpiYXdkZW4iLCJhIjoiY2ptb3NtZHhrMDAwazNwbDgzM2l4YjI1MSJ9.zCqzFmwVZoUGtTJgeZOMTw";

    public static final HashMap<String, Object> blank_user_profile = new HashMap<String, Object>() {{
        put("DOLR",0);
        put("PENY",0);
        put("QUID",0);
        put("SHIL",0);
        put("GOLD",0);
        put("coins_today",0);
        put("collected",new ArrayList<String>());
        put("last_login",Calendar.getInstance().getTime());
        put("name","");
        put("profile_url","");
        put("trades",new ArrayList<HashMap<String, Object>>());
        put("weekly_GOLD",0);
        put("email","");
        put("last_week_login",Calendar.getInstance().getTime());
    }};

    public static String getGeoJSONURL() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(calendar.getTime());
    }

    // TAKEN FROM: https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
