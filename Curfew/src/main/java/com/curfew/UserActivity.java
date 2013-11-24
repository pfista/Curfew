package com.curfew;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;

public class UserActivity extends Activity {

    private TextView mFriendName;
    private ParseUser mFriend;
    private ParseUser mCurrentUser;
    private ParseObject mFriendCurfew;
    private TextView mCurfewText;
    private Button mViewMapButton;
    private ImageView mUserPicture;
    private boolean pastCurfew;

    public final String TAG = "com.curfew.UserActivity";
    public static final String LAT_LONG = "com.curfew.Lat_Long";
    public static final String USRNAME = "com.curfew.USR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendName = (TextView) findViewById(R.id.friend_username);
        mCurfewText = (TextView) findViewById(R.id.label_curfew_time);
        mViewMapButton = (Button) findViewById(R.id.button_view_location);
        mUserPicture = (ImageView) findViewById(R.id.profilePicture);
        mViewMapButton.setEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            mFriendName.setText(intent.getStringExtra("username"));
        }

        // Get the friend ParseUser object from the database
        ParseQuery friendQuery = ParseUser.getQuery();
        friendQuery.whereEqualTo("username", mFriendName.getText());
        friendQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    mFriend = (ParseUser) parseObject;

                    // Get the profile picture
                    ParseFile file = (ParseFile) mFriend.get("profilePicture");
                    try {
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (data != null)
                                    mUserPicture.setImageBitmap(bmp);

                            }
                        });
                    } catch (Exception a) {
                        Log.d(TAG, a.toString());
                    }


                    // Get the curfew object
                    ParseQuery curfewQuery = ParseQuery.getQuery("Curfew");
                    curfewQuery.whereEqualTo("toUser", mFriend);
                    curfewQuery.whereEqualTo("fromUser", mCurrentUser);
                    curfewQuery.getFirstInBackground(new GetCallback() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null && parseObject != null) {
                                mFriendCurfew = parseObject;
                                Log.i(TAG, "curfew: " + mFriendCurfew.getString("Curfew"));
                                mCurfewText.setText("Active Curfew: "
                                        + mFriendCurfew.get("Curfew"));
                                String curfewTime = (String) mFriendCurfew.get("Curfew");
                                // TODO: makes sure curfew time is valid to view location
                                int hour = Integer.parseInt(curfewTime.split(":")[0]);
                                int minute = Integer.parseInt(curfewTime.split(":")[1]);
                                Time curfewTimeObject = new Time();
                                Calendar c = Calendar.getInstance();
                                curfewTimeObject.set(0, minute, hour, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                                Time currTime = new Time();
                                currTime.setToNow();
                                if (Time.compare(curfewTimeObject, currTime) < 0) {
                                    //Then it is past the curfew and show the location
                                    pastCurfew = true;
                                } else {
                                    pastCurfew = false;
                                }
                                mViewMapButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ParseGeoPoint geopoint = mFriend.getParseGeoPoint("location");
                                        if (pastCurfew && geopoint != null) {
                                            Intent intentNormal = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("geo:0,0?q=" +
                                                            geopoint.getLatitude() + "," +
                                                            geopoint.getLongitude() +
                                                            "(" + mFriendName.getText() + ")"));
                                            try {
                                                startActivity(intentNormal);
                                            } catch (ActivityNotFoundException e) {
//
                                                Intent intentAuxillary = new Intent(UserActivity.this, MapViewActivity.class);
                                                LatLng loc = new LatLng(geopoint.getLatitude(), geopoint.getLongitude());
                                                String usrname = (String) mFriendName.getText();
                                                intentAuxillary.putExtra("LAT_LONG", loc);
                                                intentAuxillary.putExtra("USRNAME", usrname);
                                                startActivity(intentAuxillary);


                                            }
                                        } else {
                                            //It is not past the curfew so show a toast saying location is not available before curfew
                                            Toast toast = Toast.makeText(getApplicationContext(), "Location not available before curfew", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }

                                    }
                                });

                                mViewMapButton.setEnabled(true);

                            } else {
                                Log.e(TAG, "Unable to find curfew" + e.getMessage());
                                // TODO: error cannot find friend
                            }
                        }
                    });
                } else {

                    Log.e(TAG, "Unable to find friend" + e.getMessage());
                    // TODO: error
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                // Destroy this activity
                ParseUser.logOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
