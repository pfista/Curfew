package com.curfew;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageUpload extends Activity {
    private static final int PICK_IMAGE = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private ImageView imgView;
    private Button upload;
    private Button loadPicture;
    private EditText caption;
    private Bitmap bitmap;
    private ProgressDialog dialog;
    private ParseUser mCurrentUser;
    private String TAG = "com.curfew.ImageUpload";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageupload);

        imgView = (ImageView) findViewById(R.id.ImageView);
        upload = (Button) findViewById(R.id.Upload);
        loadPicture = (Button) findViewById(R.id.load_picture_button);
        mCurrentUser = ParseUser.getCurrentUser();

        loadPicture.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(),
                            "Please select image", Toast.LENGTH_SHORT).show();
                } else {
                    dialog = ProgressDialog.show(ImageUpload.this, "Uploading",
                            "Please wait...", true);
                    new ImageUploadTask().execute();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.imageupload_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ic_menu_gallery:
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            PICK_IMAGE);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.exception_message),
                            Toast.LENGTH_LONG).show();
                    Log.e(e.getClass().getName(), e.getMessage(), e);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    String filePath = null;

                    try {
                        // OI FILE Manager
                        String filemanagerstring = selectedImageUri.getPath();

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(selectedImageUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (filemanagerstring != null) {
                            filePath = filemanagerstring;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (filePath != null) {
                            decodeFile(filePath);
                        } else {
                            bitmap = null;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        Log.e(e.getClass().getName(), e.getMessage(), e);
                    }
                }
                break;
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    ImageView imageView = (ImageView) findViewById(R.id.ImageView);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    File f = new File(picturePath);

                    bitmap = decodeFile(f);
                    imgView.setImageBitmap(bitmap);
                }

            default:

        }
    }
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=70;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        }
        return null;
    }

    class ImageUploadTask extends AsyncTask <Void, Void, String>{
        @Override
        protected String doInBackground(Void... unsued) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile file = new ParseFile("ProfilePicture.png", byteArray);
                file.save();

                mCurrentUser.put("profilePicture", file);
                mCurrentUser.save();
                return "Success";

            } catch (Exception e) {
                if (dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.exception_message),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
                return null;
            }

            // (null);
        }

        @Override
        protected void onProgressUpdate(Void... unsued) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (dialog.isShowing())
                    dialog.dismiss();

                if (sResponse != null) {
                        Toast.makeText(getApplicationContext(),
                                "Photo uploaded successfully",
                                Toast.LENGTH_SHORT).show();
                    }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.exception_message),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        System.gc();

    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);

        imgView.setImageBitmap(bitmap);

    }
}