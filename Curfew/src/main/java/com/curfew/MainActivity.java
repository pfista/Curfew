package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;
    private String TAG = "com.curfew.MainActivity";
    private ParseUser mCurrentUser;

    protected ArrayList<String> mToUserList;
    protected ListView mCurfewListView;
    protected ArrayAdapter mCurfewAdapter;
    protected ImageView mProfilePicture;

    private final String PARSE_APPLICATION_ID = "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs";
    private final String PARSE_CLIENT_KEY = "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI";

    private ParseQueryAdapter<ParseObject> mCurfewAdapterP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
        ParseAnalytics.trackAppOpened(getIntent());

        mCurrentUser = ParseUser.getCurrentUser();
        mUserNameTextView = (TextView) findViewById(R.id.username);
        mCurfewListView = (ListView) findViewById(R.id.curfewListView);
        mToUserList = new ArrayList<String>();
        mCurfewAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.curfewtextview, mToUserList);
        mCurfewListView.setAdapter(mCurfewAdapter);
        mProfilePicture = (ImageView) findViewById(R.id.profilePicture);

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

            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Curfew");
            parseQuery.whereEqualTo("fromUser", mCurrentUser);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> curfews, ParseException e) {
                    if (e == null) {
                        for (ParseObject parseObject : curfews) {
                            ParseUser user = (ParseUser) parseObject.get("toUser");
                            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        String name = parseObject.getString("username");
                                        int index = mToUserList.indexOf(name);
                                        if (index > -1)
                                            mToUserList.set(index, name);
                                        else {
                                            mToUserList.add(name);
                                        }
                                        Collections.sort(mToUserList);
                                        mCurfewAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, "Error getting curfew user" + e.getMessage());
                                    }
                                }
                            });

                        }
                    } else {
                        Log.e(TAG, "Parse error " + e.getMessage());
                    }
                }
            });
            ParseFile file = (ParseFile)mCurrentUser.get("profilePicture");
            try{
                file.getDataInBackground(new GetDataCallback(){
                    @Override
                    public void done(byte[] data, ParseException e){
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if(data!=null)
                            mProfilePicture.setImageBitmap(bmp);

                    }
                });
            }catch(Exception e){
                Log.d(TAG, e.toString());
            }
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
            case R.id.action_profile_settings:
                Intent intent3 = new Intent(this, ProfileSettingsActivity.class);
                startActivity(intent3);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}