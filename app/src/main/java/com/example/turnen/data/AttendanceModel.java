package com.example.turnen.data;

public class AttendanceModel {
    //date formatted as DD.MM.yyyy, begin and end times formatted as hh:mm
    private String date, begin, end;
    private int id;

    public AttendanceModel(int id, String date, String begin, String end) {
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
