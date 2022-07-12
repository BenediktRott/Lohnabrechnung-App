package com.example.turnen;

import static java.time.temporal.ChronoUnit.DAYS;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;

public abstract class DateHandler {
    public static SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
    public static DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String generatePeriod(String dateStart, String dateEnd) {
        return dateStart + " - " + dateEnd;
    }

    public static String generateTimePeriod(String timeStart, String timeEnd) {
        return timeStart + " - " + timeEnd;
    }

    public static String generateTime(int hour, int minute) {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

    public static String generateMonthYearDate(int year, int month){
        return String.format("%02d", month) + "." + year;
    }

    public static String generateDate(int year, int month, int day){
        LocalDate localDate = LocalDate.of(year, month, day);
        return localDateFormatter.format(localDate);
    }

    public static Integer getDayFromDate(String date){
        return Integer.parseInt(date.split("\\.")[0]);
    }

    public static Integer getMonthFromDate(String date){
        return Integer.parseInt(date.split("\\.")[1]);
    }

    public static String getCurrentDate() {
        return formatterDate.format(new Date());
    }

    public static String getCurrentTime() {
        return formatterTime.format(new Date());
    }

    public static int getCurrentDayOfWeek() {
        return LocalDate.now().getDayOfWeek().getValue();
    }

    public static String[] splitTime(String time) {
        return time.replaceAll("\\s+", "").split("-");
    }

    public static LocalDate toLocalDate(String date) {
        String[] dateSplit = date.split("\\.");
        return LocalDate.of(Integer.parseInt(dateSplit[2]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]));
    }

    public static String switchDate(String date) {
        String[] dateSplit = date.split("\\.");
        return dateSplit[2] + "." + dateSplit[1] + "." + dateSplit[0];
    }

    public static int getCalendarMonthSize(int year, int month){
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());
        int diff = end.get(WeekFields.ISO.weekOfWeekBasedYear()) - start.get(WeekFields.ISO.weekOfWeekBasedYear()) + 1;
        diff = diff > 0 ? diff : end.get(WeekFields.ISO.weekOfWeekBasedYear()) + 1;
        return diff;
    }

    public static byte[][] getCalendarDates(int year, int month){
        byte[][] calDates = new byte[getCalendarMonthSize(year, month)][7];
        LocalDate localDate = LocalDate.of(year, month, 1);
        localDate = localDate.minusDays(localDate.getDayOfWeek().getValue() - 1);
        for(int i = 0; i < getCalendarMonthSize(year, month); i++){
            for(int j = 0; j < 7; j++){
                calDates[i][j] = (byte) localDate.getDayOfMonth();
                localDate = localDate.plusDays(1);
            }
        }
        return calDates;
    }

    /**
     * Offsets a given month and year by + the offset given in months. Returns array containing the new month as the first
     * element and the new year as the second element
     * @param month
     * @param year
     * @param offset
     * @return
     */
    public static int[] offsetMonth(int month, int year, int offset){
        int yearReturn = year + offset/12;
        int monthReturn = month + offset%12;
        if(monthReturn > 12){
            monthReturn = monthReturn - 12;
            yearReturn = yearReturn + 1;
        }else if(monthReturn < 1){
            monthReturn = monthReturn + 12;
            yearReturn = yearReturn - 1;
        }
        return new int[]{monthReturn, yearReturn};
    }

    public static boolean datesEqual(String date, int[] monthYear, int day){
        LocalDate date1 = toLocalDate(date);
        LocalDate date2 = LocalDate.of(monthYear[1], monthYear[0], day);
        return date1.equals(date2);
    }

    public static int daysFromCurrent(LocalDate selectedDate){
        return (int) DAYS.between(selectedDate, LocalDate.now());
    }
}
