package com.example.instagram;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("U9yNareubZ9hjney5YyLaPqwK11OvJ8Hf0Dqca1e")
                .clientKey("s01KBxqIQEL7QldLFvrnT9OXQJGb3AOOCtbwvUGK")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
