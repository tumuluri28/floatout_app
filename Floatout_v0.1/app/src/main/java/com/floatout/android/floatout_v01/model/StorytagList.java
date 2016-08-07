package com.floatout.android.floatout_v01.model;

import java.util.HashMap;

/**
 * Created by Yashwanth on 16-06-02.
 */
public class StorytagList {

    private HashMap<String, String> main;
    private int views;

    public StorytagList() {
    }

    public StorytagList(HashMap<String,String> main, int views){

        this.main= main;
        this.views = views;
    }

    public HashMap<String, String> getMain() {
        return main;
    }
}

