package com.floatout.android.floatout_v01;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.floatout.android.floatout_v01.Gesture.OnSwipeTouchListener;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class StoryFeedActivity extends AppCompatActivity{

    private RelativeLayout storyDescLayout;
    private ImageView storyImage;
    private ImageButton storyLike,storyLocationButton;
    private TextView storyDesc,storyLocation;

    private DatabaseReference storyFeed;
    private DatabaseReference storyFeedIdref;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    private ArrayList<String> storyFeedDescList = new ArrayList<>();
    private ArrayList<String> storyFeedList = new ArrayList<>();
    private ArrayList<String> storyFeedKeys = new ArrayList<>();
    private ArrayList<String> storyFeedLocationList = new ArrayList<>();

    int current;
    int numOfChildren;
    int likeFlag;
    int screenWidth;
    int screenHeight;

    String storyId;
    String currentKey;
    String LOG_TAG = StoryFeedActivity.class.getSimpleName();

    Dialog locationDialog;

    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        Random rand = new Random();
        int  n = rand.nextInt(3) + 1;


        storyDesc = (TextView) findViewById(R.id.storyDesc);
        //storyLocation = (TextView) findViewById(R.id.storylocation);
        storyLike = (ImageButton) findViewById(R.id.storylike);
        storyLocationButton = (ImageButton) findViewById(R.id.storylocationbutton);

        storyImage = (ImageView) findViewById(R.id.image);

        textureView = (TextureView) findViewById(R.id.screensize);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        storyId = intent.getStringExtra("storyId");

        storyFeedIdref = storyFeed.child(storyId);

        storyFeedIdref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                numOfChildren = (int) currentData.getChildrenCount();
                    for (MutableData data : currentData.getChildren()) {
                        storyFeedKeys.add(data.getKey());
                        storyFeedList.add(data.child(Constants.FIREBASE_STORYFEED_URL)
                                .getValue().toString());
                        storyFeedDescList.add(data.child(Constants.FIREBASE_STORYFEED_DESCRIPTION)
                                .getValue().toString());
                        storyFeedLocationList.add(data.child(Constants.FIREBASE_STORYFEED_LOCATION)
                        .getValue().toString());

                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if(numOfChildren == 0){
                    Toast.makeText(StoryFeedActivity.this, "There's no story yo!, why don't you add?", Toast.LENGTH_LONG).show();
                    getBackToMainActivity();
                }else{
                    Log.v(LOG_TAG, "keys " + storyFeedKeys.toString());
                    getStory(0);
                }
            }
        });
        Log.v(LOG_TAG, storyId);


        storyImage.setOnTouchListener(new OnSwipeTouchListener(StoryFeedActivity.this) {
            public void onSwipeTop() {
                if(likeFlag == 0 && !storyFeedList.isEmpty()) {
                    storyLike.setBackgroundResource(R.drawable.like);
                    new BackgroundLikeTask().execute(currentKey);
                    likeFlag = 1;
                }
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
                getBackToMainActivity();
            }
        });

        storyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDialog.dismiss();
            }
        });

        storyLike.setBackgroundResource(R.drawable.like_24px);
        storyLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(likeFlag == 0) {
                    storyLike.setBackgroundResource(R.drawable.like);
                    new BackgroundLikeTask().execute(currentKey);
                    likeFlag = 1;
                }else {
                    storyLike.setBackgroundResource(R.drawable.like_24px);
                    new BackgroundUnlikeTask().execute(currentKey);
                    likeFlag = 0;
                }*/
            }
        });

        storyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDialog = new Dialog(StoryFeedActivity.this);
                locationDialog.setContentView(R.layout.location_popupdialog);
                locationDialog.setTitle("Saw this @");

                TextView storyLocation = (TextView) locationDialog.findViewById(R.id.storylocation);
                if(!storyFeedLocationList.isEmpty())storyLocation.setText(storyFeedLocationList.get(current));
                locationDialog.show();
            }
        });

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            screenWidth = width;
            screenHeight = height;
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void getStory(int storyNumber){
        if(storyNumber >= 0 && !storyFeedList.isEmpty()) {
            storyLike.setBackgroundResource(R.drawable.like_24px);
            currentKey = storyFeedKeys.get(storyNumber);
            FirebaseUser user = mAuth.getCurrentUser();
            final String uid = user.getUid();
            final DatabaseReference usersLikesRef = storyFeedIdref.child(currentKey)
                    .child("likes").child("users");
            usersLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() > 0){
                        if (dataSnapshot.child(uid).getValue() != null) {
                            likeFlag = 1;
                            storyLike.setBackgroundResource(R.drawable.like);
                        } else {
                            likeFlag = 0;
                            storyLike.setBackgroundResource(R.drawable.like_24px);
                        }
                    }else{
                        likeFlag = 0;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Log.v(LOG_TAG, "current key " + currentKey);
            current = storyNumber;
            if(!storyFeedDescList.isEmpty()) storyDesc.setText(storyFeedDescList.get(storyNumber));
            //if(!storyFeedLocationList.isEmpty())storyLocation.setText(storyFeedLocationList.get(storyNumber));
            storageRef.child(storyId + "/" + storyFeedList.get(storyNumber)).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Picasso.with(getApplicationContext()).load(url).resize(screenWidth,screenHeight)
                                    .into(storyImage);
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

    private void getBackToMainActivity() {
        Intent intent = new Intent(StoryFeedActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class BackgroundLikeTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            String currentStoryKey = params[0];
            final DatabaseReference likesCountRef = storyFeedIdref.child(currentStoryKey)
                    .child("likes").child("likesCount");
            likesCountRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    if(currentData.getValue() == null){
                        currentData.setValue(1);
                    }else{
                        currentData.setValue((long) currentData.getValue()+1);
                    }
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });

            storyFeedIdref.child(currentStoryKey).child("likes").child("users")
                    .child(uid).setValue("true");

            return null;
        }
    }

    private class BackgroundUnlikeTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            String currentStoryKey = params[0];
            final DatabaseReference likesCountRef = storyFeedIdref.child(currentStoryKey)
                    .child("likes").child("likesCount");
            likesCountRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    if((long) currentData.getValue() > 0){
                        currentData.setValue((long) currentData.getValue()- 1);
                    }
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
            final DatabaseReference usersLikesRef = storyFeedIdref.child(currentStoryKey)
                    .child("likes").child("users");
            usersLikesRef.child(uid).setValue(null);

            return null;
        }
    }
}