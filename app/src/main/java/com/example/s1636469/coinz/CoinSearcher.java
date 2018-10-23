package com.example.s1636469.coinz;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewOutlineProvider;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

public class CoinSearcher extends AsyncTask<Location, Void, Void> {
    private List<Coin> coinsNearbyCurrentLocation;
    private Activity activity;
    private MapboxMap mapboxMap;
    public CoinSearcher(Activity activity,MapboxMap mapboxMap) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
    }
    protected Void doInBackground(Location... location) {
        Log.d("STATUS","Starting coin search");
        if (MapPoints.coins.size() == 0) {
            Log.d("STATUS", "Unable to get list of coins");
            // TODO: Send dialog
            return null;
        }
        Location userLocation = location[0];
        Coin c;
        for (int i = 0; i < MapPoints.coins.size(); i++) {
            c = MapPoints.coins.get(i);
            float dist = userLocation.distanceTo(c.getLocation()) ;
            if (dist < Config.distanceForCollection) {
                Log.d("STATUS","near");
                MapPoints.coins.get(i).setNearby(true);

            } else {
                MapPoints.coins.get(i).setNearby(false);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        MapPoints.addMapPoints(this.activity,mapboxMap);


    }
}
