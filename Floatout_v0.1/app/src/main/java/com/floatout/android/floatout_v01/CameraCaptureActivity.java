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
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
    ImageButton add,backtoMain;
    EditText description;
    Bitmap bm;
    int i;
    String storageBucket;
    private ListView mCamera_Storytag_List;
    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref, usersRef,databaseRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    private ArrayList<String> storyNames = new ArrayList<>();

    String uid;

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
        int width = intent.getIntExtra("width", 720);
        int height = intent.getIntExtra("height",1280);

        try{
            f = new File(path, "image.jpg");
            bm = BitmapFactory.decodeStream(new FileInputStream(f));
            Bitmap scaled = Bitmap.createScaledBitmap(bm,width,height,true);
            im.setImageBitmap(scaled);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        im.setOnTouchListener(new OnSwipeTouchListener(CameraCaptureActivity.this) {
            public void onSwipeRight() {
                getBackToCameraActivity();
            }

            @Override
            public void onSwipeTop() {
                super.onSwipeTop();
                mCamera_Storytag_List.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
            }
        });


        description = (EditText) findViewById(R.id.description);
        description.setVisibility(View.INVISIBLE);

        add = (ImageButton) findViewById(R.id.add_story);
        add.setVisibility(View.INVISIBLE);


        backtoMain = (ImageButton) findViewById(R.id.backtomain);
        backtoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBackToMainActivity();
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
        mCamera_Storytag_List = (ListView) findViewById(R.id.camera_storytag_list);
        mCamera_Storytag_List.setVisibility(View.INVISIBLE);
        mStorytagListAdapter = new FirebaseListAdapter<StorytagList>(CameraCaptureActivity.this, StorytagList.class, R.layout.camera_list_item_storytag, ref) {
            @Override
            protected void populateView(View view, StorytagList model, int position) {
                TextView storyTag = (TextView) view.findViewById(R.id.camera_storytag_data);
                storyTag.setText(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
                storyNames.add(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
            }
        };
        mCamera_Storytag_List.setAdapter(mStorytagListAdapter);
        storySelectionListener();
        //Log.v(LOG_TAG, Integer.toString(mCamera_Storytag_List.getChildCount()));
    }

    private void storySelectionListener() {
        mCamera_Storytag_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                add.setVisibility(View.VISIBLE);
                String selected = ((TextView) view.findViewById(R.id.camera_storytag_data)).getText().toString();
                int index;
                for (String story : storyNames) {
                    if (selected == story) {
                        index = storyNames.indexOf(selected);
                        mCamera_Storytag_List.getItemAtPosition(index);
                        view.findViewById(R.id.camera_storytag_data).setBackgroundColor(getResources().getColor(R.color.random3));
                        storageBucket = Integer.toString(index+1);
                        addStory();
                    }
                }
            }
        });
    }

    private void addStory(){
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageRef = fStorage.getInstance().getReference();
                FirebaseUser user = mAuth.getCurrentUser();
                String userEmail = user.getEmail();

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

                Toast.makeText(CameraCaptureActivity.this, "Story Added!", Toast.LENGTH_SHORT)
                        .show();

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

    private void getBackToCameraActivity() {
        Intent cameraActivityIntent = new Intent(CameraCaptureActivity.this, CameraActivity.class);
        cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(cameraActivityIntent);
    }

    private void getBackToMainActivity() {
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
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.reconnect();
    }

    private class GetAddressTask extends AsyncTask<Location, Void, String>{

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
    }
}