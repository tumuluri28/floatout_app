package com.floatout.android.floatout_v01;

//import android.app.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Yashwanth on 16-06-01.
 */
public class MainActivity_Fragment extends Fragment {

    private ProgressDialog progressDialog;

    private ListView mStoryTagList;

    private ImageButton menu_Button, camera_button;

    private View rootview;

    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref;
    private DatabaseReference ref2;
    private DatabaseReference storyId;
    private DatabaseReference storyFeed;
    private DatabaseReference storyFeedDesc;
    private DatabaseReference connectedRef;
    private FirebaseAuth mAuth;

    private StorageReference storageRef;
    private StorageReference storageStoryNumber;

    private ArrayList<String> storyNames = new ArrayList<>();
    private ArrayList<String> totalViews = new ArrayList<>();
    private ArrayList<String> storyIds = new ArrayList<>();
    private ArrayList<String> cachePaths = new ArrayList<>();

    boolean connected;

    int cacheIndex;
    private static final int increment_view = 1;
    private int v;
    private int i = 1;
    private int j;

    private String uid;
    private String rtnStoryIdCachePath;
    private String rtnStoryId;

    private final String LOG_TAG = MainActivity_Fragment.class.getSimpleName();

    BackgroundTask bt = new BackgroundTask();
    BackgroundRtnDownloadTask rtnBt;


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
        if (getArguments() != null) {
        }

        storageRef = FirebaseStorage.getInstance().getReference();
        storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);
        storyFeedDesc = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED_DESC);

        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    //Log.v(LOG_TAG, storyFeedDesc.toString());
                    storyFeedDesc.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            storyIds.clear();
                            for(MutableData data : mutableData.getChildren()){
                                storyIds.add(data.getKey());
                            }
                            Log.v(LOG_TAG, "storyIds list " + storyIds.toString());
                            return Transaction.success(mutableData);
                        }
                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            createDirs();
                        }
                    });

                    if(!getActivity().getIntent().hasExtra("storyFeedIntentFlag")) {
                        bt.execute();
                    }

                } else {
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();
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
        ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYTAGS);
        mStorytagListAdapter = new FirebaseListAdapter<StorytagList>(getActivity(), StorytagList.class, R.layout.list_item_storytag, ref) {
            @Override
            protected void populateView(View view, StorytagList model, int position) {
                TextView storyTag = (TextView) view.findViewById(R.id.storytag_data);
                storyTag.setText(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
                storyNames.add(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
                Log.v(LOG_TAG,storyNames.toString());
            }
        };
        mStoryTagList.setAdapter(mStorytagListAdapter);
    }

    private void storyTagListListener() {
        mStoryTagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ((TextView) view.findViewById(R.id.storytag_data)).getText().toString();

                int index;
                v = 0;
                //Log.v(LOG_TAG, "stories " + storyNames);
                for (String stories : storyNames) {
                    if (selected == stories) {
                        index = storyNames.indexOf(selected);
                        v = Integer.parseInt(totalViews.get(index));
                        v++;

                        ref2.child(Integer.toString(index + increment_view)).
                                child(Constants.FIREBASE_STORYTAG_TOTALVIEWS).setValue(v);

                        storyId = ref2.child(Integer.toString(index + increment_view));

                        DatabaseReference userViewsRef = storyId.child(Constants.FIREBASE_USERS_STORYTAGSTATS).child(uid)
                                .child(Constants.FIREBASE_LOCATION_USER_VIEWS_STORYTAGSTATS);

                        Log.v(LOG_TAG, "storyId " + storyId);

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
                        String storyId = Integer.toString(index + 1);
                        String pressedStoryCachePath = cachePaths.get(index);
                        storyFeedIntent.putExtra("storyIdCachePath", pressedStoryCachePath);
                        storyFeedIntent.putExtra("storyId", storyId);
                        startActivity(storyFeedIntent);
                    }
                }
            }
        });
    }

    private void getStoryTagsTotalViews() {
        ref2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYTAGSTATS);
        ref2.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalViews.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String r = child.getKey();
                    String t = dataSnapshot.getRef().child(r).
                            child(Constants.FIREBASE_STORYTAG_TOTALVIEWS).getKey();
                    String totalViewCount = child.child(t).getValue().toString();
                    totalViews.add(totalViewCount);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    private void createDirs () {
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        cachePaths.clear();
        for(final String storyId: storyIds){
            File dir = cw.getDir("storyFeed"+storyId, Context.MODE_PRIVATE);
            cachePaths.add(dir.getAbsolutePath());
        }
        Log.v(LOG_TAG,"yola " + cachePaths.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        getStoryTagsTotalViews();
        Intent intent = getActivity().getIntent();
        rtnStoryId = intent.getStringExtra("rtnStoryId");
        rtnStoryIdCachePath = intent.getStringExtra("rtnStoryIdCachePath");
        if(intent.hasExtra("storyFeedIntentFlag")) {
            new BackgroundRtnDownloadTask().execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mStorytagListAdapter != null) {
            mStorytagListAdapter.cleanup();
        }
        new BackgroundTask().cancel(true);
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Wait up yo, I'm loading");
            //progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for(final String storyId: storyIds){
                Query storyFeedIdref = storyFeed.child(storyId);
                storyFeedIdref.limitToFirst(5).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        j = j + (int) dataSnapshot.getChildrenCount();
                        for(DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            storageStoryNumber = storageRef.child(storyId + "/" + ds.getValue().toString());
                            File path = new File(cachePaths.get(storyIds.indexOf(storyId)), ds.getValue().toString());
                            storageStoryNumber.getFile(path).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    i++;
                                }
                            });
                        }
                        Log.v(LOG_TAG," active dwnlds yash " + storageRef.getActiveDownloadTasks().size());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            if(i < j || i >= j){
                storyTagListListener();
                progressDialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class BackgroundRtnDownloadTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            storageRef = FirebaseStorage.getInstance().getReference();
            storyFeed = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYFEED);

            Query storyFeedIdref = storyFeed.child(rtnStoryId);
            storyFeedIdref.limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        storageStoryNumber = storageRef.child(rtnStoryId + "/" + ds.getValue().toString());
                        File path = new File(rtnStoryIdCachePath, ds.getValue().toString());
                        storageStoryNumber.getFile(path);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initializeScreen(rootview);
            storyTagListListener();
        }
    }
}