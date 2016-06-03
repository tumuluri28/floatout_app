package com.floatout.android.floatout_v01.model;

import java.util.HashMap;

/**
 * Created by Yashwanth on 16-06-02.
 */
public class StorytagList {


    private String name;
    private HashMap<String, String> main;

    public StorytagList() {
    }

//    public StorytagList(String storyName) {
//        this.storyName = storyName;
//    }

    public StorytagList(String name, HashMap<String, String> main){

        this.name = "name";
        this.main = main;
    }

//    public String getStoryTag() {
//        return storyName;
//    }


    public String getName(){
        return name;
    }


    public HashMap<String, String> getMain() {
        return main;
    }

}

