package com.floatout.android.floatout_v01;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StoryFeed extends Fragment {

    ImageView im;
    TextView tv;

    Bitmap bm;
    String story;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story_feed, container, false);
        im = (ImageView) rootView.findViewById(R.id.image);
        tv = (TextView) rootView.findViewById(R.id.story);
        setThings();
        // Inflate the layout for this fragment
        return rootView;
    }

    public void setImage(Bitmap bm){
        this.bm = bm;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public void setThings() {
        im.setImageBitmap(bm);
        tv.setText(story);
    }
}
