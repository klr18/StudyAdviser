package com.example.studyadviser;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddTaskFragment extends Fragment {
    private EditText editTextTitle, editTextDescription;
    private TextView textDateTime;
    private Spinner spinnerCategory, spinnerStatus;
    private Calendar calendar;
    private DBHelper dbHelper;
    private boolean isEditMode = false;
    private int taskId = -1;
    private UserRewards userRewards;
    private String oldStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        textDateTime = view.findViewById(R.id.textDateTime);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        dbHelper = new DBHelper(getContext());
        calendar = Calendar.getInstance();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Добавление задачи");
        }

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.status_array_display, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        if (getArguments() != null) {
            taskId = getArguments().getInt("taskId", -1);
            if (taskId != -1) {
                isEditMode = true;
                loadTaskData(taskId);
            }
        }

        Button buttonDate = view.findViewById(R.id.buttonDate);
        Button buttonTime = view.findViewById(R.id.buttonTime);
        buttonDate.setOnClickListener(v -> showDatePicker());
        buttonTime.setOnClickListener(v -> showTimePicker());

        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> saveTask());
        if (!isEditMode) {
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 10);
        }

        updateDateTimeDisplay();

        return view;
    }

    private void loadTaskData(int id) {
        Task task = dbHelper.getTaskById(id);
        if (task != null) {
            editTextTitle.setText(task.getTitle());
            editTextDescription.setText(task.getDescription());
            calendar.setTimeInMillis(task.getDeadline());
            updateDateTimeDisplay();

            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCategory.getAdapter();
            int position = adapter.getPosition(task.getCategory());
            spinnerCategory.setSelection(position);

            String statusValue = task.getStatus().toString();
            String[] statusValues = getResources().getStringArray(R.array.status_array_values);
            for (int i = 0; i < statusValues.length; i++) {
                if (statusValues[i].equals(statusValue)) {
                    spinnerStatus.setSelection(i);
                    oldStatus = statusValue;
                    break;
                }
            }
        }
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        long deadline = calendar.getTimeInMillis();
        String category = spinnerCategory.getSelectedItem().toString();

        int selectedStatusPosition = spinnerStatus.getSelectedItemPosition();
        String[] statusValues = getResources().getStringArray(R.array.status_array_values);
        String status = statusValues[selectedStatusPosition];

        if (title.isEmpty()) {
            showError("Введите название задачи");
            return;
        }

        if (!isEditMode && deadline < System.currentTimeMillis()) {
            showError("Нельзя выбрать прошедшее время");
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);
        task.setCategory(category);
        task.setStatus(Task.Status.valueOf(status));

        userRewards = new UserRewards(requireContext());

        if(Objects.equals(status, "COMPLETED")){
            userRewards.rewardTaskCompletion(requireContext());
            userRewards.updateLevel(requireContext());

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateHeader(userRewards.getPoints());
            }
        }
        else if(isEditMode && Objects.equals(oldStatus, "COMPLETED")){
            userRewards.removeTaskCompletion(requireContext());
            userRewards.updateLevel(requireContext());

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateHeader(userRewards.getPoints());
            }
        }

        task.setCompleted(task.getStatus() == Task.Status.COMPLETED);

        if (isEditMode) {
            task.setId(taskId);
            dbHelper.updateTask(task);

        } else {
            dbHelper.addTask(task);
        }

        Navigation.findNavController(requireView()).popBackStack();
    }

    private void updateDateTimeDisplay() {
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(calendar.getTime());
        textDateTime.setText(date);
    }

    private void showError(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        if (isToday(calendar)) {
            Calendar now = Calendar.getInstance();
            timePicker.updateTime(
                    Math.max(now.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.HOUR_OF_DAY)),
                    Math.max(now.get(Calendar.MINUTE), calendar.get(Calendar.MINUTE))
            );
        }
        timePicker.show();
    }

    private boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }
}