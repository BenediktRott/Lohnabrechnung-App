package com.example.turnen.data;

import androidx.annotation.NonNull;

public class FixtureModel {

    private int id;
    private String day;
    private String timeStart;
    private String timeEnd;
    private int enabled;

    public FixtureModel(int id, String day, String timeStart, String timeEnd, int enabled) {
        this.id = id;
        this.day = day;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.enabled = enabled;
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeModel{" +
                "id=" + id +
                ", day='" + day + '\'' +
                ", timeStart='" + timeStart + '\'' +
                ", timeEnd='" + timeEnd + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }
}
