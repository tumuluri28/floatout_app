package com.floatout.android.floatout_v01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class StoryFeedActivity extends AppCompatActivity  {

    private ImageView storyImage;

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
    private ArrayList<String> storyFeedList = new ArrayList<>();

    ArrayList<Bitmap> bm = new ArrayList<>();
    ArrayList<Bitmap> bm2;

    private ArrayList<String> backgroundTaskData = new ArrayList<>();

    int numOfChildren = 0;
    int tracker = 4;
    int segmentTracker = 0;
    int flag= 0;
    int imageTracker = 0;
    int refreshFlag = 0;
    int storyEndFlag = 0;

    String storyIdCachePath, storyId;
    String LOG_TAG = StoryFeedActivity.class.getSimpleName();

    BackgroundDeleteFilesTask bt = new BackgroundDeleteFilesTask();
    BackgroundDownloadTask bdt = new BackgroundDownloadTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        storyImage = (ImageView) findViewById(R.id.image);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);
        mAuth = FirebaseAuth.getInstance();
        storageRef = fStorage.getInstance().getReference();

        final Intent intent = getIntent();
        storyIdCachePath = intent.getStringExtra("storyIdCachePath");
        storyId = intent.getStringExtra("storyId");

        DatabaseReference storyFeedIdref = storyFeed.child(storyId);
        storyFeedIdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storyFeedList.clear();
                numOfChildren = (int) dataSnapshot.getChildrenCount();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    storyFeedList.add(data.getValue().toString());
                }
                segmentTracker = numOfChildren/5;
                flag = 1;
                Log.v(LOG_TAG, "children " + Integer.toString(storyFeedList.size()));
                Log.v(LOG_TAG, "i is " + Integer.toString(segmentTracker));
                Log.v(LOG_TAG, "children count " + Integer.toString(numOfChildren));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Log.v(LOG_TAG, storyId);

        passData(storyIdCachePath);

        storyImage.setOnTouchListener(new OnSwipeTouchListener(StoryFeedActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(StoryFeedActivity.this, "top", Toast.LENGTH_SHORT).show();
                Intent intent  = new Intent(StoryFeedActivity.this, MainActivity.class);
                intent.putExtra("storyFeedIntentFlag", "storyFeedIntentFlag");
                intent.putExtra("rtnStoryIdCachePath", storyIdCachePath);
                intent.putExtra("rtnStoryId", storyId);
                startActivity(intent);
            }
            public void onSwipeRight() {
                Toast.makeText(StoryFeedActivity.this, "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                //Toast.makeText(StoryFeedActivity.this, "left", Toast.LENGTH_SHORT).show();
                if(imageTracker == 1 && bt.getStatus() != AsyncTask.Status.FINISHED ){
                    bt.execute();

                }
                if(bt.getStatus() == AsyncTask.Status.FINISHED && bdt.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.v(LOG_TAG, "segments " + Integer.toString(segmentTracker));
                    if(segmentTracker != 0){
                        bdt.execute();
                    }
                    else{}
                }
                if (imageTracker == bm.size() && bdt.getStatus() == AsyncTask.Status.FINISHED){
                    if(segmentTracker != 0) {
                        imageTracker = 0;
                        bm.clear();
                        bt.restart();
                        bdt.restart();
                        getData(storyIdCachePath);
                    }
                    if(storyEndFlag == 1){
                        onSwipeTop();
                        //return;
                    }
                    if(segmentTracker == 0){
                        imageTracker = 0;
                        bm.clear();
                        getData(storyIdCachePath);
                        storyEndFlag = 1;
                    }
                }
                if(numOfChildren < 5 && imageTracker == bm.size()){
                    onSwipeTop();
                    return;
                }
                if(imageTracker < bm.size()) {
                    Log.v(LOG_TAG, "story number " + Integer.toString(imageTracker));
                    storyImage.setImageBitmap(bm.get(imageTracker));
                    imageTracker++;
                }
            }
            public void onSwipeBottom() {
                Toast.makeText(StoryFeedActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void passData(String storyIdCachePath) {
        //getDataFromStorage(storyId);
        File dir = new File(storyIdCachePath);
        File[] imageFiles = dir.listFiles();
        try {
            for (File child : imageFiles) {
                bm.add(BitmapFactory.decodeStream(new FileInputStream(child)));
            }
            Log.v(LOG_TAG, "Size " + Integer.toString(bm.size()));
            if(!bm.isEmpty()) {
                storyImage.setImageBitmap(bm.get(0));
                imageTracker++;
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private void getData(String storyIdCachePath){
        File dir = new File(storyIdCachePath);
        File[] imageFiles = dir.listFiles();
        try {
            for (File child : imageFiles) {
                bm.add(BitmapFactory.decodeStream(new FileInputStream(child)));
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    private class BackgroundDeleteFilesTask extends AsyncTask<Void,Void,Void>{
        public void restart(){
            bt = new BackgroundDeleteFilesTask();
        }
        @Override
        protected synchronized Void doInBackground(Void... params) {
            Log.v(LOG_TAG, "I'm here");
            File dir = new File(storyIdCachePath);
            File[] imageFiles = dir.listFiles();
            if(imageFiles.toString() != null){
                for(File image: imageFiles){
                    image.delete();
                }
            }
            return null;
        }
    }

    private class BackgroundDownloadTask extends AsyncTask<Void, Void, Void>{
        public void restart(){
            bdt = new BackgroundDownloadTask();
        }

        @Override
        protected synchronized Void doInBackground(Void... params) {
            if(numOfChildren <= 5){
                storyEndFlag = 1;
            }
            /*if(numOfChildren > 5 && segmentTracker == 1 && !storyFeedList.isEmpty()){
                for(String story: storyFeedList.subList(tracker+1,storyFeedList.size())){
                    Log.v(LOG_TAG, "children now " + story);
                    storageStoryNumber = storageRef.child(storyId+"/"+story);
                    File path = new File(storyIdCachePath, story);
                    storageStoryNumber.getFile(path);
                }
                segmentTracker--;
            }*/
            else if(numOfChildren > 5 && segmentTracker >= 1 && !storyFeedList.isEmpty()){
                if(segmentTracker > 1) {
                    for (String story : storyFeedList.subList(tracker + 1, tracker + 6)) {
                        Log.v(LOG_TAG, "children now " + story);
                        storageStoryNumber = storageRef.child(storyId + "/" + story);
                        File path = new File(storyIdCachePath, story);
                        storageStoryNumber.getFile(path);
                    }
                    tracker = tracker + 5;
                    segmentTracker--;
                }else{
                    for (String story : storyFeedList.subList(tracker + 1, storyFeedList.size())) {
                        Log.v(LOG_TAG, "children now " + story);
                        storageStoryNumber = storageRef.child(storyId + "/" + story);
                        File path = new File(storyIdCachePath, story);
                        storageStoryNumber.getFile(path);
                    }
                    segmentTracker--;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //passData(storyIdCachePath);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backPressIntent = new Intent(StoryFeedActivity.this, MainActivity.class);
        backPressIntent.putExtra("storyFeedIntentFlag", "storyFeedIntentFlag");
        backPressIntent.putExtra("rtnStoryIdCachePath", storyIdCachePath);
        backPressIntent.putExtra("rtnStoryId", storyId);
        startActivity(backPressIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bm.clear();
        bt.restart();
        bt.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}