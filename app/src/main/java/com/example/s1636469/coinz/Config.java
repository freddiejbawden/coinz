package com.example.s1636469.coinz;

import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Config {
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
}
