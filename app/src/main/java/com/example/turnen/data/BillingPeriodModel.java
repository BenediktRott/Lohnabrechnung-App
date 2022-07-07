package com.example.turnen.data;

import com.example.turnen.DateHandler;

public class BillingPeriodModel {
    //dates formatted as DD:MMM:yyyy
    public String dateStart, dateEnd;
    public int id;

    public BillingPeriodModel(int id, String dateStart, String dateEnd) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.id = id;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public String getDate(){
        return DateHandler.generatePeriod(dateStart, dateEnd);
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPeriod(){
        return DateHandler.generatePeriod(dateStart, dateEnd);
    }
}
