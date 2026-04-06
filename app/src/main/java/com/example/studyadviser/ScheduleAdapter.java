package com.example.studyadviser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyadviser.R;

import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Map<String, List<Schedule>> schedulesByDay;

    public ScheduleAdapter(Map<String, List<Schedule>> schedulesByDay) {
        this.schedulesByDay = schedulesByDay;
    }

    @Override
    public int getItemViewType(int position) {
        int count = 0;
        for (Map.Entry<String, List<Schedule>> entry : schedulesByDay.entrySet()) {
            if (position == count) {
                return TYPE_HEADER;
            }
            count++;
            if (position < count + entry.getValue().size()) {
                return TYPE_ITEM;
            }
            count += entry.getValue().size();
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_day_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_schedule, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int count = 0;
        for (Map.Entry<String, List<Schedule>> entry : schedulesByDay.entrySet()) {
            if (position == count) {
                ((HeaderViewHolder) holder).bind(entry.getKey());
                return;
            }
            count++;

            if (position < count + entry.getValue().size()) {
                Schedule schedule = entry.getValue().get(position - count);
                ((ItemViewHolder) holder).bind(schedule);
                return;
            }
            count += entry.getValue().size();
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Map.Entry<String, List<Schedule>> entry : schedulesByDay.entrySet()) {
            count += 1 + entry.getValue().size();
        }
        return count;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dayHeaderTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dayHeaderTextView = itemView.findViewById(R.id.dayHeaderTextView);
        }

        public void bind(String day) {
            dayHeaderTextView.setText(day);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventTimeTextView;
        TextView locationTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }

        public void bind(Schedule schedule) {
            eventNameTextView.setText(schedule.getEventName());
            eventTimeTextView.setText(schedule.getEventDate());
            locationTextView.setText(schedule.getLocation());
        }
    }
}