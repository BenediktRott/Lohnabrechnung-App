package com.example.turnen.ui.HomeFragment;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.example.turnen.DateHandler;
import com.example.turnen.R;
import com.example.turnen.data.AttendanceModel;
import com.example.turnen.data.DataBaseHelper;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.data.DataBaseHelperBillingPeriod;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HomeViewModel extends ViewModel {

    public String[] monthsString;
    public int year;
    public int month;
    public String date;
    public String time;
    public String period;
    public DataBaseHelper dataBaseHelper;
    public DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod;
    public DataBaseHelperAttendance dataBaseHelperAttendance;
    public int[][] activeDates; //contains the amount of Attendances on a Date, where activeDates[1][5] represents
                                //represents the 5. of the currently displayed month and year
    public byte[][] calendarDates;
    public ArrayList<String> daysOfWeek;

    //Variables for AttendanceCalendarAdapter
    public AttendanceCalendarAdapter attendanceCalendarAdapter;
    public ArrayList<String> datesSelected;

    //Variables for currently displayed Attendance adapter (List View, NOT Calendar)
    public ArrayList<AttendanceModel> attendancesSelected;
    public int attendanceAdapterDay, attendanceAdapterMonth, attendanceAdapterYear;
    public ActionMode attendanceAdapterActionMode;
    public AttendanceAdapter attendanceAdapter;
    public ArrayList<AttendanceModel> attendancesDay;

    //BottomSheet
    public BottomSheetBehavior<View> bottomSheetBehavior;
    public BottomSheetCalendarItem bottomSheet;
    public View standardBottomSheet;

    //Fragment
    public HomeFragment homeFragment;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDay(){
        String[] dateSplit = date.split("\\.");
        LocalDate localDate = LocalDate.of(Integer.parseInt(dateSplit[2]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]));
        return localDate.getDayOfWeek().getValue();
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }


    /**
     * Changes all the relevant variables (date, time, period) to the previous Date. Dates must be given in the Form dd.MM.yyyy
     * a date may be given to simultaneously check whether the given date equals saved date
     * @param txtDate
     */
    public void previousDate(@Nullable String txtDate){
        LocalDate currentDate;
        if(txtDate != null){
            if(!txtDate.equals(date)){
                Log.e("HomeViewModel", "HomeViewModel date does not equal displayed date");
            }
            currentDate = DateHandler.toLocalDate(txtDate);
        }else{
            currentDate = DateHandler.toLocalDate(date);
        }
        currentDate = currentDate.minusDays(1);
        attendanceCalendarAdapter.chosenPos--;
        date = DateHandler.localDateFormatter.format(currentDate);
        time = dataBaseHelper.getNearestTimeslot(currentDate.getDayOfWeek().getValue(), DateHandler.formatterTime.format(new Date()));
        period = dataBaseHelperBillingPeriod.getNearestPeriod(date);

    }

    /**
     * Changes all the relevant variables (date, time, period) to the next Date. Dates must be given in the Form dd.MM.yyyy
     * a date may be given to simultaneously check whether the given date equals saved date
     * @param txtDate
     */
    public void nextDate(@Nullable String txtDate){
        LocalDate currentDate;
        if(txtDate != null){
            if(!txtDate.equals(date)){
                Log.e("HomeViewModel", "HomeViewModel date does not equal displayed date");
            }
            currentDate = DateHandler.toLocalDate(txtDate);
        }else{
            currentDate = DateHandler.toLocalDate(date);
        }
        attendanceCalendarAdapter.chosenPos++;
        currentDate = currentDate.plusDays(1);
        date = DateHandler.localDateFormatter.format(currentDate);
        time = dataBaseHelper.getNearestTimeslot(currentDate.getDayOfWeek().getValue(), DateHandler.formatterTime.format(new Date()));
        period = dataBaseHelperBillingPeriod.getNearestPeriod(date);

    }

    //changes the time to the previous fixture. If not possible, e.g. because it is the first fixture of the date
    //nothing will change
    public void previousTime(Context context){
        if (time.equals(context.getString(R.string.selectTime))) {
            return;
        }
        String[] timeSplit = DateHandler.splitTime(time);
        String[] timePrevSplit = dataBaseHelper.getPreviousTime(getDay(), timeSplit[0], timeSplit[1]);
        if (timePrevSplit[0] != null) {
            time = DateHandler.generateTimePeriod(timePrevSplit[0], timePrevSplit[1]);
        }
    }

    //changes the time to the previous fixture. If not possible, e.g. because it is the last fixture of the date
    //nothing will change
    public void nextTime(Context context){
        if (time.equals(context.getString(R.string.selectTime))) {
            return;
        }
        String[] timeSplit = DateHandler.splitTime(time);
        String[] timePrevSplit = dataBaseHelper.getNextTime(getDay(), timeSplit[0], timeSplit[1]);
        if (timePrevSplit[0] != null) {
            time = DateHandler.generateTimePeriod(timePrevSplit[0], timePrevSplit[1]);
        }
    }


    /**
     * Add the attendance currently defined by the saved parameters to the database and update
     * activeDates accordingly
      * @param context
     */
    public void submitAttendance(Context context){
        if (time.equals(context.getString(R.string.selectTime))) {
            return;
        }
        AttendanceModel attendanceModel = new AttendanceModel(-1, date,
                DateHandler.splitTime(time)[0],
                DateHandler.splitTime(time)[1]);
        //TODO: Add at correct, sorted position
        dataBaseHelperAttendance.addOne(attendanceModel);

        //adjust activeDates
        if(DateHandler.getMonthFromDate(date) == month){
            activeDates[1][DateHandler.getDayFromDate(date)] ++;
        }else if(DateHandler.getMonthFromDate(date) == month - 1){
            activeDates[0][DateHandler.getDayFromDate(date)] ++;
        }else if(DateHandler.getMonthFromDate(date) == month + 1){
            activeDates[2][DateHandler.getDayFromDate(date)] ++;
        }
        dataBaseHelperAttendance.sort();
    }


    /**
     * Remove the attendance currently defined by the saved parameters from the database and update
     * activeDates accordingly
     * @param context
     * @return true if an element was removed, false if not
     */
    public boolean removeAttendance(Context context){
        if (time.equals(context.getString(R.string.selectTime))) {
            return false;
        }

        if(DateHandler.getMonthFromDate(date) == month){
            activeDates[1][DateHandler.getDayFromDate(date)] --;
        }else if(DateHandler.getMonthFromDate(date) == month - 1){
            activeDates[0][DateHandler.getDayFromDate(date)] --;
        }else if(DateHandler.getMonthFromDate(date) == month + 1){
            activeDates[2][DateHandler.getDayFromDate(date)] --;
        }

        return dataBaseHelperAttendance.deleteOne(date,
                DateHandler.splitTime(time)[0],
                DateHandler.splitTime(time)[1]);
    }


    //generates an int[3][32] where each entry represents the amount of attendances on that date
    public int[][] generateActiveDates(int year, int month){
        int[][] res = new int[3][32];
        if(month == 1){
            res[0] = dataBaseHelperAttendance.busyDays(year - 1, 12);
            res[1] = dataBaseHelperAttendance.busyDays(year, month);
            res[2] = dataBaseHelperAttendance.busyDays(year, month + 1);
        }else if(month == 12){
            res[0] = dataBaseHelperAttendance.busyDays(year, month - 1);
            res[1] = dataBaseHelperAttendance.busyDays(year, month);
            res[2] = dataBaseHelperAttendance.busyDays(year + 1, 1);
        }else{
            res[0] = dataBaseHelperAttendance.busyDays(year, month - 1);
            res[1] = dataBaseHelperAttendance.busyDays(year, month);
            res[2] = dataBaseHelperAttendance.busyDays(year, month + 1);
        }

        activeDates = res;
        return res;
    }

    /**
     * Generates an int[3][32] activeDates where each entry represents the amount of attendances on that date.
     * The first index represents the month. The currently selected month is activeDates[1], the previous/next is
     * activeDates[0] / activeDates[2]. The second Index represents the Day of month. E.g. activeIndex[0][25] would equal the
     * amount of attendances on the 25th of the previous month.
     * @return
     */
    public int[][] generateActiveDates(){
        int[][] res = new int[3][32];
        int[] prevMonth = DateHandler.offsetMonth(month, year, -1);
        int[]nextMonth = DateHandler.offsetMonth(month, year, 1);
        res[0] = dataBaseHelperAttendance.busyDays(prevMonth[1], prevMonth[0]);
        res[1] = dataBaseHelperAttendance.busyDays(year, month);
        res[2] = dataBaseHelperAttendance.busyDays(nextMonth[1], nextMonth[0]);

        activeDates = res;
        return res;
    }


    //update month and year to the next month and update activeDates accordingly
    public void nextMonth(){

        //change month and year
        if(month == 12){
            month = 1;
            year++;
        }else{
            month++;
        }

        //change activeDates
        activeDates[0] = activeDates[1];
        activeDates[1] = activeDates[2];
        int[] nextMonth = DateHandler.offsetMonth(month, year, 1);
        activeDates[2] = dataBaseHelperAttendance.busyDays(nextMonth[1], nextMonth[0]);
        calendarDates = DateHandler.getCalendarDates(year, month);
    }

    //update month and year to the previous month and update activeDates accordingly
    public void previousMonth(){

        //change month and year
        if(month == 1){
            month = 12;
            year--;
        }else{
            month--;
        }

        //change activeDates
        activeDates[2] = activeDates[1];
        activeDates[1] = activeDates[0];
        int[] prevMonth = DateHandler.offsetMonth(month, year, -1);
        activeDates[0] = dataBaseHelperAttendance.busyDays(prevMonth[1], prevMonth[0]);
        calendarDates = DateHandler.getCalendarDates(year, month);
    }

    /**
     * Add the given day of month month + monthOffset to the ArrayList of selected Dates
     * @param day
     * @param monthOffset
     */
    public void addDateSelected(int day, int monthOffset){
        int monthDelete = 0;
        int yearDelete = 0;
        //handle border cases
        if(month == 1 && monthOffset == -1){
            monthDelete = 12;
            yearDelete = year--;
        }else if(month == 12 && monthOffset == 1){
            monthDelete = 1;
            year = year++;
        }else{
            monthDelete = month;
            yearDelete = year;
        }
        datesSelected.add(DateHandler.generateDate(yearDelete, monthDelete + monthOffset, day));
    }

    /**
     * Remove the given day of month month + monthOffset from the ArrayList of selected Dates
     * @param day
     * @param monthOffset
     */
    public void deleteDateSelected(int day, int monthOffset){
        int monthDelete = 0;
        int yearDelete = 0;
        //handle border cases
        if(month == 1 && monthOffset == -1){
            monthDelete = 12;
            yearDelete = year--;
        }else if(month == 12 && monthOffset == 1){
            monthDelete = 1;
            year = year++;
        }else{
            monthDelete = month;
            yearDelete = year;
        }
        datesSelected.removeAll(Collections.singleton(DateHandler.generateDate(yearDelete, monthDelete + monthOffset, day)));
    }

    /**
     * Remove all attendances on the selected dates from the database
     */
    public void deleteDatesSelectedFromDatabase(){
        for (int i = 0; i < datesSelected.size(); i++){
            dataBaseHelperAttendance.deleteByDate(datesSelected.get(i));
        }
        activeDates = generateActiveDates();
    }

    /**
     * Check whether the given attendanceModel is contained in the list of selected attendances.
     * Equality is checked by comparing IDs.
     * @param attendanceModel
     * @return
     */
    public boolean isAttendanceSelected(AttendanceModel attendanceModel){
        for(AttendanceModel a : attendancesSelected){
            if(a.getId() == attendanceModel.getId()){
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the given attendanceModel from the list of selected attendances.
     * Equality is checked by comparing IDs.
     * @param attendanceModel
     * @return
     */
    public void unselectAttendance(AttendanceModel attendanceModel){
        /*
        for(AttendanceModel a : attendancesSelected) {
            if (a.getId() == attendanceModel.getId()) {
                attendancesSelected.remove(a);
            }
        }
         */

        for(int i = 0; i < attendancesSelected.size(); i++){
            AttendanceModel a = attendancesSelected.get(i);
            if (a.getId() == attendanceModel.getId()) {
                attendancesSelected.remove(a);
            }
        }
    }


    /**
     * Returns the Month and year as, e.g. April 2022
     * @return
     */
    public String getMonthYearString(){
        return monthsString[month - 1] + " " + year;
    }

    /**
     * Remove all attendances in attendancesSelected from the database. attendancesSelected is NOT set to null
     */
    public void deleteAttendancesSelectedFromDatabase(){
        for (int i = 0; i < attendancesSelected.size(); i++){
            dataBaseHelperAttendance.deleteOne(attendancesSelected.get(i));
        }
    }

    public void setDatePeriod(int year, int month, int day){
        this.date = DateHandler.generateDate(year, month, day);
        this.period = dataBaseHelperBillingPeriod.getNearestPeriod(this.date);
        int dayOfWeek = LocalDate.of(year, month, day).getDayOfWeek().getValue();
        this.time = dataBaseHelper.getNearestTimeslot(dayOfWeek, DateHandler.formatterTime.format(new Date()));
    }

}
