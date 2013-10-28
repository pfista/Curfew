package com.curfew;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");
        ParseAnalytics.trackAppOpened(getIntent());

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

        //Get username textview
        mUserNameTextView = (TextView) findViewById(R.id.userNameTextView);
//        mUserNameTextView
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
