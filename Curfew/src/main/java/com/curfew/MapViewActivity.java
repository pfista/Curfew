package com.curfew;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Created by cameronrison on 11/12/13.
 */
public class MapViewActivity extends FragmentActivity {


    private GoogleMap mMap;
    private LatLng userLocation;

//    @Override
//    protected void onResume() {
//        super.onResume();
//        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (status != ConnectionResult.SUCCESS) {
//            GooglePlayServicesUtil.getErrorDialog(status, this.getParent(), 0); // TODO: what should the last parameter be?
//        }
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Get the message from the intent
        Intent mapActivity = getIntent();
        Bundle b = mapActivity.getExtras();
        String usrname = b.getString("USRNAME");
        final LatLng loc = (LatLng) b.get("LAT_LONG");

        setContentView(R.layout.activity_mapview);


        MarkerOptions opts = new MarkerOptions();
        opts.position(loc);
        opts.title(usrname);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        mMap.addMarker(opts);


    }

}
