/*
 *  MapFragment
 *
 *  Displays the map for the user and tracks location
 *
 */
package com.example.s1636469.coinz;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;


public class MapFragment extends Fragment implements LocationEngineListener {

    private MapView mapView;
    private LocationLayerPlugin locationPlugin;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private Location originLocation;
    private View view;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get instance with key
        Mapbox.getInstance(context, Config.mapbox_key);
    }

    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();
        // set up references to UI
        FloatingActionButton fab = view.findViewById(R.id.gps_centre);

        // Get the mapview from layout
        MapView mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        this.mapView = mapView;

        // Set GPS centering icon
        fab.setImageResource(R.drawable.ic_gps_fixed);

        setUpListeners();
        getMap();

    }


    protected void setMapBox(MapboxMap map) {
        this.mapboxMap = map;
    }

    private void mapASyncCallBack() {
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

    /*
     *  getMap
     *
     *  Sets up the mapbox map
     */
    public void getMap() {

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If permission was granted define a callback function and fetch map
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapASyncCallBack();
                }
            });
        } else {
            // If permission was not given then get permission
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(context, "GPS permission is needed!",Toast.LENGTH_SHORT).show();
            }
            // upon getting permission, fire request permissions function
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Config.REQUEST_GPS);
        }
    }

    /*
     *  onRequestPermissionResult
     *
     *  handles permissions call back
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull  int[] grantResults) {
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
                            mapASyncCallBack();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Cannot access location!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /*
     *  enableLocationPlugin
     *
     *  set up location plug in to track user location
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            // Create an instance of location engine
            startLocationEngine();

            // Configure plug in
            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap,locationEngine);
            locationPlugin.setRenderMode(RenderMode.COMPASS);
            locationPlugin.setCameraMode(CameraMode.TRACKING);
            setCameraPosition(originLocation,true);

        } else {
            Toast.makeText(getActivity(), "Cannot access location, unable to aquire permissions!",Toast.LENGTH_LONG).show();
        }
    }

    /*
     *  startLocationEngine
     *
     *  uses Mapbox locationengine to find the best location provider
     */
    @SuppressWarnings( {"MissingPermission"})
    protected void startLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(context);

        // Get the best provider of location
        this.locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);

        // Manually set interval
        locationEngine.setFastestInterval(100);

        // set position of user
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
        }
        locationEngine.addLocationEngineListener(this);

        // activate the location engine
        locationEngine.activate();
        Log.d("STATUS","Location Engine Started");

    }

    /*
     *  setCameraPosition
     *
     *  move the camera to the chosen location
     */
    private void setCameraPosition(Location location,boolean resetCamera) {
        double zoomLevel;

        if (location ==null) {
            Toast.makeText(context,"Could not find current location to centre on, please try again later.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if zoom level is being reset
        if (resetCamera) {
            zoomLevel = 15;
        } else {
            zoomLevel = this.mapboxMap.getCameraPosition().zoom;
        }


        Log.d("UI_UPDATE", "Moving camera");


        // Get location and move camera
        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ltlng, zoomLevel), 1);
    }


    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onStart() {
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
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
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
    public void onSaveInstanceState(@NonNull  Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public void onConnected() {
        Log.d("STATUS","Location Engine Connected");
        locationEngine.requestLocationUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
        // If the location is changed, then start the coin seracher
        Log.d("STATUS","location changed");
        if (location != null) {
            originLocation = location;
            setCameraPosition(location,false);
            CoinSearcher coinSearcher = new CoinSearcher(getActivity(), mapboxMap);
            if (MapPoints.coins.size() > 0) {
                coinSearcher.execute(originLocation);
            }
        } else {
          Log.d("STATUS","Location update is null");
        }
    }

    /*
     *  setUpListeners
     *
     *  set up GPS functionality
     */
    private void setUpListeners() {
        FloatingActionButton gps =  view.findViewById(R.id.gps_centre);

        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("UI_UPDATE", "GPS Centering button pressed");

                if (originLocation == null) {
                    try {
                        // Force the location engine to get the location
                        locationPlugin.forceLocationUpdate(locationPlugin.getLastKnownLocation());
                    } catch (SecurityException e) {
                        Log.d("Status","Cannot get permission to find location");
                    }
                    Toast.makeText(getActivity(), "Cannot find current location to center on",Toast.LENGTH_LONG).show();
                } else {
                    setCameraPosition(originLocation,true);
                }
            }
        });
    }
    /*
     *  plotGeoJSON
     *
     *  start the download of the GeoJSON coin file
     */
    private void plotGeoJSON() {
        GeoJSONGetter getter = new GeoJSONGetter(getActivity(),mapboxMap,originLocation);
        String url = String.format("http://homepages.inf.ed.ac.uk/stg/coinz/%s/coinzmap.geojson",Config.getGeoJSONURL());
        getter.execute(url);
    }

}
