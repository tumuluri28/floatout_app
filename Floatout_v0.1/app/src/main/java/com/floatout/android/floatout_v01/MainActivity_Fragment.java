package com.floatout.android.floatout_v01;

//import android.app.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.floatout.android.floatout_v01.model.StorytagList;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * Created by Yashwanth on 16-06-01.
 */
public class MainActivity_Fragment extends Fragment {

    private ListView mStoryTagList;

    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref;
    private DatabaseReference ref2;
    private DataSnapshot ds;
    private Query q;

    private ArrayList<String> storyNames = new ArrayList<>();

    private ArrayList<String> views = new ArrayList<>();

    private static final int increment_view = 1;

    private int v = 0;

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
        ref2 = FirebaseDatabase.getInstance().getReference("storyViews");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child : dataSnapshot.getChildren()){
                    String r = child.getKey();
                    //child.child(r).child("views").getValue();
                    String t = dataSnapshot.getRef().child(r).child("views").getKey();
                    child.child(t).getValue();

                    views.add(child.child(t).getValue().toString());

                    //child.child("1").child("views").getValue();
                    //child.child(key).child("views").getValue();
                    //views.add(child.child("views").getValue().toString());
                    //Log.v(LOG_TAG, "children view " + child.child(key).child("views").getValue());

                    //Log.v(LOG_TAG, "children view " +child.child(t).getValue());
                }
                //Log.v(LOG_TAG, "views" + views);

                //Log.v(LOG_TAG, "children" + dataSnapshot.);
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


        //ref2.child("1").child("views").setValue(1);
        //Log.v(LOG_TAG, "URL: "+ ref2.child("1").child("views"));

        


        mStoryTagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ((TextView) view.findViewById(R.id.storytag_data)).getText().toString();

                int index = 0;
                v= 0;

                Log.v(LOG_TAG, "stories " + storyNames);
                //Log.v(LOG_TAG, "views " + views);

                for (String stories : storyNames) {
                    if (selected == stories) {
                        index = storyNames.indexOf(selected);
                        v = Integer.parseInt(views.get(index));
                        v++;

                        ref2.child(Integer.toString(index+1)).child("views").setValue(v);

                        Log.v(LOG_TAG, "views_inc " + v);
                        /*views.set(index, views.get(index) + increment_view);
                        views.get(index);
                        Log.v(LOG_TAG, "view count" + views.get(index));*/
                    }
                }
                Log.v(LOG_TAG, "URL " + ref2.child(Integer.toString(index+1)).child("views"));



                //newref.child("views").setValue(1);
                //Log.v(LOG_TAG, "storyviews:" + ref2.toString());
                /*HashMap<String, String> main = new HashMap<String, String>();
                main.put("storyName",selected);*/

                /*StorytagViews vv = new StorytagViews(v);
                ref = FirebaseDatabase.getInstance().getReference().child("storyViews");
                DatabaseReference r = ref.child(Integer.toString(index)).getDatabase().getReference();
                //DatabaseReference r = ref.push();
                r.setValue(vv);
               // ref.setValue(vv);*/


                //Log.v(LOG_TAG, "updated view count" + views.toString());
                //ref2 = FirebaseDatabase.getInstance().getReference().child(selected);
                //ref2.setValue(1);

                //Log.v(LOG_TAG,ref2.toString());
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

                //views.add(model.getViews());
            }
        };

        mStoryTagList.setAdapter(mStorytagListAdapter);
        //Log.v(LOG_TAG,"views" + views);

    }
}