package com.curfew;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class SetCurfewActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private TextView mSetCurfewTextView;
    private TextView mCurfewDateTextView;
    private TextView mTimeDisplayTextView;
    private AnalogClock mAnalogClock;
    private ParseUser mCurrentUser;
    private String TAG = "com.curfew.SetCurfewActivity";
    private Calendar curfewDate;
    private ImageView mImageView;
    private ClockDrawable cd;

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

        Bundle bundle = getIntent().getExtras();


        mTimeDisplayTextView = (TextView) findViewById(R.id.time_display);

        mTimeDisplayTextView.setText("12:00");
        mSetCurfewTextView = (TextView) findViewById(R.id.editText);
        mImageView = (ImageView) findViewById(R.id.curfew_clock);
        cd = new ClockDrawable(100, R.color.black);
        cd.setTime(0,0,0);
        mImageView.setBackground(cd);

        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, 12, 0, isVibrate());
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show(getFragmentManager(), "timepicker");
            }
        });


        mCurfewDateTextView= (TextView)findViewById(R.id.curfew_date);
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        mCurfewDateTextView.setText(df.format(cal.getTime()));
        curfewDate = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, curfewDate.get(Calendar.YEAR), curfewDate.get(Calendar.MONTH), curfewDate.get(Calendar.DAY_OF_MONTH), isVibrate());

        mCurfewDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(2013, 2028);
                datePickerDialog.show(getFragmentManager(), "datepicker");
            }
        });

        if (bundle != null && bundle.getString("username") != null){
            mSetCurfewTextView.setText(bundle.getString("username"));
            mSetCurfewTextView.setEnabled(false);
        }


    }

    public void createCurfew() {
        ParseQuery toUserQuery = ParseUser.getQuery();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        toUserQuery.whereEqualTo("username", mSetCurfewTextView.getText().toString().toLowerCase());

        // This should only ever be one user
        toUserQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(final ParseObject toUser, ParseException e) {
                if (toUser == null) {
                    mSetCurfewTextView.setError("Invalid Username");
                    mSetCurfewTextView.requestFocus();

                } else if (toUser.getString("username").equals(currentUser.getString("username"))) {
                    mSetCurfewTextView.setError("Cannot be yourself");
                    mSetCurfewTextView.requestFocus();

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

                            curfew.put("Curfew", curfewDate.getTime());
                            curfew.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {

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
        getMenuInflater().inflate(R.menu.set_curfew, menu);
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
                stopService(new Intent(this, CurfewService.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        curfewDate.set(year, month, day);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        mCurfewDateTextView.setText(df.format(curfewDate.getTime()));
        createCurfew();
        Toast.makeText(SetCurfewActivity.this, "Date saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {

        curfewDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        curfewDate.set(Calendar.MINUTE, minute);
        createCurfew();
        DateFormat df = new SimpleDateFormat("hh:mm a");
        cd.setTime(curfewDate.get(Calendar.HOUR), minute, 0);
        mImageView.setBackground(cd);
        mTimeDisplayTextView.setText(df.format(curfewDate.getTime()));
        Toast.makeText(SetCurfewActivity.this, "Curfew saved: " + df.format(curfewDate.getTime()), Toast.LENGTH_SHORT).show();

    }
    private boolean isVibrate() {
        return false;
    }
}