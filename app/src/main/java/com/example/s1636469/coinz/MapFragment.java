package com.example.s1636469.coinz;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class MapFragment extends Fragment implements LocationEngineListener {
    private static final String TAG = "MapFragment";

    private MapView mapView;
    private LocationLayerPlugin locationPlugin;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private Location originLocation;
    private View view;
    private Context context;
    private TextView coin_combo_indicator;

    //TODO: Fix permissions get crash
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Mapbox.getInstance(context, "pk.eyJ1IjoiZnJlZGRpZWpiYXdkZW4iLCJhIjoiY2ptb3NtZHhrMDAwazNwbDgzM2l4YjI1MSJ9.zCqzFmwVZoUGtTJgeZOMTw");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            Log.d("STATUS", "view is null");    locationEngine.setInterval(5000);

        } else {
            setUpListeners();
        }
        context = getActivity();
        FloatingActionButton fab = view.findViewById(R.id.gps_centre);
        fab.setImageResource(R.drawable.ic_gps_fixed);
        MapView mapView = view.findViewById(R.id.mapView);
        this.mapView = mapView;
        mapView.onCreate(savedInstanceState);
        getMap();

        if (originLocation == null) {
            Log.d("STATUS", "oLoc is null");
        }
    }

    public  MapboxMap getMapBoxMap() {
        return mapboxMap;
    }

    protected void setMapBox(MapboxMap map) {
        this.mapboxMap = map;
    }

    public void getMap() {

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(context, "GPS permission is needed!",Toast.LENGTH_SHORT).show();
            }
            // upon getting permission, fire request permissions function
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Config.REQUEST_GPS);
        }
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
                    Toast.makeText(getActivity(), "Oh no", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            // Create an instance of LOST location engine
            startLocationEngine();

            // TODO: "setCameraMode(CameraMode.Tracking)"
            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap,locationEngine);
            locationPlugin.setRenderMode(RenderMode.COMPASS);
            setCameraPosition(originLocation,true,false);

        } else {
            Toast.makeText(getActivity(), "Cannot access location, unable to aquire permissions!",Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    protected void startLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(context);
        this.locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setFastestInterval(100);
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            locationEngine.addLocationEngineListener(this);
        } else {
            Log.d("STATUS", "Location Engine has no \'lastLocation\' ");
            locationEngine.addLocationEngineListener(this);
        }
        locationEngine.activate();
        Log.d("STATUS","Location Engine Started");

    }


    private void setCameraPosition(Location location,boolean resetCamera,boolean animate) {
        double zoomLevel;
        if (location ==null) {
            Toast.makeText(context,"Could not find current location to centre on, please try again later.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (resetCamera) {
            zoomLevel = 15;
        } else {
            zoomLevel = this.mapboxMap.getCameraPosition().zoom;
        }
        Log.d("UI_UPDATE", "Moving camera");
        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
        //if (animate) {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    ltlng, zoomLevel), 1);
        //}
    }

    @SuppressWarnings( {"MissingPermission"})
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
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
        Log.d("STATUS","Location Engine Connected");
        locationEngine.requestLocationUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO: Find out why my phone does not update the location here
        Log.d("STATUS","location changed");
        if (location != null) {
            originLocation = location;
            setCameraPosition(location,false,true);
            CoinSearcher coinSearcher = new CoinSearcher(getActivity(), mapboxMap);
            if (MapPoints.coins.size() > 0) {
                coinSearcher.execute(originLocation);
            }
        } else {
          Log.d("STATUS","Location update is null");
        }
    }

    protected void setUpListeners() {
        FloatingActionButton gps = (FloatingActionButton) view.findViewById(R.id.gps_centre);

        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("UI_UPDATE", "GPS Centering button pressed");

                if (originLocation == null) {
                    try {
                        locationPlugin.forceLocationUpdate(locationPlugin.getLastKnownLocation());
                    } catch (SecurityException e) {
                        Log.d("Status","Cannot get permission to find location");
                    }
                    Toast.makeText(getActivity(), "Cannot find current location to center on",Toast.LENGTH_LONG).show();
                } else {
                    setCameraPosition(originLocation,true, true);
                }
            }
        });
    }
    private void getMarkersFromServer() {
        GeoJSONGetter getter = new GeoJSONGetter(getActivity(),mapboxMap,originLocation,coin_combo_indicator);
        String url = String.format("http://homepages.inf.ed.ac.uk/stg/coinz/%s/coinzmap.geojson",Config.getGeoJSONURL());
        getter.execute(url);
    }
    private void plotGeoJSON() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        //TODO: Sub in user name from here
        DocumentReference dRef = firestore.collection("users").document("initial");
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String,Object> data = task.getResult().getData();
                Date last_login = (Date) data.get("last_login");
                Calendar last_login_cal = Calendar. getInstance();
                last_login_cal.setTime(last_login);
                // Check to purge

                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                if(!fmt.format(Calendar.getInstance().getTime()).equals(fmt.format(last_login))) {
                    // purge
                    HashMap<String, Object> toUpdate = new HashMap<String, Object>();
                    toUpdate.put("collected", new ArrayList<String>());
                    toUpdate.put("last_login",Calendar.getInstance().getTime());
                    dRef.set(toUpdate, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("MARKERS","Day has changed since last login");
                            getMarkersFromServer();
                        }
                    });
                } else {
                    Log.d("MARKERS","Day has not changed since last login");
                    getMarkersFromServer();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("STATUS","Failed",e);

            }
        });
    }

}
