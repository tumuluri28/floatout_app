package com.floatout.android.floatout_v01;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private ArrayList<String> textViewStoryDesc = new ArrayList<>();
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
            storyImage.setBackgroundColor(getResources().getColor(R.color.random1));
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random2));
        }
        if (n==2){
            storyImage.setBackgroundColor(getResources().getColor(R.color.random2));
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random3));
        }
        if(n==3) {
            storyImage.setBackgroundColor(getResources().getColor(R.color.random3));
            storyDesc.setBackgroundColor(getResources().getColor(R.color.random1));
        }


        storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
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
                getStory(0);
                Log.v(LOG_TAG, "children count " + Integer.toString(numOfChildren));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Log.v(LOG_TAG, storyId);

        storyImage.setOnTouchListener(new OnSwipeTouchListener(StoryFeedActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(StoryFeedActivity.this, "top", Toast.LENGTH_SHORT).show();
                Intent intent  = new Intent(StoryFeedActivity.this, MainActivity.class);
                startActivity(intent);
            }
            public void onSwipeRight() {
                Toast.makeText(StoryFeedActivity.this, "right", Toast.LENGTH_SHORT).show();
                if(current == 0){
                    onSwipeTop();
                }
                getStory(current-1);
            }
            public void onSwipeLeft() {
                Toast.makeText(StoryFeedActivity.this, "left", Toast.LENGTH_SHORT).show();
                if(current == storyFeedList.size()-1){
                    onSwipeTop();
                }
                getStory(current+1);

            }
            public void onSwipeBottom() {
                Toast.makeText(StoryFeedActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getStory(int storyNumber){
        if(storyNumber >= 0) {
            current = storyNumber;
            if (!storyFeedList.isEmpty()) {
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