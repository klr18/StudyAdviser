package com.example.studyadviser;

public class Task {
    private int id;
    private String title;
    private long deadline;
    private String category;
    private boolean isCompleted;
    private String description;
    private Status status;

    public enum Status {
        PLANNED, IN_PROGRESS, COMPLETED
    }

    public Task() {
        this.status = Status.PLANNED;
    }

    public Task(String title, long deadline, String category, String description, Status status, boolean isCompleted) {
        this.title = title;
        this.deadline = deadline;
        this.category = category;
        this.description = description;
        this.status = (status != null) ? status : Status.PLANNED;
        this.isCompleted = isCompleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public String getStatusAsString() {
        return status.name();
    }

    public void setStatusFromString(String status) {
        try {
            this.status = Status.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            this.status = Status.PLANNED;
        }
    }
}
