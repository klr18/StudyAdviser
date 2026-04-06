package com.example.studyadviser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private boolean isEvenWeek = false;
    private TextView weekHeaderTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Мое расписание");
        }

        dbHelper = new DBHelper(getContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        weekHeaderTextView = view.findViewById(R.id.weekHeaderTextView);
        updateWeekHeader();

        Button switchWeekButton = view.findViewById(R.id.switchWeekButton);
        switchWeekButton.setOnClickListener(v -> {
            isEvenWeek = !isEvenWeek;
            updateWeekHeader();
            loadSchedules();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            AddScheduleFragment addScheduleFragment = new AddScheduleFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isEvenWeek", isEvenWeek);
            NavController navController = Navigation.findNavController(view);

            navController.navigate(R.id.action_scheduleFragment_to_addScheduleFragment, bundle);
        });

        loadSchedules();
        return view;
    }

    private void updateWeekHeader() {
        String weekText = isEvenWeek ? "Четная неделя" : "Нечетная неделя";
        weekHeaderTextView.setText(weekText);
    }

    private void loadSchedules() {
        Map<String, List<Schedule>> schedulesByDay = dbHelper.getSchedulesGroupedByDay(isEvenWeek);
        adapter = new ScheduleAdapter(schedulesByDay);
        recyclerView.setAdapter(adapter);
    }
}