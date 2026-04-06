package com.example.studyadviser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;
    private final OnTaskInteractionListener listener;
    private final DBHelper dbHelper;
    private boolean isSelectable = false;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private static final int MODE_UNGROUPED = 0;
    private static final int MODE_GROUPED_BY_CATEGORY = 1;
    private static final int MODE_GROUPED_BY_DEADLINE = 2;
    public int currentMode = MODE_UNGROUPED;

    public interface OnTaskInteractionListener {
        void onTaskClick(Task task);
        void onTaskChecked(Task task, boolean isChecked);
        void onLongTaskClick();
        void onAllSelectionsCleared();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textViewTitle, textViewDeadline, textViewCategory, textViewStatus, textViewOverdue;

        public ItemViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxCompleted);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDeadline = itemView.findViewById(R.id.textViewDeadline);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
        }
    }

    public TaskAdapter(List<Task> tasks, OnTaskInteractionListener listener, DBHelper dbHelper) {
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    public void updateTasksUngrouped(@Nullable List<Task> tasks) {
        this.currentMode = MODE_UNGROUPED;
        items = new ArrayList<>();
        if (tasks != null) {
            List<Task> completedTasks = new ArrayList<>();
            List<Task> uncompletedTasks = new ArrayList<>();

            for (Task task : tasks) {
                if (task.isCompleted()) {
                    completedTasks.add(task);
                } else {
                    uncompletedTasks.add(task);
                }
            }

            Collections.sort(uncompletedTasks, (t1, t2) -> Long.compare(t1.getDeadline(), t2.getDeadline()));

            items.addAll(uncompletedTasks);
            items.addAll(completedTasks);
        }
        notifyDataSetChanged();
    }

    public void updateTasksGroupedByCategory(@Nullable List<Task> tasks) {
        this.currentMode = MODE_GROUPED_BY_CATEGORY;
        items = new ArrayList<>();
        if (tasks == null || tasks.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Map<String, List<Task>> groupedTasks = new HashMap<>();

        for (Task task : tasks) {
            String category = task.getCategory() != null ? task.getCategory() : "Без категории";
            if (!groupedTasks.containsKey(category)) {
                groupedTasks.put(category, new ArrayList<>());
            }
            groupedTasks.get(category).add(task);
        }

        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            List<Task> categoryTasks = entry.getValue();

            List<Task> completedInCategory = new ArrayList<>();
            List<Task> uncompletedInCategory = new ArrayList<>();

            for (Task task : categoryTasks) {
                if (task.isCompleted()) {
                    completedInCategory.add(task);
                } else {
                    uncompletedInCategory.add(task);
                }
            }

            Collections.sort(uncompletedInCategory, (t1, t2) -> Long.compare(t1.getDeadline(), t2.getDeadline()));

            items.add(entry.getKey());

            items.addAll(uncompletedInCategory);
            items.addAll(completedInCategory);
        }

        notifyDataSetChanged();
    }

    public void updateTasksGroupedByDeadline(@Nullable List<Task> tasks) {
        this.currentMode = MODE_GROUPED_BY_DEADLINE;
        items = new ArrayList<>();
        if (tasks == null || tasks.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Map<String, List<Task>> groupedTasks = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        for (Task task : tasks) {
            String date = task.getDeadline() > 0 ? sdf.format(new Date(task.getDeadline())) : "Без даты";
            if (!groupedTasks.containsKey(date)) {
                groupedTasks.put(date, new ArrayList<>());
            }
            groupedTasks.get(date).add(task);
        }

        List<Map.Entry<String, List<Task>>> sortedEntries = new ArrayList<>(groupedTasks.entrySet());
        Collections.sort(sortedEntries, (entry1, entry2) -> {
            try {
                Date date1 = sdf.parse(entry1.getKey());
                Date date2 = sdf.parse(entry2.getKey());
                return date1.compareTo(date2);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });

        for (Map.Entry<String, List<Task>> entry : sortedEntries) {
            List<Task> dateTasks = entry.getValue();

            List<Task> completedInDate = new ArrayList<>();
            List<Task> uncompletedInDate = new ArrayList<>();

            for (Task task : dateTasks) {
                if (task.isCompleted()) {
                    completedInDate.add(task);
                } else {
                    uncompletedInDate.add(task);
                }
            }

            Collections.sort(uncompletedInDate, (t1, t2) -> Long.compare(t1.getDeadline(), t2.getDeadline()));

            items.add(entry.getKey());

            items.addAll(uncompletedInDate);
            items.addAll(completedInDate);
        }

        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_task_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_task, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            String category = (String) items.get(position);
            ((HeaderViewHolder) holder).headerTextView.setText(category);
            return;
        }

        Task task = (Task) items.get(position);
        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        itemHolder.textViewTitle.setText(task.getTitle());
        itemHolder.textViewDeadline.setText(sdf.format(new Date(task.getDeadline())));
        itemHolder.textViewCategory.setText(task.getCategory());
        itemHolder.textViewStatus.setText(getStatusString(task.getStatus()));

        itemHolder.checkBox.setOnCheckedChangeListener(null);
        itemHolder.checkBox.setChecked(task.isCompleted());

        updateTextStyle(itemHolder.textViewTitle, task.isCompleted());

        itemHolder.itemView.setOnClickListener(v -> {
            if (isSelectable) {
                toggleSelection(position);
            } else if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        itemHolder.itemView.setOnLongClickListener(v -> {
            if (!isSelectable) {
                isSelectable = true;
                toggleSelection(position);
                if (listener != null) {
                    listener.onLongTaskClick();
                }
            }
            return true;
        });

        itemHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            task.setStatus(isChecked ? Task.Status.COMPLETED : Task.Status.IN_PROGRESS);
            dbHelper.updateTask(task);

            List<Task> currentTasks = new ArrayList<>();
            for (Object item : items) {
                if (item instanceof Task) {
                    currentTasks.add((Task) item);
                }
            }

            switch (currentMode) {
                case MODE_GROUPED_BY_CATEGORY:
                    updateTasksGroupedByCategory(currentTasks);
                    break;
                case MODE_GROUPED_BY_DEADLINE:
                    updateTasksGroupedByDeadline(currentTasks);
                    break;
                default:
                    updateTasksUngrouped(currentTasks);
            }

            if (listener != null) {
                listener.onTaskChecked(task, isChecked);
            }

            notifyItemChanged(position);
        });

        boolean isOverdue = !task.isCompleted() && task.getDeadline() < System.currentTimeMillis();
        itemHolder.textViewOverdue.setVisibility(isOverdue ? View.VISIBLE : View.GONE);
        itemHolder.textViewDeadline.setTextColor(isOverdue ? Color.RED : 0xFF666666);

        int highlightColor = getHighlightColor(itemHolder.itemView.getContext());
        itemHolder.itemView.setBackgroundColor(selectedPositions.contains(position) ? highlightColor : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateTasks(List<Task> newTasks) {
        notifyDataSetChanged();
    }

    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
        if (!selectable) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        if (getItemViewType(position) == TYPE_HEADER) return;

        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);

        if (selectedPositions.isEmpty() && listener != null) {
            listener.onAllSelectionsCleared();
        }
    }

    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new ArrayList<>();
        for (int position : selectedPositions) {
            if (getItemViewType(position) == TYPE_ITEM) {
                selectedTasks.add((Task) items.get(position));
            }
        }
        return selectedTasks;
    }

    private int getHighlightColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true);
        return typedValue.data;
    }

    private String getStatusString(Task.Status status) {
        switch (status) {
            case PLANNED: return "Запланировано";
            case IN_PROGRESS: return "Выполняется";
            case COMPLETED: return "Завершено";
            default: return "Неизвестно";
        }
    }

    private void updateTextStyle(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(0xFF888888);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            TypedValue typedValue = new TypedValue();
            textView.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            textView.setTextColor(typedValue.data);
        }
    }
}