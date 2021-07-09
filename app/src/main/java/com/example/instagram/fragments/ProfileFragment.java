package com.example.instagram.fragments;

import android.util.Log;

import com.example.instagram.models.Post;
import com.example.instagram.utils.Constants;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProfileFragment extends FeedFragment {

    private static final String TAG = "ProfileFragment";

    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Constants.KEY_USER);
        query.whereEqualTo(Constants.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            // clear list and add all new elements, notify adapter
            adapter.clear();
            adapter.addAll(posts);
            scrollListener.resetState();
        });
    }

}
