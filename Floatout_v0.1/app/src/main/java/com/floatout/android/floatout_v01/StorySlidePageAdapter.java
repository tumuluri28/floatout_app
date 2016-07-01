package com.floatout.android.floatout_v01;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Yashwanth on 16-06-29.
 */
public class StorySlidePageAdapter extends FragmentStatePagerAdapter {
    private int storyCount;
    private Context context;
    private ArrayList<Bitmap> bm = new ArrayList<>();
    private ArrayList<String> story = new ArrayList<>();

    public StorySlidePageAdapter(FragmentManager fm, Context context, int storyCount, ArrayList<Bitmap>bm, ArrayList<String>story){
        super(fm);
        this.storyCount = storyCount;
        this.context = context;
        this.bm = bm;
        this.story = story;
    }

    @Override
    public Fragment getItem(int position) {
        StoryFeed fragment = new StoryFeed();
        if(!bm.isEmpty()){
            fragment.setImage(bm.get(position));
            //fragment.setStory(story.get(position));
        }
        return fragment;
    }

    @Override
    public int getCount(){
        return storyCount;
    }
}
