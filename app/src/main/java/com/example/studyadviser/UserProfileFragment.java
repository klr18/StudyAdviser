package com.example.studyadviser;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class UserProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView userNameTextView;
    private TextView userStatusTextView;
    private TextView statsTextView;
    private Button editProfileButton;
    private Button changeThemeButton;
    private Button viewAchievementsButton;
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_STATUS = "userStatus";

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_THEME = "isDarkTheme";
    private static final String KEY_PROFILE_IMAGE_URI = "profileImageUri";
    private static final String KEY_POMODORO_SESSIONS = "pomodoroSessions";
    private static final String KEY_TOTAL_TIME = "totalTime";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        userNameTextView = view.findViewById(R.id.user_name);
        userStatusTextView = view.findViewById(R.id.user_status);
        statsTextView = view.findViewById(R.id.stats_text);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        changeThemeButton = view.findViewById(R.id.change_theme_button);

        setUserData();

        changeThemeButton.setOnClickListener(v -> {
            boolean isDarkTheme = getCurrentThemePreference();

            boolean newTheme = !isDarkTheme;
            setNightMode(newTheme);

            savePreference(KEY_THEME, newTheme);
        });

        editProfileButton.setOnClickListener(v -> {
            openEditProfileActivity();
        });

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int pomodoroSessions = prefs.getInt(KEY_POMODORO_SESSIONS, 0);
        long totalTimeMillis = prefs.getLong(KEY_TOTAL_TIME, 0);

        long totalTimeHours = totalTimeMillis / (1000 * 60 * 60);
        long totalTimeMinutes = (totalTimeMillis % (1000 * 60 * 60)) / (1000 * 60);

        String statsText = "Сессий помодоро: " + pomodoroSessions + "\nОбщее время: "
                + totalTimeHours + " часов " + totalTimeMinutes + " минут";

        statsTextView.setText(statsText);

        return view;
    }

    private void setUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = prefs.getString(KEY_USER_NAME, "Имя");
        String userStatus = prefs.getString(KEY_USER_STATUS, "Статус");
        String profileImageUri = prefs.getString(KEY_PROFILE_IMAGE_URI, null);

        userNameTextView.setText(userName);
        userStatusTextView.setText(userStatus);

        if (profileImageUri != null) {
            Uri imageUri = Uri.parse(profileImageUri);
            profileImage.setImageURI(imageUri);
        }
    }

    private boolean getCurrentThemePreference() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        return prefs.getBoolean(KEY_THEME, false);
    }

    private void setNightMode(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        requireActivity().recreate();
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void openEditProfileActivity() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_userProfile_to_editProfile);
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userName = prefs.getString(KEY_USER_NAME, "Имя");
        String userStatus = prefs.getString(KEY_USER_STATUS, "Статус");

        userNameTextView.setText(userName);
        userStatusTextView.setText(userStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}


