package com.example.pokemongocontroller;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.SystemClock;

import java.util.Timer;


/**
 * Created by lisa on 15/07/16.
 */

public class FakeGPS {
    Context ctx;

    private boolean moving = false;

    private double lat = 0.0;
    private double lon = 0.0;

    private Handler handler;
    private Runnable timerTask;

    private static double step = 0.0001;
    private static int frequency = 200;

    public FakeGPS(Context ctx, LocationListener ll) {

        this.ctx = ctx;
        Timer timer = new Timer();

        init(LocationManager.NETWORK_PROVIDER, ctx, ll);
        init(LocationManager.GPS_PROVIDER, ctx, null);
        //init(LocationManager.PASSIVE_PROVIDER, ctx, null); // cannot mock that ;)

        handler = new Handler();
        timerTask = new Runnable() {
            @Override
            public void run() { updateLocation(); }
        };
        updateLocation();

    }

    private void updateLocation() {
        pushLocation(this.lat, this.lon);
        handler.postDelayed(timerTask, frequency);
    }
    private void init(String provider, Context ctx, LocationListener ll) {

        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        lm.addTestProvider(provider, false, false, false, false, false,
                true, true, 0, 5);
        if (ll != null) {
            lm.requestLocationUpdates(provider, 0, 0, ll);
        }
        lm.setTestProviderStatus(provider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
        lm.setTestProviderEnabled(provider, true);
    }

    public void walkN() { pushLocation(this.lat + step, this.lon); }

    public void walkS() { pushLocation(this.lat - step, this.lon); }

    public void walkW() { pushLocation(this.lat, this.lon - step); }

    public void walkE() { pushLocation(this.lat, this.lon + step); }

    public void walkNE() { pushLocation(this.lat + step / 2, this.lon + step / 2); }

    public void walkSE() { pushLocation(this.lat - step / 2, this.lon + step / 2);}

    public void walkNW() { pushLocation(this.lat + step / 2, this.lon - step / 2); }

    public void walkSW() { pushLocation(this.lat - step / 2, this.lon - step / 2); }

    public void pushLocation(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        _pushLocation(LocationManager.NETWORK_PROVIDER, lat, lon);
        _pushLocation(LocationManager.GPS_PROVIDER, lat, lon);
        // _pushLocation(LocationManager.PASSIVE_PROVIDER, lat, lon);
    }


    private void _pushLocation(String providerName, double lat, double lon) {
        LocationManager lm = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
        Location loc = new Location(providerName);
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        loc.setAltitude(0);
        loc.setTime(System.currentTimeMillis());
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        loc.setAccuracy(Criteria.ACCURACY_HIGH);
        lm.setTestProviderLocation(providerName, loc);
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        lm.removeTestProvider(LocationManager.GPS_PROVIDER);
        lm.removeTestProvider(LocationManager.NETWORK_PROVIDER);
        //lm.removeTestProvider(LocationManager.PASSIVE_PROVIDER);
    }

    public void moveTo(double lat, double lon) {
        moving = true;
        long ms;
        long current_time;
        long last_time = System.currentTimeMillis();

        while (moving) {
            current_time = System.currentTimeMillis();
            ms = current_time - last_time;

            double allowed_distance = (8 / 60.0) * ms / 1000 / 3600;
            double dx = (lat - this.lat);
            double dy = (lon - this.lon);

            double needed = Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)));
            dx = dx * allowed_distance / needed;
            dy = dy * allowed_distance / needed;

            pushLocation(this.lat + dx, this.lon + dy);
            last_time = current_time;
            if (needed < 0.00001) {
                moving = false;
            }
        }
    }

    public void stopMove() {
        moving = false;
    }
}
