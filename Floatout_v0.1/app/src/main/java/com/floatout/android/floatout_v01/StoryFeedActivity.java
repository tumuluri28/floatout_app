package com.floatout.android.floatout_v01;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class StoryFeedActivity extends AppCompatActivity  {

    private ImageView storyImage;
    private TextView storyDesc;

    private DatabaseReference storyFeed, storyFeedDesc;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    private ArrayList<String> storyFeedDescList = new ArrayList<>();
    private ArrayList<String> storyFeedList = new ArrayList<>();

    int current;
    int numOfChildren;

    String storyId;
    String LOG_TAG = StoryFeedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        Random rand = new Random();
        int  n = rand.nextInt(3) + 1;

        storyDesc = (TextView) findViewById(R.id.storyDesc);
        storyImage = (ImageView) findViewById(R.id.image);
        if(n==1) {
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random2));
        }
        if (n==2){
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random3));
        }
        if(n==3) {
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random1));
        }

        storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);
        storyFeedDesc = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED_DESC);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        storyId = intent.getStringExtra("storyId");

        DatabaseReference storyFeedIdref = storyFeed.child(storyId);
        final DatabaseReference storyFeedDescIdRef = storyFeedDesc.child(storyId);

        storyFeedIdref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                storyFeedList.clear();
                numOfChildren = (int) currentData.getChildrenCount();
                for(MutableData data: currentData.getChildren()){
                    storyFeedList.add(data.getValue().toString());
                }
                Log.v(LOG_TAG, "children count " + Integer.toString(numOfChildren));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                storyFeedDescIdRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData currentData) {
                        storyFeedDescList.clear();
                        for(MutableData data: currentData.getChildren()){
                            storyFeedDescList.add(data.getValue().toString());
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot){
                        getStory(0);
                    }
                });
            }
        });
        Log.v(LOG_TAG, storyId);

        storyImage.setOnTouchListener(new OnSwipeTouchListener(StoryFeedActivity.this) {
            public void onSwipeTop() {

            }
            public void onSwipeRight() {
                if(current == 0){
                    onSwipeBottom();
                }
                getStory(current-1);
            }
            public void onSwipeLeft() {
                if(current == storyFeedList.size()-1){
                    onSwipeBottom();
                }
                getStory(current+1);
            }
            public void onSwipeBottom() {
                Intent intent  = new Intent(StoryFeedActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getStory(int storyNumber){
        if(storyNumber >= 0 && !storyFeedList.isEmpty()) {
            current = storyNumber;
            storyDesc.setText(storyFeedDescList.get(storyNumber));
            storageRef.child(storyId + "/" + storyFeedList.get(storyNumber)).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Picasso.with(getApplicationContext()).load(url).into(storyImage);
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}