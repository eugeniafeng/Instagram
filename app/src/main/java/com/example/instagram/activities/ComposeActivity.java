package com.example.instagram.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.utils.BitmapScaler;
import com.example.instagram.databinding.ActivityComposeBinding;
import com.example.instagram.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ComposeActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityComposeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComposeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    Toast.makeText(ComposeActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_compose:
                    Toast.makeText(ComposeActivity.this, "Compose!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_profile:
                default:
                    Toast.makeText(ComposeActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
    }
}