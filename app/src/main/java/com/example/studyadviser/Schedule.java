package com.example.studyadviser;

public class Schedule {
    private int scheduleId;
    private String eventName;
    private String eventDate;
    private String location;
    private boolean isEvenWeek;

    public Schedule() {}

    public Schedule(String eventName, String eventDate, String location, boolean isEvenWeek) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.location = location;
        this.isEvenWeek = isEvenWeek;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isEvenWeek() {
        return isEvenWeek;
    }

    public void setEvenWeek(boolean evenWeek) {
        isEvenWeek = evenWeek;
    }
}