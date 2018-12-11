/*
 *  GeoJSONGetter
 *
 *  Pulls down the GeoJSON data from the inf server
 *
 *  Based on course lectures
 *
 *
 */
package com.example.s1636469.coinz;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import android.widget.Toast;


import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;


public class GeoJSONGetter extends AsyncTask<String, Void, String> {

    private Activity activity;
    private MapboxMap mapboxMap;
    private Location location;
    private static String TAG = "GeoJSONGetter";

    protected GeoJSONGetter(Activity activity,MapboxMap mapboxMap, Location location) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
        this.location = location;
    }

    /*
     *  doInBackground
     *
     *  starts the download
     *
     */
    @Override
    protected String doInBackground(String... urls){
        try {
            Log.d(TAG,"Starting download");
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    /*
     *  loadFileFromNetwork
     *
     *  converts the inputs stream from the download into a string
     *
     */
    private String loadFileFromNetwork(String urlString) throws IOException {
        try {
            return readStream(downloadURL(new URL(urlString)));
        } catch (NullPointerException e) {
            return "";
        }

    }

    /*
     * downloadURL
     *
     *  opens an input stream to the inf server and returns it
     */
    private InputStream downloadURL(URL url)  {
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
            Log.w(TAG, "socket timed out",e);
            return null;
        } catch (IOException e){
            Log.w(TAG,"IOExpection", e);
            return null;
        }

    }

    /*
     *  readStream
     *
     *  Converts the input stream to string
     */
    @NonNull
    private String readStream(InputStream stream) {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    /*
     *  onPostExecute
     *
     *  Passes the GeoJSON string to the MapPoints class
     */
    @Override
    protected  void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null  || result.equals("")) {
            Log.d(TAG, "readStream was blank");
            Toast.makeText(this.activity, "Unable to get coin location data", Toast.LENGTH_LONG).show();
            return;
        }
        MapPoints.addMapPoints(result, activity, mapboxMap, location);
    }
}
