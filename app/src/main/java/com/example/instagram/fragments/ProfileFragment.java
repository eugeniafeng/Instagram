package com.example.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.adapters.ProfileAdapter;
import com.example.instagram.databinding.FragmentProfileBinding;
import com.example.instagram.models.Post;
import com.example.instagram.utils.Constants;
import com.example.instagram.utils.EndlessRecyclerViewScrollListener;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    protected EndlessRecyclerViewScrollListener scrollListener;
    protected List<Post> allPosts;
    private FragmentProfileBinding binding;
    private ProfileAdapter adapter;
    private ParseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPosts);
        user = ParseUser.getCurrentUser();

        binding.swipeContainer.setOnRefreshListener(() -> {
            queryPosts();
            binding.swipeContainer.setRefreshing(false);
        });

        // set the adapter on the recycler view
        binding.rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.rvPosts.setLayoutManager(gridLayoutManager);

        // add scroll listener for endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore(view);
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);

        binding.tvUsername.setText(user.getUsername());

        // query posts from database
        queryPosts();
    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Constants.KEY_USER);
        query.whereEqualTo(Constants.KEY_USER, user);
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

    protected void loadMore(RecyclerView view) {
        int currentSize = adapter.getItemCount();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Constants.KEY_USER);
        query.whereEqualTo(Constants.KEY_USER, user);
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
            view.post(() -> adapter.notifyItemRangeInserted(currentSize, allPosts.size() - 1));
        });
    }
}
