package com.example.s1636469.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapPoints {


    public static HashMap<String, Coin> coins = new HashMap<String, Coin>();
    public static HashMap<String, MarkerOptions> markers = new HashMap<String, MarkerOptions>();


    public static void plotMapPoints(Context context, MapboxMap mapboxMap) {
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon ic;
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.mapbox_marker_icon_default, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        //DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.SRC_IN);
        for (String c_key : coins.keySet()) {
            Coin c = coins.get(c_key);
            DrawableCompat.setTint(vectorDrawable,c.getColor());
            vectorDrawable.draw(canvas);
            ic = IconFactory.getInstance(context).fromBitmap(bitmap);

            LatLng pos = new LatLng(c.getLocation());
            MarkerOptions mo = new MarkerOptions().position(pos).icon(ic);
            mapboxMap.addMarker(mo);
            MapPoints.markers.put(c.getId(),mo);
        }
    }
    public static void addMapPoints(String file_string, Activity activity, MapboxMap mapboxMap, Location location) {
        String TAG = "MAP_PLOTTER";

        try {
            MapPoints.coins = new HashMap<String, Coin>();

            assert(file_string != null);
            assert(file_string.length() != 0);
            assert(!file_string.equals("{}"));
            JSONObject json = new JSONObject(file_string);
            JSONArray points = json.getJSONArray("features");

            // Get collected from database
            FirebaseFirestore.setLoggingEnabled(false);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            database.setFirestoreSettings(settings);

            final DocumentReference docRef = database.collection("users").document("initial");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        ArrayList<String> collected = (ArrayList<String>) task.getResult().getData().get("collected");
                        for (int i = 0; i < points.length(); i++) {
                            //Get Position of point and add marker
                            JSONObject feature = points.getJSONObject(i);
                            JSONObject geometery = feature.getJSONObject("geometry");
                            JSONArray coords = geometery.getJSONArray("coordinates");

                            //Add coin to the MapPoints object
                            JSONObject props = feature.getJSONObject("properties");
                            String id = (String) props.get("id");
                            //if we have already collected the coin
                            if (!collected.contains(id)){
                                String currency = (String) props.get("currency");
                                double value = Double.parseDouble((String) props.get("value"));
                                Location x = new Location("A");
                                x.setLatitude(coords.getDouble(1));
                                x.setLongitude(coords.getDouble(0));
                                MapPoints.coins.put(id, new Coin(id, currency, value, x));
                            }
                        }
                        plotMapPoints(activity,mapboxMap);
                        Log.d(TAG, "Added Coins to array");
                        CoinSearcher coinSearcher = new CoinSearcher(activity, mapboxMap);
                        coinSearcher.execute(location);
                    } catch (JSONException e) {
                        Log.e("ERROR", "error parsing json");
                    }
                }
            });
        } catch (JSONException e) {
            Log.d(TAG,"GeoJSONGetter failed at post exectue");
        }
    }
}
