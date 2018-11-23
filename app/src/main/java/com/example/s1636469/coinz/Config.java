package com.example.s1636469.coinz;

import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Config {
    public static final double TOTAL_COINS_PER_DAY = 25;
    public static final int CUR_VALUE_DP = 4;
    public static final int REQUEST_GPS = 1;
    public static final int REQUEST_PROFILE = 1;
    public static final double distanceForCollection = 25;
    public static final int SHIL_COLOR = Color.RED;
    public static final int QUID_COLOR = Color.BLUE;
    public static final int DOLR_COLOR  = Color.GREEN;
    public static final int PENY_COLOR  = Color.CYAN;
    public static final int NEAR_COLOR  = Color.YELLOW;
    public static final String[] currencies = new String[] {"PENY","DOLR","QUID","SHIL"};
    public static final String GEOJSON_LOCAL_FILE = "map_points.txt";

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
