package com.example.s1636469.coinz;

import android.app.Activity;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GeoJSONGetter extends AsyncTask<String, Void, String> {
    static String out;
    private Activity activity;
    public GeoJSONGetter(Activity activity) {
        this.activity = activity;
    }
    public static void downloadComplete(String result) {
        GeoJSONGetter.out = result;
    }

    @Override
    protected String doInBackground(String... urls){
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            Log.e("STATUS", e.toString());
            return "Unable to load content";
        }
    }
    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadURL(new URL(urlString)));
    }
    private InputStream downloadURL(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        System.out.println(conn.getResponseCode());
        return conn.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream) throws IOException {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
    @Override
    protected  void onPostExecute(String result) {

        super.onPostExecute(result);
        GeoJSONGetter.downloadComplete(result);
        assert (result != null);
        MapView mapView = (MapView) this.activity.findViewById(R.id.mapView);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                try {
                    Log.d("STATUS","RUNNING");
                    String s = GeoJSONGetter.out;
                    System.out.println(s);
                    JSONObject json = new JSONObject(s);
                    Log.d("STATUS","RUNNING 2");

                    JSONArray points = json.getJSONArray("features");
                    System.out.println(points.length());
                    for (int i = 0; i < points.length(); i++) {
                        System.out.println("HEHEHE");
                        JSONObject feature = points.getJSONObject(i);
                        Log.d("FEATURE",feature.toString());
                        JSONObject geometery = feature.getJSONObject("geometry");
                        JSONArray coords = geometery.getJSONArray("coordinates");
                        LatLng pos = new LatLng(coords.getDouble(1), coords.getDouble(0));
                        MarkerOptions mo = new MarkerOptions().position(pos);
                        mapboxMap.addMarker(mo);
                        Log.d("UPDATE","Adding pointer");
                    }
                } catch (JSONException e) {

                }
            }
        });

    }
}
