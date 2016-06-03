package com.floatout.android.floatout_v01;

//import android.app.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.floatout.android.floatout_v01.model.StorytagList;
import com.floatout.android.floatout_v01.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by Yashwanth on 16-06-01.
 */
public class MainActivity_Fragment extends Fragment {



    private ListView mStoryTagList;
    private FirebaseListAdapter mStorytagListAdapter;
    private DatabaseReference ref;



    public MainActivity_Fragment() {
    }

    public static MainActivity_Fragment newInstance(){
        MainActivity_Fragment fragment = new MainActivity_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        if(getArguments() != null){}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootview = inflater.inflate(R.layout.fragment_mainactivity,container,false);
        initializeScreen(rootview);

        return rootview;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStorytagListAdapter.cleanup();
    }

    private void initializeScreen(View rootView){

        mStoryTagList = (ListView) rootView.findViewById(R.id.storytag_list);

        ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_STORYTAGS);

        mStorytagListAdapter = new FirebaseListAdapter<StorytagList>(getActivity(),StorytagList.class,R.layout.list_item_storytag,ref) {

            @Override
            protected void populateView(View view, StorytagList model, int position) {
                TextView storyTag = (TextView) view.findViewById(R.id.storytag_data);
                storyTag.setText(model.getMain().get(Constants.FIREBASE_STORYTAG_KEY));
            }
        };

        mStoryTagList.setAdapter(mStorytagListAdapter);
    }
}


