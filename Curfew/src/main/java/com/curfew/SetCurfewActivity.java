package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class SetCurfewActivity extends Activity {
    private TextView mSetCurfewTextView;
    private TimePicker timePicker;
    private ParseUser mCurrentUser;
    private String TAG = "com.curfew.MainActivity";

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
        findViewById(R.id.setCurfewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSetCurfewTextView = (TextView) findViewById(R.id.editText);
                timePicker = (TimePicker) findViewById(R.id.timePicker);
                createCurfew();
            }
        });
    }

    public void createCurfew() {
        ParseQuery parseQuery = ParseUser.getQuery();
        ParseUser currentUser = ParseUser.getCurrentUser();
        parseQuery.whereEqualTo("username", mSetCurfewTextView.getText());

        try {
            List<ParseUser> userList = parseQuery.find();
            if (userList.size() != 1) {
                Log.d(TAG, "User " + mSetCurfewTextView.getText() + " found " + userList.size() + " copies of the user");
            } else {
                ParseObject newCurfew = new ParseObject("Curfew");
                newCurfew.put("fromUser", currentUser);
                newCurfew.put("toUser", userList.get(0));
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String dateTime = "" + hour + ":" + minute;
                newCurfew.put("Curfew", dateTime);
                newCurfew.save();
            }
        } catch (ParseException e) {
            Log.d(TAG, "ParseException thrown when finding user: ", e);
        }

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