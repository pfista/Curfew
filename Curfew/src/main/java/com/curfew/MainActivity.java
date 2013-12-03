package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends Activity {

    private TextView mUserNameTextView;
    private final String TAG = "com.curfew.MainActivity";
    private final String CURFEW_LIST_CACHE = "com.curfew.list.curfew_list_cache";
    private ParseUser mCurrentUser;

    protected ListView mCurfewListView;
    protected ImageView mProfilePicture;

    protected CurfewQueryAdapter<ParseObject> mCurfewAdapter;

    private final String PARSE_APPLICATION_ID = "OsjvQm4BT1hdH1bkBZ3ljx9T8tbRiLAf1cojknJs";
    private final String PARSE_CLIENT_KEY = "ah2Y1VCB6MkOplR0YpL9M60Ex2qEhKkISL1ciRdI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
        ParseAnalytics.trackAppOpened(getIntent());

        mCurrentUser = ParseUser.getCurrentUser();
        mUserNameTextView = (TextView) findViewById(R.id.username);
        mCurfewListView = (ListView) findViewById(R.id.curfewListView);

        mProfilePicture = (ImageView) findViewById(R.id.profilePicture);

        mCurfewAdapter = new CurfewQueryAdapter<ParseObject>(this,
                new CurfewQueryAdapter.QueryFactory<ParseObject>() {
                    @Override
                    public ParseQuery<ParseObject> create() {
                        ParseQuery query = new ParseQuery("Curfew");
                        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                        query.whereEqualTo("fromUser", mCurrentUser);
                        query.orderByAscending("toUser");
                        return query;
                    }
                });

        // Perhaps set a callback to be fired upon successful loading of a new set of ParseObjects.
        mCurfewAdapter.addOnQueryLoadListener(new CurfewQueryAdapter.OnQueryLoadListener<ParseObject>() {

            public void onLoading() {
                Log.i(TAG, "Started loading");
            }

            public void onLoaded(List<ParseObject> objects, Exception e) {
                // Execute any post-loading logic, hide "loading" UI
                Log.i(TAG, "Done loading");
            }
        });

        mCurfewListView.setAdapter(mCurfewAdapter);

        mCurfewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view.findViewById(R.id.curfew_item_text)).getText().toString();
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("username", item);
                startActivity(intent);
            }
        });

        //Starting the service
        startService(new Intent(this, CurfewService.class));

        registerForContextMenu(mCurfewListView);

    }

    @Override
    public void onResume() {
        super.onResume();
        mCurfewAdapter.loadObjects();
        if (mCurrentUser != null) {
            mUserNameTextView.setText(mCurrentUser.getUsername());
            ParseFile file = (ParseFile)mCurrentUser.get("profilePicture");
            try{
                file.getDataInBackground(new GetDataCallback(){
                    @Override
                    public void done(byte[] data, ParseException e){
                        if (data != null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            mProfilePicture.setImageBitmap(bmp);
                        }

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        try {
            // TODO: is there an easier way to do this?
            menu.setHeaderTitle(mCurfewAdapter.getItem(info.position).getParseUser("toUser").fetchIfNeeded().getString("username"));
        }
        catch (ParseException e){
            Log.e(TAG, e.getMessage());
        }
        menu.add("Edit");
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO: switch on options
        Intent intent = new Intent(this, SetCurfewActivity.class);
        intent.putExtra("username", item.getTitle());

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        try {
            // TODO: is there an easier way to do this?
            intent.putExtra("username", mCurfewAdapter.getItem(info.position).getParseUser("toUser").fetchIfNeeded().getString("username"));
        }
        catch (ParseException e){
            Log.e(TAG, e.getMessage());
        }

        startActivity(intent);
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
            case R.id.action_add_curfew:
                Intent intent2 = new Intent(this, SetCurfewActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_profile_settings:
                Intent intent3 = new Intent(this, ProfileSettingsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}