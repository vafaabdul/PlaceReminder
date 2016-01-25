package com.example.rohithengu.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ROHITHENGU on 5/4/2015.
 */
public class AppActivity extends Activity {

    Button addBtn;
    Button deleteBtn;
    ListView listView;
    ArrayList<String> arr;
    ArrayList<String> locations;
    List<String> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        arr = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.placesList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        locations = new ArrayList<String>();
        deleteBtn = (Button) findViewById(R.id.btnDelete);
        checkGPS();

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = listView.getCount();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                for (int i = 0; i < len; i++)
                    if (checked.get(i)) {
                        String place = list.get(i);
                        PrefUtils.removePlace(getApplicationContext(), place);


                    }
                populateExitingLocations();
            }
        });
        populateExitingLocations();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int itemPosition = position;
                String itemValue = (String) listView.getItemAtPosition(position);
            }
        });
        addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation(v);
            }
        });
    }


    private void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This Application requires GPS, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public void populateExitingLocations() {
        locations.clear();
        list = new ArrayList<String>();
        list = PrefUtils.getSavedPlaces(getApplicationContext());
        for (String item : list) {
            int pos = item.lastIndexOf("#");
            locations.add(item.substring(0, pos));
        }

        ArrayAdapter adapter = new ArrayAdapter(this,
                R.layout.list_item, locations);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateExitingLocations();

    }

    public void showLocation(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
