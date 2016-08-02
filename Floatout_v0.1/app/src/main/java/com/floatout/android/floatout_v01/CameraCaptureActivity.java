package com.floatout.android.floatout_v01;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.model.StorytagList;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CameraCaptureActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient googleApiClient;

    protected Location location;

    ImageView im;
    String path;
    String strLocation;
    File f;
    private Size imageDimension;
    ImageButton add,clear;
    EditText description;
    Bitmap bm;
    int i;
    String storageBucket;
    private Spinner mCamera_Storytag_List;
    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref, usersRef,databaseRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    private ArrayList<String> storyNames = new ArrayList<>();

    String uid;

    final String LOG_TAG = MainActivity_Fragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        googleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();

        firebaseInitializations();
        initializeScreen();

        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        im = (ImageView) findViewById(R.id.image);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        try{
            f = new File(path, "image.jpg");
            bm = BitmapFactory.decodeStream(new FileInputStream(f));
            im.setImageBitmap(bm);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        im.setOnTouchListener(new OnSwipeTouchListener(CameraCaptureActivity.this) {
            public void onSwipeRight() {
                getBackToCameraActivity();
            }
        });

        description = (EditText) findViewById(R.id.description);

        add = (ImageButton) findViewById(R.id.add_story);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView storyTag = (TextView) findViewById(R.id.camera_storytag_data);
                String selected = storyTag.getText().toString();
                storageRef = fStorage.getInstance().getReference();
                FirebaseUser user = mAuth.getCurrentUser();
                String userEmail = user.getEmail();
                int index = 0;
                for (String stories: storyNames){
                    if(stories == selected){
                        index = storyNames.indexOf(selected);
                        if(index == 0) {
                            storageBucket = Integer.toString(index+1);
                        } else {
                            storageBucket = Integer.toString(index);
                        }
                    }
                }
                Uri file = Uri.fromFile(f);

                String storyDescription = description.getText().toString();

                DatabaseReference newPost = databaseRef.child(Constants.FIREBASE_LOCATION_STORYFEED)
                        .child(storageBucket).push();
                newPost.child(Constants.FIREBASE_STORYFEED_URL)
                        .setValue(userEmail+newPost.getKey()+file.getLastPathSegment());
                newPost.child(Constants.FIREBASE_STORYFEED_USERID).setValue(uid);
                newPost.child(Constants.FIREBASE_STORYFEED_DESCRIPTION).setValue(storyDescription);
                newPost.child(Constants.FIREBASE_STORYFEED_LOCATION).setValue(strLocation);

                StorageReference storyIdBucket = storageRef.child(storageBucket+"/"+userEmail+newPost.getKey()+file.getLastPathSegment());
                UploadTask uploadTask = storyIdBucket.putFile(file);

                getBackToMainActivity();


                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        f.delete();
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        fullScreen();

        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

    private void fullScreen(){
        View decorView = getWindow().getDecorView();
        int uioptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uioptions);
    }

    private void initializeScreen() {
        mCamera_Storytag_List = (Spinner) findViewById(R.id.camera_storytag_list);
        mStorytagListAdapter = new FirebaseListAdapter<StorytagList>(CameraCaptureActivity.this, StorytagList.class, R.layout.camera_list_item_storytag, ref) {
            @Override
            protected void populateView(View view, StorytagList model, int position) {
                TextView storyTag = (TextView) view.findViewById(R.id.camera_storytag_data);
                storyTag.setText(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
                storyNames.add(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
            }
        };
        mCamera_Storytag_List.setAdapter(mStorytagListAdapter);
        //Log.v(LOG_TAG, Integer.toString(mCamera_Storytag_List.getChildCount()));
    }

    private void getBackToCameraActivity() {
        Intent cameraActivityIntent = new Intent(CameraCaptureActivity.this, CameraActivity.class);
        cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(cameraActivityIntent);
    }

    private void getBackToMainActivity() {
        Toast.makeText(CameraCaptureActivity.this, "Story Added!", Toast.LENGTH_SHORT)
                .show();

        Intent mainActivityIntent = new Intent(CameraCaptureActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private void firebaseInitializations() {
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYTAGS);
        usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Double lat;
            Double lon;
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                new GetAddressTask().execute(location);
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.v(LOG_TAG, "co-ords lat cam " + String.valueOf(lat));
                Log.v(LOG_TAG, "co-ords lon cam " + String.valueOf(lon));
                /*Toast.makeText(getActivity(), "this is my Toast message!!! =)",
                        Toast.LENGTH_LONG).show();*/
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class GetAddressTask extends AsyncTask<Location, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Location... params) {
            Geocoder geoCoder = new Geocoder(CameraCaptureActivity.this, Locale.getDefault());
            Location loc = params[0];
            List<Address> addresses = null;

            try{
                addresses = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(),1);
            }catch (IOException e1){
                e1.printStackTrace();
            }

            if(addresses !=null && addresses.size()>0){
                if(addresses.get(0).getFeatureName() == null) {
                    strLocation = addresses.get(0).getSubLocality()+","+
                            addresses.get(0).getLocality() + "," +
                            addresses.get(0).getAdminArea() + "," +
                            addresses.get(0).getCountryName();
                }else {
                    strLocation = addresses.get(0).getFeatureName()+","+
                            addresses.get(0).getSubLocality()+","+
                            addresses.get(0).getLocality() + "," +
                            addresses.get(0).getAdminArea() + "," +
                            addresses.get(0).getCountryName();
                }
            }
            return strLocation;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.v(LOG_TAG, "came cap " + s);
        }
    }
}
