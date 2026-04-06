package com.example.studyadviser;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class EditProfileFragment extends Fragment {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_STATUS = "userStatus";
    private static final String KEY_PROFILE_IMAGE_URI = "profileImageUri";

    private EditText userNameEditText;
    private EditText userStatusEditText;
    private Button saveButton;
    private ImageView profileImageView;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedImageUri); // Установить выбранное изображение
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        userNameEditText = view.findViewById(R.id.user_name_edit);
        userStatusEditText = view.findViewById(R.id.user_status_edit);
        saveButton = view.findViewById(R.id.save_button);
        profileImageView = view.findViewById(R.id.profile_image_edit);

        loadUserData();

        profileImageView.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> {
            saveUserData();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        String userName = prefs.getString(KEY_USER_NAME, "");
        String userStatus = prefs.getString(KEY_USER_STATUS, "");
        String profileImageUri = prefs.getString(KEY_PROFILE_IMAGE_URI, null);

        userNameEditText.setText(userName);
        userStatusEditText.setText(userStatus);

        if (profileImageUri != null) {
            selectedImageUri = Uri.parse(profileImageUri);
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    private void saveUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, userNameEditText.getText().toString());
        editor.putString(KEY_USER_STATUS, userStatusEditText.getText().toString());

        if (selectedImageUri != null) {
            editor.putString(KEY_PROFILE_IMAGE_URI, selectedImageUri.toString());
        }

        editor.apply();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }
}
