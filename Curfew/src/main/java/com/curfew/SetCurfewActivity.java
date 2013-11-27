package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SetCurfewActivity extends Activity {
    private TextView mSetCurfewTextView;
    private TextView mCurfewDateTextView;
    private TimePicker timePicker;
    private ParseUser mCurrentUser;
    private Button mSaveButton;
    private String TAG = "com.curfew.MainActivity";
    private Date curfewDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curfew);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");
        ParseAnalytics.trackAppOpened(getIntent());

        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser == null) {
            //TODO: go to signin screen
        }
        mSaveButton = (Button) findViewById(R.id.setCurfewButton);

        findViewById(R.id.setCurfewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSetCurfewTextView = (TextView) findViewById(R.id.editText);
                timePicker = (TimePicker) findViewById(R.id.timePicker);
                createCurfew();
            }
        });
       mCurfewDateTextView= (TextView)findViewById(R.id.curfew_date);
       Calendar cal = Calendar.getInstance();
       mCurfewDateTextView.setText(cal.toString());
    }

    public void createCurfew() {
        ParseQuery toUserQuery = ParseUser.getQuery();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        toUserQuery.whereEqualTo("username", mSetCurfewTextView.getText().toString().toLowerCase());
        mSaveButton.setEnabled(false);

        // This should only ever be one user
        toUserQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(final ParseObject toUser, ParseException e) {
                if (toUser == null) {
                    mSetCurfewTextView.setError("Invalid Username");
                    mSetCurfewTextView.requestFocus();
                    mSaveButton.setEnabled(true);
                } else if (toUser.getString("username").equals(currentUser.getString("username"))) {
                    mSetCurfewTextView.setError("Cannot be yourself");
                    mSetCurfewTextView.requestFocus();
                    mSaveButton.setEnabled(true);
                } else {
                    // The user 'toUser' exists, we can add the curfew now
                    // Check if there is already a curfew for toUser, and retrieve it if so
                    ParseQuery curfewQuery = ParseQuery.getQuery("Curfew");
                    curfewQuery.whereEqualTo("toUser", toUser);

                    curfewQuery.findInBackground(new FindCallback() {
                        @Override
                        public void done(List list, ParseException e) {
                            Log.d(TAG, "User " + mSetCurfewTextView.getText() + " found " + list.size() + " copies of the user");

                            ParseObject curfew;

                            if (list.size() == 0) {
                                curfew = new ParseObject("Curfew");
                                Log.i(TAG, "Adding new curfew");
                            } else {
                                Log.i(TAG, "Updating existing curfew");
                                // TODO: we should just be getting the first one rather than a list
                                curfew = (ParseObject) list.get(0);
                            }
                            // Update the new/existing curfew
                            curfew.put("fromUser", currentUser);
                            curfew.put("toUser", toUser);
                            int hour = timePicker.getCurrentHour();
                            int minute = timePicker.getCurrentMinute();
                            String dateTime = "" + hour + ":" + minute;
//                            Date dateTime = new Date();
//                            Calendar cal = Calendar.getInstance();
//                            cal.set(Calendar.)

                            curfew.put("Curfew", dateTime);
                            curfew.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        mSaveButton.setEnabled(true);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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