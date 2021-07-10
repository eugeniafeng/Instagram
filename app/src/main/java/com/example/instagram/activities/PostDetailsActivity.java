package com.example.instagram.activities;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.instagram.databinding.ActivityPostDetailsBinding;
import com.example.instagram.models.Post;
import com.example.instagram.utils.Constants;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";

    private ActivityPostDetailsBinding binding;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        post = Parcels.unwrap(getIntent().getParcelableExtra(Constants.POST_KEY));
        Log.d(TAG, "Post: " + post.getDescription() +
                ", username: " + post.getUser().getUsername());

        Spanned description = Html.fromHtml(
                "<b>" + post.getUser().getUsername() + "</b>  " + post.getDescription());
        binding.tvDescription.setText(description);
        binding.tvUsername.setText(post.getUser().getUsername());
        binding.tvTimestamp.setText(Post.calculateTimeAgo(post.getCreatedAt()));
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(binding.ivImage);
        }
    }
}