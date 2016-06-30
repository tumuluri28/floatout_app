package com.floatout.android.floatout_v01;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.floatout.android.floatout_v01.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class StoryFeedActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private StorySlidePageAdapter mStatePageAdapter;

    private DatabaseReference storyFeed, storyFeedDesc;
    private FirebaseAuth mAuth;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    private StorageReference storageStoryNumber;

    private ArrayList<String> storageLocationStoryFeed = new ArrayList<>();
    private ArrayList<String> databaseLocationStoryDescKey = new ArrayList<>();
    private ArrayList<String> textViewStoryDesc = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();

    String storyId;
    String LOG_TAG = StoryFeedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        storyFeed = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYFEED);
        storyFeedDesc = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYFEED_DESC);
        mAuth = FirebaseAuth.getInstance();
        storageRef = fStorage.getInstance().getReference();

        Intent intent = getIntent();
        storyId = intent.getStringExtra("storyId");

        final DatabaseReference storyIdFeed = storyFeed.child(storyId);
        DatabaseReference storyIdFeedDesc = storyFeedDesc.child(storyId);

        storyIdFeed.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                for (MutableData data : currentData.getChildren()) {
                    storageLocationStoryFeed.add(data.getValue().toString());
                    databaseLocationStoryDescKey.add(data.getKey());
                }
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });

        storyIdFeedDesc.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                for(String key : databaseLocationStoryDescKey){
                    String storyDesc = currentData.child(key).getValue().toString();
                    textViewStoryDesc.add(storyDesc);
                }
                Log.v(LOG_TAG, "hello " + textViewStoryDesc.toString());
                Log.v(LOG_TAG, "blah blah " + storageLocationStoryFeed.toString());
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                //getDataFromStorage(storyId);
                passData(storyId);
            }
        });

    }

    private void passData(String storyId) {
        getDataFromStorage(storyId);
        File imageFile;
        ArrayList<Bitmap> bm = new ArrayList<>();
        try {
            for (String path : paths) {
                imageFile = new File(path);
                bm.add(BitmapFactory.decodeStream(new FileInputStream(imageFile)));
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Log.v(LOG_TAG, "Size " + Integer.toString(bm.size()));
        mStatePageAdapter = new StorySlidePageAdapter(getSupportFragmentManager(),this, bm.size(),bm,textViewStoryDesc);
        viewPager.setAdapter(mStatePageAdapter);
        //mStatePageAdapter = new StorySlidePageAdapter(getSupportFragmentManager(), this, paths.size(), )
    }

    private void getDataFromStorage(String storyId) {
        Log.v(LOG_TAG, "hello2 " + storageLocationStoryFeed.toString());
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("storyFeed"+storyId, Context.MODE_PRIVATE);
        for(int storyNumber = storageLocationStoryFeed.size() - 1; storyNumber >= 0 ; storyNumber--){
            storageStoryNumber = storageRef.child(storyId+"/"+storageLocationStoryFeed.get(storyNumber));
            File path = new File(dir, storageLocationStoryFeed.get(storyNumber));
            storageStoryNumber.getFile(path);
            paths.add(path.toString());
        }
    }
}