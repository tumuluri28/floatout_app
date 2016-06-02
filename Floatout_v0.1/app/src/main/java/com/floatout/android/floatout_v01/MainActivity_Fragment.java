package com.floatout.android.floatout_v01;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Yashwanth on 16-06-01.
 */
public class MainActivity_Fragment extends Fragment {

    private ListView mHashTagList;
    ArrayAdapter<String> mHashtagData;

    public MainActivity_Fragment() {
    }

    public static MainActivity_Fragment newInstance(){
        MainActivity_Fragment fragment = new MainActivity_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootview = inflater.inflate(R.layout.fragment_mainactivity,container,false);
        initializeScreen(rootview);

        return rootview;
    }





    @Override
    public void onDestroy() {super.onDestroy();}

    private void initializeScreen(View rootView){
        String[] data = {
                "Hash1",
                "Hash2",
                "Hash3",
                "Hash4",
                "Hash5",

        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));


        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mHashtagData =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_hashtag, // The name of the layout ID.
                        R.id.hashtag_data, // The ID of the textview to populate.
                        weekForecast);
        mHashTagList = (ListView) rootView.findViewById(R.id.hashtag_list);
        mHashTagList.setAdapter(mHashtagData);
    }
}


