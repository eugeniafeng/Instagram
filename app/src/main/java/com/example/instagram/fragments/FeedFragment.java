package com.example.instagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instagram.R;
import com.example.instagram.adapters.PostsAdapter;
import com.example.instagram.databinding.FragmentFeedBinding;
import com.example.instagram.models.Post;
import com.example.instagram.utils.Constants;
import com.example.instagram.utils.EndlessRecyclerViewScrollListener;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    protected FragmentFeedBinding binding;
    protected EndlessRecyclerViewScrollListener scrollListener;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);

        binding.swipeContainer.setOnRefreshListener(() -> {
            queryPosts();
            binding.swipeContainer.setRefreshing(false);
        });

        // set the adapter on the recycler view
        binding.rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvPosts.setLayoutManager(linearLayoutManager);

        // add scroll listener for endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore(view);
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);

        // query posts from database
        queryPosts();
    }

    protected void queryPosts() {
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
            // clear list and add all new elements, notify adapter
            adapter.clear();
            adapter.addAll(posts);
            scrollListener.resetState();
        });
    }

    protected void loadMore(RecyclerView view) {
        int currentSize = adapter.getItemCount();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Constants.KEY_USER);
        // find the next 20 posts
        query.setSkip(currentSize);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground((posts, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with loading more posts", e);
                return;
            }
            // add all new elements, notify adapter
            allPosts.addAll(posts);

            view.post(() -> adapter.notifyItemRangeInserted(currentSize, allPosts.size()-1));
        });
    }
}