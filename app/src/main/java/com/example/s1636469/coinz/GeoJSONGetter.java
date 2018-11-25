package com.example.s1636469.coinz;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static android.support.constraint.Constraints.TAG;

public class GeoJSONGetter extends AsyncTask<String, Void, String> {
    static String out;
    private Activity activity;
    private MapboxMap mapboxMap;
    private Location location;
    private TextView coin_combo_indicator;
    private static String TAG = "GeoJSONGetter";

    public GeoJSONGetter(Activity activity,MapboxMap mapboxMap, Location location, TextView coin_combo_indicator) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
        this.location = location;
        this.coin_combo_indicator = coin_combo_indicator;
    }
    public static void downloadComplete(String result) {
        GeoJSONGetter.out = result;
    }

    @Override
    protected String doInBackground(String... urls){
        try {
            Log.d(TAG,"Starting download");
            return loadFileFromNetwork("http://homepages.inf.ed.ac.uk/stg/coinz/2018/11/05/coinzmap.geojson");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return "Unable to load content";
        }
    }
    private String loadFileFromNetwork(String urlString) throws IOException {
        try {
            return readStream(downloadURL(new URL(urlString)));
        } catch (NullPointerException e) {
            return "";
        }

    }
    private InputStream downloadURL(URL url) throws IOException {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            Log.d(TAG,"Response Code: " + conn.getResponseCode());
            return conn.getInputStream();
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "socket timed out");
            return null;
        } catch (IOException e){
            Log.d(TAG,"IOExpection");
            return null;
        }

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
        if (result.equals("")) {
            Log.d(TAG, "readStream was blank");
            Toast.makeText(this.activity, "Unable to get coin location data", Toast.LENGTH_LONG).show();
            return;
        }
        assert (result != null);

        MapView mapView = (MapView) this.activity.findViewById(R.id.mapView);
        MapPoints.addMapPoints(result, activity, mapboxMap, location);
    }
}
