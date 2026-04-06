package com.example.studyadviser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddScheduleFragment extends Fragment {
    private DBHelper dbHelper;
    private Spinner dayOfWeekSpinner;
    private boolean isEvenWeek;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_schedule, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Добавление события");
        }

        dbHelper = new DBHelper(getContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            isEvenWeek = bundle.getBoolean("isEvenWeek");
        }

        // Настройка Spinner
        dayOfWeekSpinner = view.findViewById(R.id.dayOfWeekSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        Button addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(v -> {
            EditText eventNameEditText = view.findViewById(R.id.eventNameEditText);
            EditText locationEditText = view.findViewById(R.id.locationEditText);

            String eventName = eventNameEditText.getText().toString();
            String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();
            String location = locationEditText.getText().toString();

            if (!eventName.isEmpty() && !dayOfWeek.isEmpty()) {
                Schedule schedule = new Schedule(eventName, dayOfWeek, location, isEvenWeek); // Сохраняем на текущей неделе
                dbHelper.addSchedule(schedule);
                Toast.makeText(getContext(), "Событие добавлено", Toast.LENGTH_SHORT).show();

                // Возврат к ScheduleFragment
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack();
            } else {
                Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}