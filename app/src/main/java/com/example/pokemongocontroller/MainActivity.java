package com.example.pokemongocontroller;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private Button stop;
    private Button start;
    private Button set;

    private SimpleWebServer Server = null;

    private TextView longitude;
    private TextView latitude;

    private TextView gps_lat;
    private TextView gps_long;

    private TextView net_long;
    private TextView net_lat;


    private FakeGPS fakegps = null;
    private LocationManager lm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testPerms();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        testPerms();
    }

    private void testPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == 0 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            initGPS();
            initButtons();
        } else {
            askForPermissions();
        }
    }
    private void askForPermissions() {

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                }, 1);
    }

    private void initGPS() {

        gps_lat = (TextView) findViewById(R.id.real_lat);
        gps_long = (TextView) findViewById(R.id.real_long);

        net_lat = (TextView) findViewById(R.id.net_lat);
        net_long = (TextView) findViewById(R.id.net_long);

        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getLastLoc();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        fakegps = new FakeGPS(this, ll);
    }

    private void initButtons() {
        start = (Button) findViewById(R.id.button_start);
        stop = (Button) findViewById(R.id.button_stop);
        set = (Button) findViewById(R.id.button_set);

        View.OnClickListener oclStart = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Server == null) {
                    Toast.makeText(getApplicationContext(), "Server starting on port 8080", Toast.LENGTH_SHORT).show();
                    Server = new SimpleWebServer(8080, fakegps);
                    Server.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Server already started on port 8080", Toast.LENGTH_SHORT).show();
                }
            }
        };

        View.OnClickListener oclStop = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Server stopping", Toast.LENGTH_SHORT).show();
                if (Server != null) {
                    Server.stop();
                    Server = null;
                }
            }
        };

        View.OnClickListener olcSet = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = 0.0;
                double lon = 0.0;

                try {
                    lat = Double.parseDouble(((EditText) findViewById(R.id.field_lat)).getText().toString());
                    lon = Double.parseDouble(((EditText) findViewById(R.id.field_long)).getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Bad coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (lat > 90 || lon > 180 || lat < -90 || lon < -180) {
                    Toast.makeText(getApplicationContext(), "Not real coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }
                fakegps.pushLocation(lat, lon);
            }
        };

        start.setOnClickListener(oclStart);
        stop.setOnClickListener(oclStop);
        set.setOnClickListener(olcSet);

    }

    private void getLastLoc() {
        Location locationGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        gps_lat.setText(Double.toString(locationGPS.getLatitude()));
        gps_long.setText(Double.toString(locationGPS.getLongitude()));

        net_lat.setText(Double.toString(locationNet.getLatitude()));
        net_long.setText(Double.toString(locationNet.getLongitude()));
    }

    public void onDestroy() {
        super.onDestroy();
        if (fakegps != null) {
            fakegps.shutdown();
        }
        if (Server != null) {
            Server.stop();
        }
    }
}
