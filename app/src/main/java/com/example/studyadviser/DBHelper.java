package com.example.studyadviser;

import static java.security.AccessController.getContext;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DEADLINE = "deadline";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_STATUS = "status";
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COLUMN_SCHEDULE_ID = "schedule_id";
    private static final String COLUMN_EVENT_NAME = "event_name";
    private static final String COLUMN_EVENT_DATE = "event_date";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_IS_EVEN_WEEK = "is_even_week";
    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DEADLINE + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_STATUS + " TEXT DEFAULT 'PLANNED')";
        String CREATE_SCHEDULE_TABLE = "CREATE TABLE " + TABLE_SCHEDULE + "("
                + COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_NAME + " TEXT,"
                + COLUMN_EVENT_DATE + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_IS_EVEN_WEEK + " INTEGER)";
        db.execSQL(CREATE_TASKS_TABLE);
        db.execSQL(CREATE_SCHEDULE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DEADLINE, task.getDeadline());
        values.put(COLUMN_CATEGORY, task.getCategory());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_STATUS, task.getStatusAsString());
        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setDeadline(cursor.getLong(2));
                task.setCategory(cursor.getString(3));
                task.setCompleted(cursor.getInt(4) == 1);
                task.setDescription(cursor.getString(5));
                task.setStatusFromString(cursor.getString(6));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DEADLINE, COLUMN_CATEGORY, COLUMN_DESCRIPTION, COLUMN_STATUS, COLUMN_IS_COMPLETED},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task();
            task.setId(cursor.getInt(0));
            task.setTitle(cursor.getString(1));
            task.setDeadline(cursor.getLong(2));
            task.setCategory(cursor.getString(3));
            task.setDescription(cursor.getString(4));

            String statusStr = cursor.getString(5);
            try {
                task.setStatus(Task.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                task.setStatus(Task.Status.PLANNED);
            }

            task.setCompleted(cursor.getInt(6) == 1);
            cursor.close();
            return task;
        }
        return null;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DEADLINE, task.getDeadline());
        values.put(COLUMN_CATEGORY, task.getCategory());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_STATUS, task.getStatusAsString());

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Task> getAllTasksSorted() {
        List<Task> taskList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TASKS
                + " ORDER BY CASE WHEN " + COLUMN_IS_COMPLETED + " = 1 THEN 1 ELSE 0 END, "
                + "CASE WHEN " + COLUMN_IS_COMPLETED + " = 0 THEN " + COLUMN_DEADLINE + " ELSE 0 END ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setDeadline(cursor.getLong(2));
                task.setCategory(cursor.getString(3));
                task.setCompleted(cursor.getInt(4) == 1);
                task.setDescription(cursor.getString(5));
                task.setStatusFromString(cursor.getString(6));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public void addSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, schedule.getEventName());
        values.put(COLUMN_EVENT_DATE, schedule.getEventDate());
        values.put(COLUMN_LOCATION, schedule.getLocation());
        values.put(COLUMN_IS_EVEN_WEEK, schedule.isEvenWeek() ? 1 : 0);

        db.insert(TABLE_SCHEDULE, null, values);
        db.close();
    }

    public List<Schedule> getAllSchedules(boolean isEvenWeek) {
        List<Schedule> scheduleList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SCHEDULE + " WHERE " + COLUMN_IS_EVEN_WEEK + " = " + (isEvenWeek ? 1 : 0);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Schedule schedule = new Schedule();
                schedule.setScheduleId(cursor.getInt(0));
                schedule.setEventName(cursor.getString(1));
                schedule.setEventDate(cursor.getString(2));
                schedule.setLocation(cursor.getString(3));
                schedule.setEvenWeek(cursor.getInt(4) == 1);

                scheduleList.add(schedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scheduleList;
    }

    public void updateSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, schedule.getEventName());
        values.put(COLUMN_EVENT_DATE, schedule.getEventDate());
        values.put(COLUMN_LOCATION, schedule.getLocation());
        values.put(COLUMN_IS_EVEN_WEEK, schedule.isEvenWeek() ? 1 : 0);

        db.update(TABLE_SCHEDULE, values, COLUMN_SCHEDULE_ID + " = ?", new String[]{String.valueOf(schedule.getScheduleId())});
        db.close();
    }

    public void deleteSchedule(int scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COLUMN_SCHEDULE_ID + " = ?", new String[]{String.valueOf(scheduleId)});
        db.close();
    }

    public Map<String, List<Schedule>> getSchedulesGroupedByDay(boolean isEvenWeek) {
        Map<String, List<Schedule>> schedulesByDay = new LinkedHashMap<>();

        List<Schedule> schedules = getAllSchedules(isEvenWeek);

        for (Schedule schedule : schedules) {
            String day = schedule.getEventDate();
            if (!schedulesByDay.containsKey(day)) {
                schedulesByDay.put(day, new ArrayList<>());
            }
            schedulesByDay.get(day).add(schedule);
        }

        return schedulesByDay;
    }
}
