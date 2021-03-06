package com.example.instagram.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.instagram.databinding.FragmentComposeBinding;
import com.example.instagram.models.Post;
import com.example.instagram.utils.BitmapScaler;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class ComposeFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final int IMAGE_WIDTH = 500;
    private static final String PAUSE_KEY = "pause";
    private static final String TAG = "ComposeFragment";
    private static final String photoFileName = "photo.jpg";
    private static final String photoFileNameResized = "photo_resized.jpg";

    private FragmentComposeBinding binding;
    private File photoFile;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentComposeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                        getContext(),
                        "Description cannot be empty",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (photoFile == null || binding.ivPostImage.getDrawable() == null) {
                Toast.makeText(
                        getContext(),
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // set photoFile again in case it is reset by changed orientation
                photoFile = getPhotoFileUri(photoFileName);
                // by this point we have the camera photo on disk
                // rotate so saves in correct orientation
                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                takenImage = cropSquare(takenImage);
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
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // marker for if the image view has an image in it
        int pause_val = binding.ivPostImage.getDrawable() == null ? 0 : 1;
        outState.putInt(PAUSE_KEY, pause_val);
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
        matrix.setRotate(rotationAngle,
                (float) bm.getWidth() / 2,
                (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm,
                0,
                0,
                bounds.outWidth,
                bounds.outHeight,
                matrix,
                true);
    }

    private Bitmap cropSquare(Bitmap bitmap) {
        // From https://stackoverflow.com/questions/6908604/android-crop-center-of-bitmap
        int minLength = Math.min(bitmap.getHeight(), bitmap.getWidth());
        return Bitmap.createBitmap(bitmap, 0, 0, minLength, minLength);
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
                getContext(),
                "com.codepath.fileprovider.instagram",
                photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(
                getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
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
                        getContext(),
                        "Error while saving!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            Log.i(TAG, "Post save was successful!");
            binding.etDescription.setText("");
            binding.ivPostImage.setImageResource(0);
        });
    }
}