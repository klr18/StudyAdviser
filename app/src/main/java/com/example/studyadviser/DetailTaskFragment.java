package com.example.studyadviser;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailTaskFragment extends Fragment {
    private TextView tvTitle, tvDateTime, tvCategory, tvDescription, tvStatus;;
    private Button btnEdit, btnDelete;
    private DBHelper dbHelper;
    private Task currentTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_task, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Просмотр задачи");
        }

        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvDateTime = view.findViewById(R.id.tvDateTime);
        tvCategory = view.findViewById(R.id.tvCategory);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);

        dbHelper = new DBHelper(requireContext());

        int taskId = getArguments().getInt("taskId");
        currentTask = dbHelper.getTaskById(taskId);

        if(currentTask != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            tvTitle.setText(currentTask.getTitle());
            tvDateTime.setText(sdf.format(new Date(currentTask.getDeadline())));
            tvCategory.setText(currentTask.getCategory());
            if (currentTask.getDescription() != null && !currentTask.getDescription().isEmpty()) {
                tvDescription.setText(currentTask.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            tvStatus.setText(getStatusString(currentTask.getStatus()));
        }

        setupButtons();
        return view;
    }

    private String getStatusString(Task.Status status) {
        switch (status) {
            case PLANNED:
                return "Запланировано";
            case IN_PROGRESS:
                return "Выполняется";
            case COMPLETED:
                return "Завершено";
            default:
                return "Неизвестно";
        }
    }

    private void setupButtons() {
        btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("taskId", currentTask.getId());
            Navigation.findNavController(v).navigate(R.id.action_detailTask_to_addTaskFragment, args);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление задачи")
                .setMessage("Вы уверены, что хотите удалить эту задачу?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    dbHelper.deleteTask(currentTask.getId());
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}