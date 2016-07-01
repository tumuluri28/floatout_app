package com.floatout.android.floatout_v01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
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

    String storyIdCachePath, storyId;
    String LOG_TAG = StoryFeedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        /*storyFeed = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYFEED);
        storyFeedDesc = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYFEED_DESC);
        mAuth = FirebaseAuth.getInstance();
        storageRef = fStorage.getInstance().getReference();*/

        Intent intent = getIntent();
        storyIdCachePath = intent.getStringExtra("storyIdCachePath");
        storyId = intent.getStringExtra("storyId");
        Log.v(LOG_TAG, storyId);
        /*final DatabaseReference storyIdFeed = storyFeed.child(storyId);
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/
        passData(storyIdCachePath);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.v(LOG_TAG, "swiped page " + position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    private void passData(String storyIdCachePath) {
        //getDataFromStorage(storyId);
        File dir = new File(storyIdCachePath);
        File[] imageFiles = dir.listFiles();
        ArrayList<Bitmap> bm = new ArrayList<>();
        try {
            for (File child : imageFiles) {
                bm.add(BitmapFactory.decodeStream(new FileInputStream(child)));
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        finally {
            Log.v(LOG_TAG, "Size " + Integer.toString(bm.size()));
            mStatePageAdapter = new StorySlidePageAdapter(getSupportFragmentManager(),this, bm.size(),bm,textViewStoryDesc);
            viewPager.setAdapter(mStatePageAdapter);
        }

        //mStatePageAdapter = new StorySlidePageAdapter(getSupportFragmentManager(), this, paths.size(), )
    }

    /*private void getDataFromStorage(String storyId) {
        Log.v(LOG_TAG, "hello2 " + storageLocationStoryFeed.toString());
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("storyFeed"+storyId, Context.MODE_PRIVATE);
        for(int storyNumber = 0; storyNumber < storageLocationStoryFeed.size(); storyNumber++){
            storageStoryNumber = storageRef.child(storyId+"/"+storageLocationStoryFeed.get(storyNumber));
            File path = new File(dir, storageLocationStoryFeed.get(storyNumber));
            storageStoryNumber.getFile(path);
            paths.add(path.toString());
        }
    }*/
}