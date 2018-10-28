package com.example.s1636469.coinz;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
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
    private MapboxMap mapboxMap;
    private Location location;


    public GeoJSONGetter(Activity activity,MapboxMap mapboxMap, Location location) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
        this.location = location;
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
        conn.setReadTimeout(1000);
        conn.setConnectTimeout(5000);
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

        try {
            MapPoints.coins = new ArrayList<Coin>();
            String s = GeoJSONGetter.out;
            System.out.println(s);
            assert(s != null);
            assert(s.length() != 0);
            assert(!s.equals("{}"));
            JSONObject json = new JSONObject(s);
            JSONArray points = json.getJSONArray("features");
            System.out.println(points.length());

            for (int i = 0; i < points.length(); i++) {
                //Get Position of point and add marker
                JSONObject feature = points.getJSONObject(i);
                JSONObject geometery = feature.getJSONObject("geometry");
                JSONArray coords = geometery.getJSONArray("coordinates");

                //Add coin to the MapPoints object
                JSONObject props = feature.getJSONObject("properties");
                String id = (String) props.get("id");
                String currency = (String) props.get("currency");
                double value = Double.parseDouble((String) props.get("value"));
                Location x = new Location("A");
                x.setLatitude(coords.getDouble(1));
                x.setLongitude(coords.getDouble(0));
                MapPoints.coins.add(new Coin(id,currency,value,x));
            }
            Log.d("STATUS","Added Coins to array");
            CoinSearcher coinSearcher = new CoinSearcher(this.activity, mapboxMap);
            coinSearcher.execute(this.location);


        } catch (JSONException e) {
            Log.d("STATUS","GeoJSONGetter failed at post exectue");
        }
    }
}
