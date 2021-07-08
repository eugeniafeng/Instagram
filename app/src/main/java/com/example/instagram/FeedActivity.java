package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.example.instagram.databinding.ActivityFeedBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";

    private ActivityFeedBinding binding;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts);

        binding.swipeContainer.setOnRefreshListener(() -> {
            queryPosts();
            binding.swipeContainer.setRefreshing(false);
        });

        // set the adapter on the recycler view
        binding.rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(this));
        // query posts from database
        queryPosts();
    }

    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Constants.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground((posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }

            // for debugging purposes let's print every post description to logcat
            for (Post post : posts) {
                Log.i(TAG, "Post: " + post.getDescription() +
                        ", username: " + post.getUser().getUsername());
            }

            // clear list and add all new elements, notify adapter
            adapter.clear();
            adapter.addAll(posts);
        });
    }
}