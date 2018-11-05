package com.example.s1636469.coinz;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinSearcher extends AsyncTask<Location, Void, Void> {
    private List<Coin> coinsNearbyCurrentLocation;
    private Activity activity;
    private MapboxMap mapboxMap;
    private String TAG = "CoinSearcher";


    public CoinSearcher(Activity activity,MapboxMap mapboxMap) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
    }
    protected Void doInBackground(Location... location) {
        if (location[0] == null) {
            Log.d(TAG,"Location is null so cannot perform coin search");
            return null;
        }
        Log.d(TAG,"Starting coin search");
        if (MapPoints.coins.size() == 0) {
            Log.d(TAG, "Unable to get list of coins");
            Toast.makeText(activity, "Unable to get list of coins, please try again later.",Toast.LENGTH_LONG);
            return null;
        }
        // Pull current coin data
        FirebaseFirestore.setLoggingEnabled(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);
        //TODO: Replace Placeholder name with localStorage retrival
        final DocumentReference docRef = database.collection("users").document("initial");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Location userLocation = location[0];
                Coin c;

                Map<String, Object> data = task.getResult().getData();
                ArrayList<String> collected = (ArrayList<String>) data.get("collected");
                ArrayList<String> nearbyIds = new ArrayList<String>();
                for (int i = 0; i < MapPoints.coins.size(); i++) {
                    c = MapPoints.coins.get(i);
                    float dist = userLocation.distanceTo(c.getLocation()) ;
                    if (dist < Config.distanceForCollection) {
                        Coin nearbyCoin = MapPoints.coins.get(i);
                        Log.d(TAG,nearbyCoin.getId());
                        double val = nearbyCoin.getValue();
                        String cur = nearbyCoin.getCurrency();
                        double new_val = Double.parseDouble((String) data.get(cur));
                        data.put(cur,new_val+"");

                        //add to collected list
                        nearbyIds.add(nearbyCoin.getId());

                        //Remove the marker from the map
                        mapboxMap.removeMarker(MapPoints.markers.get(i).getMarker());
                        MapPoints.coins.remove(i);

                    }
                }
                collected.addAll(nearbyIds);
                data.put("collected",collected);
                docRef.set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"Done.");
                    }
                });
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
    }
}
