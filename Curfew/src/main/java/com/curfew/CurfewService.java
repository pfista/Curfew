package com.curfew;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Rohan on 11/2/13.
 */
public class CurfewService extends Service {
    String curTime;
    double lat;
    double lng;
    double alt;
    String TAG = "com.curfew.CurfewService";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        return 1;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager)getSystemService(context);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);

        //Initialize timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run(){
                ParseUser currentUser = ParseUser.getCurrentUser();
                ParseGeoPoint geoPoint = new ParseGeoPoint();
                geoPoint.setLatitude(lat);
                geoPoint.setLongitude(lng);
                currentUser.put("location", geoPoint);
                try {
                    currentUser.save();
                    Log.i(TAG, "Logging location with lat: " + geoPoint.getLatitude() + " and long: " + geoPoint.getLongitude());
                } catch (ParseException e) {
                    Log.i(TAG,"Error updating location: ", e);
                    e.printStackTrace();
                }
            }
        }, 5*60*1000, 1);


        updateWithNewLocation(null);

        locationManager.requestLocationUpdates(provider, (5*60*1000), 5,
                locationListener);
    }
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider){
            updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider){ }
        public void onStatusChanged(String provider, int status,
                                    Bundle extras){ }
    };
    public void updateWithNewLocation(Location location) {


        if (location != null) {
            long time = System.currentTimeMillis();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            curTime = df.format(time);
            lat = location.getLatitude();
            lng = location.getLongitude();
            alt = location.getAltitude();


        }

    }
}
