package com.example.instagram.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import android.view.View;
import android.widget.Toast;

import com.example.instagram.utils.BitmapScaler;
import com.example.instagram.databinding.ActivityMainBinding;
import com.example.instagram.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final int IMAGE_WIDTH = 500;
    private static final String PAUSE_KEY = "pause";

    private ActivityMainBinding binding;
    private File photoFile;
    private String photoFileName = "photo.jpg";
    private String photoFileNameResized = "photo_resized.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if user rotates the phone, restores the image view
        if (savedInstanceState != null && savedInstanceState.getInt(PAUSE_KEY) == 1) {
            photoFile = getPhotoFileUri(photoFileNameResized);
            Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
            binding.ivPostImage.setImageBitmap(takenImage);
        }

        binding.btnCaptureImage.setOnClickListener(v -> launchCamera());

        binding.btnSubmit.setOnClickListener(v -> {
            String description = binding.etDescription.getText().toString();
            if (description.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "Description cannot be empty",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (photoFile == null || binding.ivPostImage.getDrawable() == null) {
                Toast.makeText(
                        MainActivity.this,
                        "There is no image!",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            ParseUser currentUser = ParseUser.getCurrentUser();
            savePost(description, currentUser, photoFile);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // set photoFile again in case it is reset by changed orientation
                photoFile = getPhotoFileUri(photoFileName);
                // by this point we have the camera photo on disk
                // rotate so saves in correct orientation
                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                // RESIZE BITMAP
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, IMAGE_WIDTH);
                // Write smaller bitmap back to disk
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap
                photoFile = getPhotoFileUri(photoFileNameResized);
                try {
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    // Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    Log.e("Error creating file ", e.toString());
                }
                // Load the taken image into a preview
                binding.ivPostImage.setImageBitmap(resizedBitmap);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PAUSE_KEY, 1);
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ?
                Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(
                MainActivity.this,
                "com.codepath.fileprovider.instagram",
                photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Error while saving", e);
                Toast.makeText(
                        MainActivity.this,
                        "Error while saving!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            Log.i(TAG, "Post save was successful!");
            binding.etDescription.setText("");
            binding.ivPostImage.setImageResource(0);
        });
    }

    public void onLogoutButton (View view) {
        ParseUser.logOut();
        // Return to sign up/log in and dismiss the main activity so user cannot use back button
        Intent i = new Intent(this, LoginSignupActivity.class);
        startActivity(i);
        finish();
    }

    public void onFeedButton (View view) {
        // Navigate to FeedActivity
        Intent i = new Intent(this, FeedActivity.class);
        startActivity(i);
    }
}