package com.floatout.android.floatout_v01;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.model.StorytagList;
import com.floatout.android.floatout_v01.utils.Constants;
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
import java.util.ArrayList;

public class CameraCaptureActivity extends AppCompatActivity {
    ImageView im;
    String path;
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

    final String LOG_TAG = MainActivity_Fragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        firebaseInitializations();
        initializeScreen();
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
                Intent cameraActivityIntent = new Intent(CameraCaptureActivity.this, CameraActivity.class);
                cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                cameraActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(cameraActivityIntent);

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

                DatabaseReference newPost = databaseRef.child(Constants.FIREBASE_LOCATION_STORYFEED).child(storageBucket).push();
                databaseRef.child(Constants.FIREBASE_LOCATION_STORYFEED).child(storageBucket);
                newPost.setValue(userEmail+newPost.getKey()+file.getLastPathSegment());

                StorageReference storyIdBucket = storageRef.child(storageBucket+"/"+userEmail+newPost.getKey()+file.getLastPathSegment());
                UploadTask uploadTask = storyIdBucket.putFile(file);

                startIntent();
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        f.delete();
                    }
                });

                String storyDescription = description.getText().toString();
                DatabaseReference newPostDescription = databaseRef.child(Constants.FIREBASE_LOCATION_STORYFEED_DESC)
                        .child(storageBucket).child(newPost.getKey());
                newPostDescription.setValue(storyDescription);
            }
        });
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        fullScreen();

        return super.onCreateView(parent, name, context, attrs);
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

    private void startIntent() {
        Intent intent = new Intent(CameraCaptureActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void firebaseInitializations() {
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYTAGS);
        usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }
}
