package com.example.instagram.utils;

import android.app.Application;

import com.example.instagram.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("U9yNareubZ9hjney5YyLaPqwK11OvJ8Hf0Dqca1e")
                .clientKey("s01KBxqIQEL7QldLFvrnT9OXQJGb3AOOCtbwvUGK")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
