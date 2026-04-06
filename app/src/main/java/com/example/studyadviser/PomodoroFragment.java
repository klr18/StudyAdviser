package com.example.studyadviser;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Locale;

public class PomodoroFragment extends Fragment {

    private static final long DEFAULT_POMODORO_TIME = 25 * 60 * 1000;
    private static final long DEFAULT_BREAK_TIME = 5 * 60 * 1000;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftMillis = DEFAULT_POMODORO_TIME;
    private boolean isStudySession = true;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_POMODORO_SESSIONS = "pomodoroSessions";
    private static final String KEY_TOTAL_TIME = "totalTime";
    private TextView timerTextView, sessionStatusTextView;
    private ProgressBar progressBar;
    private Button startPauseButton, soundToggleButton;
    private Button resetButton;
    private Spinner soundSelectionSpinner;
    private MediaPlayer mediaPlayer;
    private boolean isSoundPlaying = false;
    private UserRewards userRewards;

    private int[] soundResources = {R.raw.cicada_night_forest, R.raw.rain, R.raw.morning};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Режим фокуса");
        }

        timerTextView = view.findViewById(R.id.timerTextView);
        progressBar = view.findViewById(R.id.progressBar);
        startPauseButton = view.findViewById(R.id.startPauseButton);
        resetButton = view.findViewById(R.id.resetButton);
        soundToggleButton = view.findViewById(R.id.soundToggleButton);
        soundSelectionSpinner = view.findViewById(R.id.soundSelectionSpinner);
        sessionStatusTextView = view.findViewById(R.id.sessionStatusTextView);

        restoreState();
        if (isStudySession) {
            sessionStatusTextView.setText("Учеба");
        }
        else{
            sessionStatusTextView.setText("Перерыв");
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.nature_sounds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSelectionSpinner.setAdapter(adapter);

        soundToggleButton.setOnClickListener(v -> toggleSound());

        updateTimerText();

        startPauseButton.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerText();
                progressBar.setProgress((int) (100 * timeLeftMillis / (isStudySession ? DEFAULT_POMODORO_TIME : DEFAULT_BREAK_TIME)));
            }

            @Override
            public void onFinish() {
                if (isStudySession) {
                    int currentSessions = getPomodoroSessions();
                    currentSessions++;
                    long totalTimeMillis = getTotalTimeMillis();
                    totalTimeMillis += DEFAULT_POMODORO_TIME;

                    savePomodoroData(currentSessions, totalTimeMillis);

                    isStudySession = false;
                    timeLeftMillis = DEFAULT_BREAK_TIME;
                    sessionStatusTextView.setText("Перерыв");
                    userRewards = new UserRewards(requireContext());

                    userRewards.rewardPomodoroSession(requireContext());

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateHeader(userRewards.getPoints());
                    }

                    userRewards.updateLevel(requireContext());

                } else {
                    int currentSessions = getPomodoroSessions();
                    long totalTimeMillis = getTotalTimeMillis();
                    totalTimeMillis += DEFAULT_BREAK_TIME;
                    savePomodoroData(currentSessions, totalTimeMillis);

                    isStudySession = true;
                    timeLeftMillis = DEFAULT_POMODORO_TIME;
                    sessionStatusTextView.setText("Учеба");
                }

                if (!isTimerRunning) {
                    return;
                }
                startTimer();
            }
        }.start();

        isTimerRunning = true;
        startPauseButton.setText("Пауза");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        startPauseButton.setText("Старт");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftMillis = DEFAULT_POMODORO_TIME;
        updateTimerText();
        startPauseButton.setText("Старт");
        progressBar.setProgress(100);
        sessionStatusTextView.setText("Учеба");
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftMillis / 1000) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void toggleSound() {
        int selectedSoundIndex = soundSelectionSpinner.getSelectedItemPosition();

        if (isSoundPlaying) {
            mediaPlayer.pause();
            isSoundPlaying = false;
            soundToggleButton.setText("Включить звук природы");
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(getContext(), soundResources[selectedSoundIndex]);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            isSoundPlaying = true;
            soundToggleButton.setText("Выключить звуки природы");
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isTimerRunning = false;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    private void restoreState() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("PomodoroPrefs", MODE_PRIVATE);
        timeLeftMillis = prefs.getLong("timeLeftMillis", DEFAULT_POMODORO_TIME);
        isTimerRunning = prefs.getBoolean("isTimerRunning", false);
        isStudySession = prefs.getBoolean("isStudySession", true);

        updateTimerText();

        if (isTimerRunning) {
            startTimer();
        }
    }

    private void saveState() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("PomodoroPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("timeLeftMillis", timeLeftMillis);
        editor.putBoolean("isTimerRunning", isTimerRunning);
        editor.putBoolean("isStudySession", isStudySession);
        editor.apply();
    }

    private void savePomodoroData(int sessions, long totalTimeMillis) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_POMODORO_SESSIONS, sessions);
        editor.putLong(KEY_TOTAL_TIME, totalTimeMillis);
        editor.apply();
    }

    private int getPomodoroSessions() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_POMODORO_SESSIONS, 0);
    }

    private long getTotalTimeMillis() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getLong(KEY_TOTAL_TIME, 0);
    }
}
