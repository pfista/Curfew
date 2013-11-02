package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class UserActivity extends Activity {

    private TextView mFriendName;
    private ParseUser mFriend;
    private ParseUser mCurrentUser;
    private ParseObject mFriendCurfew;
    private TextView mCurfewText;

    public final String TAG = "com.curfew.UserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendName = (TextView) findViewById(R.id.friend_username);
        mCurfewText = (TextView) findViewById(R.id.label_curfew_time);

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

        }
        return super.onOptionsItemSelected(item);
    }

}
