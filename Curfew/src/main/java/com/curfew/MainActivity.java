package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;
    private String TAG = "com.curfew.MainActivity";
    private ParseUser mCurrentUser;

    protected ArrayList<String> mToUserList;
    protected ListView mCurfewListView;
    protected ArrayAdapter mCurfewAdapter;


    private ParseQueryAdapter<ParseObject> mCurfewAdapterP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, SensitiveKeys.PARSE_APPLICATION_ID, SensitiveKeys.PARSE_CLIENT_KEY);
        ParseAnalytics.trackAppOpened(getIntent());

        mCurrentUser = ParseUser.getCurrentUser();
        mUserNameTextView = (TextView) findViewById(R.id.username);
        mCurfewListView = (ListView) findViewById(R.id.curfewListView);
        mToUserList = new ArrayList<String>();
        mCurfewAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.curfewtextview, mToUserList);

        mCurfewAdapterP = new ParseQueryAdapter<ParseObject>(this, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            @Override
            public ParseQuery<ParseObject> create() {
                ParseQuery query = new ParseQuery("User");
                query.whereEqualTo("fromUser", mCurrentUser);
                query.orderByAscending("toUser");
                return query;
            }
        });
        // TODO: These must somehow get at the User object...
        mCurfewAdapterP.setTextKey("toUser");
        mCurfewAdapterP.setImageKey("Image");
        mCurfewAdapterP.setPlaceholder(getResources().getDrawable(R.drawable.placeholder));


        // TODO: GUI loading stuff. See parse parsequeryadapter docs
        mCurfewListView.setAdapter(mCurfewAdapterP);

        mCurfewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("username", item);
                startActivity(intent);
            }
        });

        //Starting the service
        startService(new Intent(this, CurfewService.class));
        // TODO: Provide a way to stop the service

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentUser != null) {
            mUserNameTextView.setText(mCurrentUser.getUsername());
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