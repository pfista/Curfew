package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;
    private String TAG = "com.curfew.MainActivity";
    public ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs", "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI");
        ParseAnalytics.trackAppOpened(getIntent());

        mCurrentUser = ParseUser.getCurrentUser();
        mUserNameTextView = (TextView) findViewById(R.id.username);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentUser != null) {
            mUserNameTextView.setText(mCurrentUser.getUsername());

            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Curfew");
            parseQuery.whereEqualTo("fromUser", mCurrentUser);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> curfews, ParseException e) {
                    if (e == null) {
                        ArrayList<String> toUsersList = new ArrayList<String>();

                        for (ParseObject parseObject : curfews) {
                            ParseUser user = (ParseUser) parseObject.get("toUser");
                            try {
                                // TODO: Consider fetchIfNeededInBackground
                                toUsersList.add(user.fetchIfNeeded().getString("username"));
                            } catch (ParseException e1) {
                                Log.e(TAG, e1.getMessage());
                            }
                        }
                        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, toUsersList);
                        ListView listView = (ListView) findViewById(R.id.curfewListView);
                        listView.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Parse error " + e.getMessage());
                    }
                }
            });
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
            case R.id.action_add_curfew:
                Intent intent2 = new Intent(this, SetCurfewActivity.class);
                startActivity(intent2);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
