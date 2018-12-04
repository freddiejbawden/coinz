/*
 *  CoinSearch
 *
 *  Searches for coins surrounding the user
 *
 */

package com.example.s1636469.coinz;

import android.app.Activity;
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
import com.mapbox.mapboxsdk.maps.MapboxMap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CoinSearcher extends AsyncTask<Location, Void, Void> {

    private Activity activity;
    private MapboxMap mapboxMap;
    private String TAG = "CoinSearcher";


    public CoinSearcher(Activity activity,MapboxMap mapboxMap) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
    }


    /*
     *  doInBackground
     *
     *  implemented class as part of AsyncTask, searches for coins within a radius defined
     *  in the Config file, then updates the Firestore with the collected coins
     */
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

        // Get a Firestore database
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();

        // Get a reference to the user's document
        final DocumentReference docRef = database.collection("users").document(id);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Location userLocation = location[0];
                Coin c;

                Map<String, Object> user_data = task.getResult().getData();
                ArrayList<String> collected = (ArrayList<String>) user_data.get("collected");
                ArrayList<String> nearbyIds = new ArrayList<>();
                HashMap<String, Object> to_put = new HashMap<>();
                HashMap<String, Double> coin_count = new HashMap<String, Double>() {{
                    put("PENY",0.0);
                    put("DOLR",0.0);
                    put("QUID",0.0);
                    put("SHIL",0.0);
                }};

                Set<String> coin_ids = MapPoints.coins.keySet();

                // Search all coins in area
                for (String coin_id : coin_ids) {
                    c = MapPoints.coins.get(coin_id);
                    float dist = userLocation.distanceTo(c.getLocation());

                    // If coins are in the collection radius
                    if (dist < Config.distanceForCollection) {
                        Coin nearbyCoin = MapPoints.coins.get(coin_id);
                        Log.d(TAG,nearbyCoin.getId());
                        double val = nearbyCoin.getValue();
                        String cur = nearbyCoin.getCurrency();
                        coin_count.put(cur, coin_count.get(cur) + val);
                        double cur_val;
                        try {
                            cur_val = (Double) (user_data.get(cur));
                        } catch (ClassCastException e) {
                            cur_val = ((Long) user_data.get(cur)).doubleValue();
                        }

                        // Add the value of the coin to the user's document
                        to_put.put(cur,cur_val+val);
                        nearbyIds.add(nearbyCoin.getId());
                    }
                }

                // Remove marker from map
                for (String id : nearbyIds) {
                    mapboxMap.removeMarker(MapPoints.markers.get(id).getMarker());
                    MapPoints.coins.remove(id);
                }
                if (!nearbyIds.isEmpty()) {
                    String to_display = "Coins collected:";
                    for (String cur : Config.currencies) {
                        if (coin_count.get(cur) > 0) {
                            Log.d(TAG, cur);
                            String amount_string = Config.round(coin_count.get(cur),Config.CUR_VALUE_DP) + "";
                            to_display = to_display + "\n" + cur + ": " + amount_string;
                        }
                    }
                    Toast.makeText(activity,to_display, Toast.LENGTH_LONG).show();
                }
                collected.addAll(nearbyIds);
                to_put.put("collected",collected);
                Log.d(TAG, collected.toString());

                //  Update the document
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

}
