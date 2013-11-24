package com.curfew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class ProfileSettingsActivity extends Activity {
    private ParseUser mCurrentUser;
    TextView mUsernameTextView;
    ImageView mProfilePicture;
    Button mEditProfilePicture;
    private String TAG = "com.curfew.ProfileSettingsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);
        mCurrentUser = ParseUser.getCurrentUser();
        mUsernameTextView = (TextView) findViewById(R.id.username);
        mUsernameTextView.setText(mCurrentUser.getUsername());
        mProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        mEditProfilePicture = (Button) findViewById(R.id.editProfilePictureButton);
        mEditProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ProfileSettingsActivity.this, ImageUpload.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_settings, menu);
        return true;
    }
    
}
