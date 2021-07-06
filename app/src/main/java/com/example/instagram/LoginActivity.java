package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.instagram.databinding.ActivityLoginBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        binding.btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            loginUser(username, password);
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                // TODO: better error handling
                Log.e(TAG, "Issue with login", e);
                Toast.makeText(
                        LoginActivity.this,
                        "Issue with login!",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            goMainActivity();
            Toast.makeText(
                    LoginActivity.this,
                    "Success!",
                    Toast.LENGTH_SHORT)
                    .show();
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}