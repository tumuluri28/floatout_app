package com.floatout.android.floatout_v01;

//import android.app.Fragment;

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

    private ListView mStoryTagList;

    private ImageButton menu_Button;

    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref;
    private DatabaseReference ref2;
    private DatabaseReference storyId;
    private FirebaseAuth mAuth;

    private ArrayList<String> storyNames = new ArrayList<>();

    private ArrayList<String> totalViews = new ArrayList<>();

    private static final int increment_view = 1;

    private int v;

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
        if (getArguments() != null) {
        }

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();

        ref2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_LOCATION_STORYTAGSTATS);
        Log.v(LOG_TAG, "ref2 " + ref2);
        ref2.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalViews.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    //Log.v(LOG_TAG, "child " + child);

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_mainactivity, container, false);
        initializeScreen(rootview);

        menuButtonListner(rootview);

        mStoryTagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ((TextView) view.findViewById(R.id.storytag_data)).getText().toString();

                int index = 0;
                v = 0;

                //Log.v(LOG_TAG, "stories " + storyNames);

                for (String stories : storyNames) {
                    if (selected == stories) {
                        index = storyNames.indexOf(selected);
                        v = Integer.parseInt(totalViews.get(index));
                        v++;

                        //Log.v(LOG_TAG, "index" + index);

                        ref2.child(Integer.toString(index + increment_view)).
                                child(Constants.FIREBASE_STORYTAG_TOTALVIEWS).setValue(v);

                        storyId = ref2.child(Integer.toString(index + increment_view));

                        DatabaseReference userViewsRef = storyId.child("users").child(uid).child("views");

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
                    }
                }
            }
        });
        return rootview;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStorytagListAdapter.cleanup();
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
            }
        };

        mStoryTagList.setAdapter(mStorytagListAdapter);
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
}