package com.floatout.android.floatout_v01;

//import android.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.floatout.android.floatout_v01.login.LoginActivity;
import com.floatout.android.floatout_v01.model.StorytagList;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * Created by Yashwanth on 16-06-01.
 */
public class MainActivity_Fragment extends Fragment {

    private ProgressDialog progressDialog;

    private ListView mStoryTagList;
    private ImageButton menu_Button, camera_button;
    private TextView storyTag;
    private View rootview;

    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference storyTags;
    private DatabaseReference storyTagStats;
    private DatabaseReference connectedRef;
    private FirebaseAuth mAuth;

    private ArrayList<String> storyNames = new ArrayList<>();
    private ArrayList<String> storyIds = new ArrayList<>();

    boolean connected;

    private String uid;
    private final String LOG_TAG = MainActivity_Fragment.class.getSimpleName();

    public MainActivity_Fragment() {
    }

    public static MainActivity_Fragment newInstance() {
        MainActivity_Fragment fragment = new MainActivity_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    storyTagStats = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYTAGSTATS);
                    storyTagStats.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            storyIds.clear();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                storyIds.add(data.getKey());
                            }
                            Log.v(LOG_TAG, "IDs " + storyIds.toString());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    System.out.println("not connected");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_mainactivity, container, false);
        initializeScreen(rootview);
        menuButtonListner(rootview);
        cameraButtonListner(rootview);

        return rootview;
    }

    private void initializeScreen(View rootView) {
        mStoryTagList = (ListView) rootView.findViewById(R.id.storytag_list);
        storyTags = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYTAGS);
        mStorytagListAdapter = new FirebaseListAdapter<StorytagList>(getActivity(), StorytagList.class, R.layout.list_item_storytag, storyTags) {
            @Override
            protected void populateView(View view, StorytagList model, int position) {
                storyTag = (TextView) view.findViewById(R.id.storytag_text);
                storyTag.setText(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
                storyNames.add(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
            }
        };
        Log.v(LOG_TAG,storyNames.toString());
        mStoryTagList.setAdapter(mStorytagListAdapter);
        storyTagListListener();
    }

    private void storyTagListListener() {
        mStoryTagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ((TextView) view.findViewById(R.id.storytag_text)).getText().toString();
                int index;
                for (String story : storyNames) {
                    if (selected == story) {
                        index = storyNames.indexOf(selected);
                        String storyId = storyIds.get(index);
                        Log.v(LOG_TAG, "pressed story number " + storyId);

                        DatabaseReference totalViewsRef = storyTagStats.child(storyId)
                                .child(Constants.FIREBASE_STORYTAG_TOTALVIEWS);
                        totalViewsRef.runTransaction(new Transaction.Handler() {
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

                        DatabaseReference userViewsRef = storyTagStats.child(storyId).child("users").child(uid)
                                .child(Constants.FIREBASE_LOCATION_USER_VIEWS_STORYTAGSTATS);
                        Log.v(LOG_TAG, "userviewref " + userViewsRef.toString());

                        userViewsRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                if(currentData.getValue() == null){
                                    currentData.setValue(1);
                                } else {
                                    currentData.setValue((long) currentData.getValue() + 1 );
                                }
                                return Transaction.success(currentData);
                            }
                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            }
                        });
                        Intent storyFeedIntent = new Intent(getActivity(),StoryFeedActivity.class);
                        storyFeedIntent.putExtra("storyId", storyId);
                        startActivity(storyFeedIntent);
                    }
                }
            }
        });
    }

    private void menuButtonListner(View rootview) {
        menu_Button = (ImageButton) rootview.findViewById(R.id.menu_button);
        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void cameraButtonListner(View rootview){
        camera_button = (ImageButton) rootview.findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mStorytagListAdapter != null) {
            mStorytagListAdapter.cleanup();
        }
    }
}