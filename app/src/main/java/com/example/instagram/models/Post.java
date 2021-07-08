package com.example.instagram.models;

import android.util.Log;

import com.example.instagram.utils.Constants;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@ParseClassName("Post")
public class Post extends ParseObject {

    public String getDescription() {
        return getString(Constants.KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(Constants.KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(Constants.KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(Constants.KEY_IMAGE, parseFile);
    }

    public ParseUser getUser() {
        return getParseUser(Constants.KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(Constants.KEY_USER, user);
    }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        long DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minute ago";
            } else if (diff < 60 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 2 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 2 * DAY_MILLIS) {
                return diff / DAY_MILLIS + " day ago";
            } else if (diff < 30 * DAY_MILLIS) {
                return diff / DAY_MILLIS + " days ago";
            } else {
                Calendar current = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(createdAt);
                if (current.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                    return then.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " "
                            + then.get(Calendar.DAY_OF_MONTH);
                } else {
                    return then.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " "
                            + then.get(Calendar.DAY_OF_MONTH)
                            + ", " + (then.get(Calendar.YEAR));
                }
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
