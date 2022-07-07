package com.example.turnen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.turnen.DateHandler;
import com.example.turnen.R;

import java.util.ArrayList;

public class DataBaseHelperAttendance extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "COLUMN_ID";
    public static final String ATTENDANCE_TABLE = "ATTENDANCE_TABLE";
    public static final String COLUMN_ATTENDANCE_DATE = "COLUMN_ATTENDANCE_DATE";
    public static final String COLUMN_ATTENDANCE_BEGIN = "COLUMN_ATTENDANCE_BEGIN";
    public static final String COLUMN_ATTENDANCE_END = "COLUMN_ATTENDANCE_END";

    public DataBaseHelperAttendance(@Nullable Context context) {
        super(context, "attendance.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + ATTENDANCE_TABLE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_ATTENDANCE_DATE + " TEXT, " +
                COLUMN_ATTENDANCE_BEGIN + " TEXT, " + COLUMN_ATTENDANCE_END + " TEXT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean sort(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + ATTENDANCE_TABLE;

        //String query = "SELECT * FROM " + ATTENDANCE_TABLE + " ORDER BY " + switchDate(COLUMN_ATTENDANCE_DATE);
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            db.execSQL("DELETE FROM "+ ATTENDANCE_TABLE);
            do{
                int id = cursor.getInt(0);
                String date = DateHandler.switchDate(cursor.getString(1));
                String begin = cursor.getString(2);
                String end = cursor.getString(3);
                addOne(new AttendanceModel(id, date, begin, end));
            }while (cursor.moveToNext());

            query = "SELECT * FROM " + ATTENDANCE_TABLE + " ORDER BY " + COLUMN_ATTENDANCE_DATE +
            " DESC, " + COLUMN_ATTENDANCE_BEGIN +" DESC," + COLUMN_ATTENDANCE_END + " DESC";
            cursor = db.rawQuery(query,null);
            cursor.moveToFirst();

            db.execSQL("DELETE FROM "+ ATTENDANCE_TABLE);
            do{
                int id = cursor.getInt(0);
                String date = DateHandler.switchDate(cursor.getString(1));
                String begin = cursor.getString(2);
                String end = cursor.getString(3);
                addOne(new AttendanceModel(id, date, begin, end));
            }while (cursor.moveToNext());

            //db.close();
            cursor.close();
            return true;
        }else{
            cursor.close();
            //db.close();
            return false;
        }
    }


    //TODO: Add one sorted

    public boolean addOne(AttendanceModel attendanceModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ATTENDANCE_DATE, attendanceModel.getDate());
        cv.put(COLUMN_ATTENDANCE_BEGIN, attendanceModel.getBegin());
        cv.put(COLUMN_ATTENDANCE_END, attendanceModel.getEnd());

        long insert = db.insert(ATTENDANCE_TABLE, null, cv);
        return insert != -1;
    }

    public boolean deleteOne(AttendanceModel attendanceModel){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ID + " = " + attendanceModel.getId();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            //db.close();
            cursor.close();
            return true;
        }else{
            //db.close();
            cursor.close();
            return false;
        }

    }


    public boolean deleteOne(String date, String begin, String end) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ATTENDANCE_DATE + " = '" + date + "' AND " +
                COLUMN_ATTENDANCE_BEGIN + " = '" + begin + "' AND " + COLUMN_ATTENDANCE_END + " = '" + end + "'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            //db.close();
            cursor.close();
            return true;
        } else {
            //db.close();
            cursor.close();
            return false;
        }
    }

    public ArrayList<AttendanceModel> getAllAttendances(){
        ArrayList<AttendanceModel> attendances = new ArrayList<>();
        String query = "SELECT * FROM " + ATTENDANCE_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String date = cursor.getString(1);
                String begin = cursor.getString(2);
                String end = cursor.getString(3);
                attendances.add(new AttendanceModel(id, date, begin, end));
            }while (cursor.moveToNext());
        }

        //db.close();
        cursor.close();
        return attendances;
    }

    public ArrayList<AttendanceModel> getAttendancesPeriod(String period, Context context){

        if(period.equals(context.getResources().getString(R.string.showAll))){
            return getAllAttendances();
        }

        String[] dateSplit = period.replaceAll("\\s", "").split("-");
        Log.d("AttendancesPeriod", dateSplit[0]);
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + ATTENDANCE_TABLE;
        ArrayList<AttendanceModel> attendanceModels = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);
        //check if dateSplit doesn't have beginning and end date
        if(dateSplit.length != 2){
            Log.d("AttendancesPeriod", "No End");
            Log.d("AttendancesPeriod2", "Date is " + dateSplit[0] + " and length " + dateSplit.length);
            //check for no period given
            if (dateSplit[0].equals("")){
                cursor.close();
                return attendanceModels;
            }else{
                //beginning given

                //check if there are attendances
                if(!cursor.moveToFirst()){
                    Log.d("AttendancesPeriod", "Cursor empty");
                    cursor.close();
                    return attendanceModels;
                }

                do{
                    //check if given date is before attendance date
                    if(DateHandler.switchDate(dateSplit[0]).compareTo(DateHandler.switchDate(cursor.getString(1))) <= 0 ){
                        AttendanceModel current = new AttendanceModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                        attendanceModels.add(current);
                    }else {
                        //rely on sorted to break after first one is too early
                        break;
                    }
                }while (cursor.moveToNext());
            }
        }else {
            //beginning and end given

            //check if there are attendances
            if(!cursor.moveToFirst()){
                cursor.close();
                return attendanceModels;
            }

            do{
                //check if attendance date falls between dateBegin and dateEnd
                if((DateHandler.switchDate(dateSplit[0]).compareTo(DateHandler.switchDate(cursor.getString(1))) <= 0) && DateHandler.switchDate(dateSplit[1]).compareTo(DateHandler.switchDate(cursor.getString(1))) > 0){
                    Log.d("AttendancesPeriod", "In if");
                    AttendanceModel current = new AttendanceModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                    attendanceModels.add(current);
                }else {
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return attendanceModels;
    }

    public static String[] getTimeDifference(String time1, String time2){
        String[] time1Split = time1.split(":");
        String[] time2Split = time2.split(":");
        if(time1.compareTo(time2) > 0){
            time2Split[0] = String.valueOf(Integer.parseInt(time2Split[0]) + 24);
        }

        int[] timeResSplit = new int[2];
        timeResSplit[0] = Integer.parseInt(time2Split[0]) - Integer.parseInt(time1Split[0]);
        if(Integer.parseInt(time2Split[1]) < Integer.parseInt(time1Split[1])){
            timeResSplit[0] = timeResSplit[0] - 1;
            timeResSplit[1] = 60 - (Integer.parseInt(time1Split[1]) - Integer.parseInt(time2Split[1]));
        }else {
            timeResSplit[1] = Integer.parseInt(time2Split[1]) - Integer.parseInt(time1Split[1]);
        }

        String[] res = new String[2];
        res[0] = timeResSplit[0] + "h " + timeResSplit[1] + "min";
        res[1] = String.valueOf(timeResSplit[0]*60 + timeResSplit[1]);

        return res;
    }

    public String getTotalHours() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + ATTENDANCE_TABLE;

        Cursor cursor = db.rawQuery(query, null);

        int timeMin = 0;

        if(!cursor.moveToFirst()){
            cursor.close();
            return "0h";
        }
        do{
            timeMin = Integer.parseInt(getTimeDifference(cursor.getString(2), cursor.getString(3))[1]) + timeMin;
        }while (cursor.moveToNext());
        cursor.close();
        return timeMin/60 + "h";
    }

    public String getHoursPeriod(String period, Context context){
        ArrayList<AttendanceModel> attendanceModels = getAttendancesPeriod(period, context);

        int time = 0;
        for(AttendanceModel c : attendanceModels){
            time = time + Integer.parseInt(getTimeDifference(c.getBegin(), c.getEnd())[1]);
        }

        return time/60 + "h";
    }

    public int[] busyDays(int year, int month){
        String query = "SELECT * FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ATTENDANCE_DATE + " LIKE '%" + DateHandler.generateMonthYearDate(year, month) + "'";
        SQLiteDatabase db = getReadableDatabase();

        int[] res = new int[32];

        Cursor cursor = db.rawQuery(query, null);
        if(!cursor.moveToFirst()){
            cursor.close();
            return res;
        }
        do {
            res[DateHandler.getDayFromDate(cursor.getString(1))] ++;
        }while (cursor.moveToNext());
        cursor.close();
        return res;
    }

    public boolean isActive(int year, int month, int day){
        String query = "SELECT * FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ATTENDANCE_DATE + " = '" + DateHandler.generateDate(year, month, day) + "'";
        SQLiteDatabase db = getReadableDatabase();

        Log.d("SQLString", query);

        Cursor cursor = db.rawQuery(query, null);
        Log.d("SQLCursor", String.valueOf(cursor == null));
        Log.d("SQLCursor", String.valueOf(cursor.getCount()));
        boolean active = cursor.getCount() >= 1;
        cursor.close();
        return active;
    }

    /**
     * Deletes all Attendances at a particular Date given as a String with format dd.MM.yyyy
     * @param date The Date to delete
     */
    public boolean deleteByDate(String date){
        String query = "DELETE FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ATTENDANCE_DATE + " = '" + date + "'";
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
    }

    public ArrayList<AttendanceModel> getAttendancesDay(int day, int month, int year){
        String query = "SELECT * FROM " + ATTENDANCE_TABLE + " WHERE " + COLUMN_ATTENDANCE_DATE + " = '" + DateHandler.generateDate(year, month, day) + "'";
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<AttendanceModel> attendances = new ArrayList<>();

        if(!cursor.moveToFirst()){
            cursor.close();
            return attendances;
        }

        do {
            AttendanceModel current = new AttendanceModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            attendances.add(current);
        }while (cursor.moveToNext());

        cursor.close();
        return attendances;
    }
}
