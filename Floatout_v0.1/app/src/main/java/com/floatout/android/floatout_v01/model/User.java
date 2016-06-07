package com.floatout.android.floatout_v01.model;

import java.util.HashMap;

/**
 * Created by Yashwanth on 16-06-07.
 */
public class User {
    private String userName;
    private String email;


    private HashMap<String,Object> timestampJoined;

    public User() {
    }

    public User(String userName, String email, HashMap<String, Object> timestampJoined) {
        this.userName= userName;
        this.email = email;

        this.timestampJoined = timestampJoined;
    }

    public String getUserName() {
        return userName;
    }


    public String getEmail() {
        return email;
    }


    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }
}
