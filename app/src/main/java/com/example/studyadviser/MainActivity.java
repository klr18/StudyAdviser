package com.example.studyadviser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_THEME = "isDarkTheme";
    private UserRewards userRewards;
    private TextView pointsTextView;
    private TextView sectionTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSavedTheme();

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        userRewards = new UserRewards(this);

        pointsTextView = findViewById(R.id.tvPoints);
        sectionTitleTextView = findViewById(R.id.tvSectionTitle);

        userRewards.rewardLogin(this);

        updateHeader("Мои задачи", userRewards.getPoints());

        userRewards.updateLevel(this);
    }

    public void updateHeader(String title, int points) {
        if (sectionTitleTextView != null) {
            sectionTitleTextView.setText(title);
        }
        if (pointsTextView != null) {
            pointsTextView.setText("Баллы: " + points + "\nУровень: " + userRewards.getLevelName(userRewards.calculateLevel(points)));
        }

    }

    public void updateHeader(String title) {
        if (sectionTitleTextView != null) {
            sectionTitleTextView.setText(title);
        }
    }

    public void updateHeader(int points) {
        if (pointsTextView != null) {
            pointsTextView.setText("Баллы: " + points + "\nУровень: " + userRewards.getLevelName(userRewards.calculateLevel(points)));
        }
    }

    private void loadSavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = prefs.getBoolean(KEY_THEME, false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}