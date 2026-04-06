package com.example.studyadviser;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class UserRewards {
    private static final String PREFS_NAME = "UserRewardsPrefs";
    private static final String KEY_POINTS = "points";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_LAST_LOGIN = "last_login";
    private SharedPreferences sharedPreferences;

    public UserRewards(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getPoints() {
        return sharedPreferences.getInt(KEY_POINTS, 0);
    }

    public void setPoints(int points) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_POINTS, points);
        editor.apply();
    }

    public int getLevel() {
        return sharedPreferences.getInt(KEY_LEVEL, 1);
    }

    public void setLevel(int level) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LEVEL, level);
        editor.apply();
    }

    public long getLastLoginTime() {
        return sharedPreferences.getLong(KEY_LAST_LOGIN, 0);
    }

    public void setLastLoginTime(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_LOGIN, timestamp);
        editor.apply();
    }

    public void rewardLogin(Context context) {
        long currentTime = System.currentTimeMillis();
        long lastLogin = getLastLoginTime();

        if (isNewDay(lastLogin, currentTime)) {
            setPoints(getPoints() + 10);
            setLastLoginTime(currentTime);
            showToast(context, "Начислено 10 баллов за вход!");
            updateLevel(context);
        }
    }

    private boolean isNewDay(long lastLogin, long currentTime) {
        return (currentTime - lastLogin) > (24 * 60 * 60 * 1000);
    }

    public void rewardTaskCompletion(Context context) {
        setPoints(getPoints() + 20);
        showToast(context, "Начислено 20 баллов за выполненную задачу!");
        updateLevel(context);
    }

    public void removeTaskCompletion(Context context) {
        setPoints(getPoints() - 20);
        showToast(context, "Списано 20 баллов за удаление задачи!");
    }

    public void rewardPomodoroSession(Context context) {
        setPoints(getPoints() + 15);
        showToast(context, "Начислено 15 баллов за сессию Помодоро!");
        updateLevel(context);
    }

    public void updateLevel(Context context) {
        int points = getPoints();
        int level = getLevel();
        int newLevel = calculateLevel(points);

        if (newLevel > level) {
            showToast(context, "Поздравляем! Вы достигли уровня: " + getLevelName(newLevel));
            setLevel(newLevel);
        }
    }

    public int calculateLevel(int points) {
        if (points >= 3000) return 9;
        if (points >= 2000) return 8;
        if (points >= 1500) return 7;
        if (points >= 1000) return 6;
        if (points >= 750) return 5;
        if (points >= 500) return 4;
        if (points >= 250) return 3;
        if (points >= 100) return 2;
        return 1;
    }

    public String getLevelName(int level) {
        switch (level) {
            case 1: return "Новичок";
            case 2: return "Ученик";
            case 3: return "Знаток";
            case 4: return "Мастер";
            case 5: return "Эксперт";
            case 6: return "Гений";
            case 7: return "Грандмастер";
            case 8: return "Легенда";
            case 9: return "Повелитель времени";
            default: return "Неизвестный уровень";
        }
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
