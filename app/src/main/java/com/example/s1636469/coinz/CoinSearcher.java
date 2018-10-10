package com.example.s1636469.coinz;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

public class CoinSearcher extends AsyncTask<Location, Void, List<Coin>> {
    private List<Coin> coinsNearbyCurrentLocation;
    private Activity activity;
    private MapboxMap mapboxMap;
    public CoinSearcher(Activity activity,MapboxMap mapboxMap) {
        this.activity = activity;
        this.mapboxMap = mapboxMap;
    }
    protected List<Coin> doInBackground(Location... location) {
        if (MapPoints.coins.size() == 0) {
            Log.d("STATUS", "Unable to get list of coins");
            // TODO: Send dialog
            return null;
        }
        Location userLocation = location[0];
        Coin c;
        ArrayList<Coin> nearbyCoins = new ArrayList<Coin>();
        for (int i = 0; i < MapPoints.coins.size(); i++) {
            c = MapPoints.coins.get(i);
            if (userLocation.distanceTo(c.getLocation()) < Config.distanceForCollection) {
                nearbyCoins.add(c);
                c.setNearby(true);
            }
        }
        return nearbyCoins;
    }

    @Override
    protected void onPostExecute(List<Coin> nearbyCoins) {
        super.onPostExecute(nearbyCoins);
        MapPoints.addMapPoints(this.activity,mapboxMap,nearbyCoins);
    }
}
