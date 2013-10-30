package com.curfew;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");
        ParseAnalytics.trackAppOpened(getIntent());


        //Get username textview
        mUserNameTextView = (TextView) findViewById(R.id.userNameTextView);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null)
            mUserNameTextView.setText(currentUser.getUsername());
        else {
            //do nothing for now
        }

//        currentUser.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
