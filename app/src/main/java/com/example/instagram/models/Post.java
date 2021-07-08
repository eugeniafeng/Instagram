package com.example.instagram.models;

import com.example.instagram.utils.Constants;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

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

}
