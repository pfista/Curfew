package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by cameronrison on 11/12/13.
 */
public class MapViewActivity extends Activity {


    private GoogleMap mMap;
    private LatLng userLocation;

    @Override
    protected void onResume() {
        super.onResume();
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(status, this.getParent(), 0); // TODO: what should the last parameter be?
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the message from the intent
        Intent intent = getIntent();
        String usrname = intent.getStringExtra(UserActivity.USRNAME);
        //LatLng loc = intent.getExtras()

        setContentView(R.layout.activity_mapview);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

    }
}
