package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.instagram.R;
import com.example.instagram.databinding.ActivityMainBinding;
import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.FeedFragment;
import com.example.instagram.fragments.ProfileFragment;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    fragment = new FeedFragment();
                    break;
                case R.id.action_compose:
                    fragment = new ComposeFragment();
                    break;
                case R.id.action_profile:
                default:
                    fragment = new ProfileFragment();
                    break;
            }
            fragmentManager.beginTransaction()
                    .replace(binding.flContainer.getId(), fragment)
                    .commit();
            return true;
        });
        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ParseUser.logOut();
        // Return to sign up/log in and dismiss the main activity so user cannot use back button
        Intent i = new Intent(this, LoginSignupActivity.class);
        startActivity(i);
        finish();
        return super.onOptionsItemSelected(item);
    }
}