package com.example.studyadviser;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TasksListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DBHelper dbHelper;
    private List<Task> tasks = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabDelete;
    private boolean isSelectable = false;
    private UserRewards userRewards;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks_list, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateHeader("Мои задачи");
        }

        FloatingActionButton fabGrouping = view.findViewById(R.id.fab_menu);

        fabGrouping.setOnClickListener(v -> showGroupingMenu());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DBHelper(getContext());

        setupAdapter();
        updateTaskList();
        unGroup();

        fabAdd = view.findViewById(R.id.fab_add);
        fabDelete = view.findViewById(R.id.fab_delete);

        fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_to_add_task));

        fabDelete.setOnClickListener(v -> {
            if (isSelectable) {
                deleteSelectedTasks();
            }
        });

        return view;
    }

    private void setupAdapter() {
        adapter = new TaskAdapter(tasks, new TaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onTaskClick(Task task) {
                if (!isSelectable) {
                    Bundle args = new Bundle();
                    args.putInt("taskId", task.getId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_tasksList_to_detail, args);
                }
            }

            @Override
            public void onTaskChecked(Task task, boolean isChecked) {
                task.setCompleted(isChecked);
                dbHelper.updateTask(task);
                userRewards = new UserRewards(requireContext());

                if(!isChecked){
                    userRewards.removeTaskCompletion(requireContext());
                    userRewards.updateLevel(requireContext());

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateHeader(userRewards.getPoints());
                    }
                }
                else{
                    userRewards.rewardTaskCompletion(requireContext());
                    userRewards.updateLevel(requireContext());

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateHeader(userRewards.getPoints());
                    }
                }


                updateTaskList();
            }

            @Override
            public void onAllSelectionsCleared() {
                resetSelectionMode();
            }

            @Override
            public void onLongTaskClick() {
                isSelectable = true;
                fabDelete.setVisibility(View.VISIBLE);
                fabAdd.setVisibility(View.GONE);
            }
        }, dbHelper);
        recyclerView.setAdapter(adapter);
    }

    private void deleteSelectedTasks() {
        List<Task> selectedTasks = adapter.getSelectedTasks();

        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление задач")
                .setMessage("Вы уверены, что хотите удалить выбранные задачи?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    for (Task task : selectedTasks) {
                        dbHelper.deleteTask(task.getId());
                    }
                    resetSelectionMode();
                    updateTaskList();
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    resetSelectionMode();
                })
                .show();
    }

    private void updateTaskList() {
        tasks.clear();
        tasks.addAll(dbHelper.getAllTasksSorted());
        if (adapter != null) {
            switch (adapter.currentMode) {
                case 1:
                    adapter.updateTasksGroupedByCategory(tasks);
                    break;
                case 2:
                    adapter.updateTasksGroupedByDeadline(tasks);
                    break;
                default:
                    adapter.updateTasksUngrouped(tasks);
            }
        }
    }

    private void resetSelectionMode() {
        isSelectable = false;
        adapter.setSelectable(false);
        fabDelete.setVisibility(View.GONE);
        fabAdd.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectable) {
                    resetSelectionMode();
                } else {
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
        updateTaskList();
    }

    private void groupByCategory() {
        List<Task> tasks = dbHelper.getAllTasks();
        adapter.updateTasksGroupedByCategory(tasks);
    }

    private void groupByDeadline() {
        List<Task> tasks = dbHelper.getAllTasks();
        adapter.updateTasksGroupedByDeadline(tasks);
    }

    private void unGroup() {
        List<Task> tasks = dbHelper.getAllTasks();
        adapter.updateTasksUngrouped(tasks);
    }

    private void showGroupingMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), getView().findViewById(R.id.fab_menu));
        popupMenu.getMenuInflater().inflate(R.menu.menu_grouping, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.group_by_category) {
                groupByCategory();
                return true;
            } else if (item.getItemId() == R.id.group_by_deadline) {
                groupByDeadline();
                return true;
            } else if (item.getItemId() == R.id.clear_grouping) {
                unGroup();
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }
}
