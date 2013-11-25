package com.curfew;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;

/**
 * Created by Rohan on 11/2/13.
 */
public class CurfewService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    public final String TAG = "com.curfew.CurfewService";
    private LocationClient mLocClient;
    private LocationRequest mLocRequest;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 120;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;


    /* Service extension start */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        // TODO: Should this be null?
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");

        mLocRequest = LocationRequest.create();
        mLocRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocRequest.setInterval(UPDATE_INTERVAL);
        mLocRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocClient = new LocationClient(this, this, this);
        mLocClient.connect();
    }

    @Override
    public void onDestroy() {
        mLocClient.removeLocationUpdates(this);
        mLocClient.disconnect();
    }

    public void updateWithNewLocation(Location location) {
        if (location != null) {
            long time = System.currentTimeMillis();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String curTime = df.format(time);
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            ParseUser currentUser = ParseUser.getCurrentUser();
            ParseGeoPoint geoPoint = new ParseGeoPoint();
            geoPoint.setLatitude(lat);
            geoPoint.setLongitude(lng);
            currentUser.put("location", geoPoint);

            try {
                currentUser.save();
                Log.i(TAG, "Logging location with lat: " + geoPoint.getLatitude() + " and long: " + geoPoint.getLongitude());
            } catch (ParseException e) {
                Log.i(TAG, "Error updating location: ", e);
                e.printStackTrace();
            }
        }

    }
    /* End Service extension */

    /*
      Google play services implementations
      */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to GPServices");
        mLocClient.requestLocationUpdates(mLocRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected to GPServices");
        mLocClient.removeLocationUpdates(this);
        mLocClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO: showErrorDialog(connectionResult.getErrorCode());
        Log.e(TAG, "error connecting to services");

    }


    /* End google play services implementations */

    /* Location listener implementation */
    @Override
    public void onLocationChanged(Location location) {
        // TODO: may need to distinguish between GPS and Network providers here for better accuracy
        Log.i(TAG, "received Location change, sending new location");
        updateWithNewLocation(location);
    }
    /* End location listener implementation */


}
