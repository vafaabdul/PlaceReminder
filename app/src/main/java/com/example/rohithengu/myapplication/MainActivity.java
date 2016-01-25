package com.example.rohithengu.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.Geofence;


import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private GoogleMap map;
    private Marker marker;
    GPSTracker gps;
    private Button mAddGeofencesButton;
    private EditText nameText;
    private int radius = 100;
    protected static final String TAG = "creating-and-monitoring-geofences";
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;
    private LocationManager locationManager;
    static Context mContext;
    Location location; // location
    LatLng latlong;
    boolean canGetLocation = false;
    Spinner mySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySpinner = (Spinner) findViewById(R.id.radiusMeter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.radiusArray, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);
        latlong = new LatLng(0, 0);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                latlong = latLng;
                marker.remove();
                marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Now You are Here !!"));
            }
        });
        mAddGeofencesButton = (Button) findViewById(R.id.saveBtn);
        nameText = (EditText) findViewById(R.id.locationNameTextBox);
        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;
        buildGoogleApiClient();
        setCurrentLocation();
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void setCurrentLocation() {
        gps = new GPSTracker(MainActivity.this);
        if (gps.canGetLocation()) {
            LatLng currLoc = new LatLng(gps.getLatitude(), gps.getLongitude());
            latlong = currLoc;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 5));
            marker = map.addMarker(new MarkerOptions()
                    .position(latlong)
                    .title("Now You are Here !!"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (location != null) {
            /*latitude=location.getLatitude();
            longitude=location.getLongitude();*/
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {

        //Log.i(TAG, "Connection suspended");
    }

    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    public void populateGeofenceList() {

        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%Y-%m-%d %T");
        System.out.println("Dossra Rad:"+radius);
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(nameText.getText().toString() + "#" + sTime)
                .setCircularRegion(
                        latlong.latitude,
                        latlong.longitude,
                        radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
        PrefUtils.savePlace(this, nameText.getText().toString() + "#" + sTime);
        Toast.makeText(this, "Location : " + nameText.getText().toString() + " is Saved", Toast.LENGTH_SHORT).show();
    }

    public void addGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (nameText.getText().toString() != "") {
                radius = Integer.valueOf(mySpinner.getSelectedItem().toString());
                System.out.println(radius);
                populateGeofenceList();
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this);
            } else {
                Toast.makeText(this, getString(R.string.name_Val_Error), Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {

    }

    public void onResult(Status status) {
        if (status.isSuccess()) {

        } else {
        }
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFencingService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}