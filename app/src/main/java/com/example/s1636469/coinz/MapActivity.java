package com.example.s1636469.coinz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.Map;

public class MapActivity extends AppCompatActivity implements LocationEngineListener{


    private MapView mapView;
    private LocationLayerPlugin locationPlugin;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private Location originLocation;


    public  MapboxMap getMapBoxMap() {
        return mapboxMap;
    }

    protected void setMapBox(MapboxMap map) {
        this.mapboxMap = map;
    }
    public void getMap() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If it was granted define a callback funciton and fetch map
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    setMapBox(mapboxMap);
                    enableLocationPlugin();
                    Log.d("STATUS","location loaded");
                    try {
                        plotGeoJSON();
                    } catch (Exception e) {
                        Log.d("STATUS", "Failed to plot geojson");
                        Log.e("ERROR", e.toString());
                    }
                }
            });
        } else {
            // If permission was not given then get permission
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "GPS permission is needed!",Toast.LENGTH_SHORT).show();
            }
            // upon getting permission, fire request permissions function
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Config.REQUEST_GPS);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Get Map from MAPBOX using key
        // TODO: Store PublicKey in safe location
        // Start and instance of Mapbox
        setUpListeners();
        FloatingActionButton fab = findViewById(R.id.gps_centre);
        fab.setImageResource(R.drawable.ic_gps_fixed);
        Mapbox.getInstance(this, "pk.eyJ1IjoiZnJlZGRpZWpiYXdkZW4iLCJhIjoiY2ptb3NtZHhrMDAwazNwbDgzM2l4YjI1MSJ9.zCqzFmwVZoUGtTJgeZOMTw");
        MapView mapView = findViewById(R.id.mapView);
        this.mapView = mapView;
        mapView.onCreate(savedInstanceState);
        getMap();
        // Set map to the last instance that was saved

        // check if permission is given for location access

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // which service has been allowed
        switch(requestCode) {
            case Config.REQUEST_GPS: {
                // if the permission has been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // get map
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            // start location puck service
                            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap);
                            // choose how the puck will be rendered
                            locationPlugin.setRenderMode(RenderMode.COMPASS);
                            // I have no idea what this does
                            getLifecycle().addObserver(locationPlugin);
                            // pass out the MapBoxMap Object
                            setMapBox(mapboxMap);
                            enableLocationPlugin();

                        }
                    });
                } else {
                    // oh no
                }
                return;
            }
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            startLocationEngine();
            Log.d("STATUS","Location Engine Started");
            // TODO: "setCameraMode(CameraMode.Tracking)"
            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap,locationEngine);
            locationPlugin.setRenderMode(RenderMode.COMPASS);
            setCameraPosition(originLocation,true);

        } else {

        }
    }

    @SuppressWarnings( {"MissingPermission"})
    protected void startLocationEngine() {
        LocationEngineProvider  locationEngineProvider = new LocationEngineProvider(this);
        this.locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            System.out.println(lastLocation);
            originLocation = lastLocation;
            locationEngine.addLocationEngineListener(this);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
        locationEngine.activate();

    }


    private void setCameraPosition(Location location,boolean resetCamera) {
        double zoomLevel;
        if (resetCamera) {
            zoomLevel = 15;
        } else {
            zoomLevel = this.mapboxMap.getCameraPosition().zoom;
        }
        Log.d("UI_UPDATE", "Moving camera");
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel),1000);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    protected void onStart() {
        super.onStart();

        // Location engine start up
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @SuppressWarnings( {"MissingPermission"})

    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location,false);
            CoinSearcher coinSearcher = new CoinSearcher(this, mapboxMap);
            if (MapPoints.coins.size() > 0) {
                coinSearcher.execute(originLocation);
            }
        }
    }

    protected void setUpListeners() {
        FloatingActionButton gps = (FloatingActionButton) this.findViewById(R.id.gps_centre);

        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("UI_UPDATE", "GPS Centering button pressed");
                setCameraPosition(originLocation,true);
            }
        });
        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                      switch(menuItem.getItemId()) {
                          case (R.id.action_map):
                              Log.d("UI UPDATE", "map pressed");
                              startActivity(new Intent(getApplicationContext(), MapActivity.class));
                              break;
                          case (R.id.action_bank):
                              Log.d("UI UPDATE", "bank pressed");
                              startActivity(new Intent(getApplicationContext(), BankActivity.class));
                              break;
                          case (R.id.action_community):
                              Log.d("UI UPDATE", "community pressed");
                              startActivity(new Intent(getApplicationContext(), CommunityActivity.class));
                              break;
                          case (R.id.action_profile):
                              Log.d("UI UPDATE", "profile pressed");
                              startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                              break;
                      }
                      return false;
                    }
                }
        );
    }

    private void plotGeoJSON() {
        GeoJSONGetter getter = new GeoJSONGetter(this,mapboxMap,this.originLocation);
        getter.execute("http://homepages.inf.ed.ac.uk/stg/coinz/2018/06/05/coinzmap.geojson");


    }
}
