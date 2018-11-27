package com.example.s1636469.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class CoinSearcher extends AsyncTask<Location, Void, Void> {
    private List<Coin> coinsNearbyCurrentLocation;
    private Activity activity;
    private MapboxMap mapboxMap;
    private int time_since_last_coin;
    private TextView coin_combo_indicator;
    private String TAG = "CoinSearcher";
    private Thread runner;


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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();

        final DocumentReference docRef = database.collection("users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Location userLocation = location[0];
                Coin c;

                Map<String, Object> user_data = task.getResult().getData();
                ArrayList<String> collected = (ArrayList<String>) user_data.get("collected");
                ArrayList<String> nearbyIds = new ArrayList<String>();
                HashMap<String, Object> to_put = new HashMap<String, Object>();
                int prevSize = 0;
                boolean start_new_combo = false;

                Set<String> coin_ids = MapPoints.coins.keySet();
                for (String coin_id : coin_ids) {
                    c = MapPoints.coins.get(coin_id);
                    float dist = userLocation.distanceTo(c.getLocation());
                    if (dist < Config.distanceForCollection) {
                        Coin nearbyCoin = MapPoints.coins.get(coin_id);
                        Log.d(TAG,nearbyCoin.getId());
                        double val = nearbyCoin.getValue();
                        String cur = nearbyCoin.getCurrency();
                        double cur_val;
                        try {
                            cur_val = (Double) (user_data.get(cur));
                        } catch (ClassCastException e) {
                            cur_val = ((Long) user_data.get(cur)).doubleValue();
                        }
                        to_put.put(cur,cur_val+val);
                        prevSize = MapPoints.coins.size();
                        //add to collected list
                        nearbyIds.add(nearbyCoin.getId());
                    }
                }
                for (String id : nearbyIds) {
                    mapboxMap.removeMarker(MapPoints.markers.get(id).getMarker());
                    MapPoints.coins.remove(id);
                }
                collected.addAll(nearbyIds);
                to_put.put("collected",collected);
                Log.d(TAG, collected.toString());
                docRef.set(to_put, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
