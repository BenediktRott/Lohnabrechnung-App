package com.example.turnen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.turnen.DateHandler;

import java.util.ArrayList;
/**
 * DataBaseHelper for the fixtures, i.e. the regular training times
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String FIXTURE_TABLE = "FIXTURE_TABLE";
    public static final String COLUMN_FIXTURE_DAY = "FIXTURE_DAY";
    public static final String COLUMN_FIXTURE_BEGIN = "FIXTURE_BEGIN";
    public static final String COLUMN_FIXTURE_END = "FIXTURE_END";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_FIXTURE_ENABLED = "FIXTURE_ENABLED";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "fixture.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + FIXTURE_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FIXTURE_DAY + " TEXT, " + COLUMN_FIXTURE_BEGIN + " TEXT, " + COLUMN_FIXTURE_END + " TEXT, " + COLUMN_FIXTURE_ENABLED + " INT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public boolean addOne(FixtureModel fixtureModel){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_FIXTURE_DAY, fixtureModel.getDay());
        cv.put(COLUMN_FIXTURE_BEGIN, fixtureModel.getTimeStart());
        cv.put(COLUMN_FIXTURE_END, fixtureModel.getTimeEnd());
        cv.put(COLUMN_FIXTURE_ENABLED, fixtureModel.getEnabled());

        long insert = db.insert(FIXTURE_TABLE, null, cv);
        return insert != -1;
    }

    public boolean deleteOne(FixtureModel fixtureModel){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + FIXTURE_TABLE + " WHERE " + COLUMN_ID + " = " + fixtureModel.getId();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            cursor.close();
            return true;
        }else{
            //db.close();
            cursor.close();
            return false;
        }

    }

    public ArrayList<FixtureModel> getAllFixtures(){
        ArrayList<FixtureModel> fixtures = new ArrayList<>();
        String queryString = "SELECT * FROM " + FIXTURE_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){

            do{
                int fixtureID = cursor.getInt(0);
                String fixtureDay = cursor.getString(1);
                String fixtureTimeStart = cursor.getString(2);
                String fixtureTimeEnd = cursor.getString(3);
                int enabled = cursor.getInt(4);
                fixtures.add(new FixtureModel(fixtureID, fixtureDay, fixtureTimeStart, fixtureTimeEnd, enabled));
            }while (cursor.moveToNext());

        }

        //close cursor and db
        cursor.close();
        //db.close();

        return fixtures;
    }

    public boolean sort(){
        SQLiteDatabase db = getWritableDatabase();

        //TODO: Use Days
        String query = "SELECT * FROM " + FIXTURE_TABLE + " ORDER BY (CASE " + COLUMN_FIXTURE_DAY + " WHEN 'Mo' THEN 1 WHEN 'Di' THEN 2 WHEN 'Mi' THEN 3 WHEN 'Do' THEN 4 WHEN 'Fr' THEN 5 WHEN 'Sa' THEN 6 WHEN 'So' THEN 7 END), " + COLUMN_FIXTURE_BEGIN + ", " + COLUMN_FIXTURE_END;
        Cursor cursor = db.rawQuery(query, null);

        //delete everything from database and rewrite the ordered database to the database
        if(cursor.moveToFirst()){
            db.execSQL("DELETE FROM "+ FIXTURE_TABLE);

            do{
                int id = cursor.getInt(0);
                String day = cursor.getString(1);
                String ts = cursor.getString(2);
                String te = cursor.getString(3);
                int e = cursor.getInt(4);
                addOne(new FixtureModel(id, day, ts, te, e));
            }while (cursor.moveToNext());

            //db.close();
            cursor.close();
            return true;
        }else{
            //db.close();
            cursor.close();
            return false;
        }

    }

    public boolean setDisabled(FixtureModel fixtureModel){
        SQLiteDatabase db = getWritableDatabase();
        String fd = fixtureModel.getDay();
        String fs = fixtureModel.getTimeStart();
        String fe = fixtureModel.getTimeEnd();
        String query = "UPDATE " + FIXTURE_TABLE + " SET " + COLUMN_FIXTURE_ENABLED + " = 0 WHERE " + COLUMN_FIXTURE_DAY + " = " + "'" + fd + "'" + " AND " +
                COLUMN_FIXTURE_BEGIN + " = " + "'" + fs + "'" + " AND " +
                COLUMN_FIXTURE_END + " = " + "'" + fe + "'";
        Cursor cursor = db.rawQuery(query, null);
        boolean res = cursor.moveToFirst();
        //db.close();
        cursor.close();
        return res;
    }

    public boolean setEnabled(FixtureModel fixtureModel){
        SQLiteDatabase db = getWritableDatabase();
        String fd = fixtureModel.getDay();
        String fs = fixtureModel.getTimeStart();
        String fe = fixtureModel.getTimeEnd();
        String query = "UPDATE " + FIXTURE_TABLE + " SET " + COLUMN_FIXTURE_ENABLED + " = 1 WHERE " + COLUMN_FIXTURE_DAY + " = " + "'" + fd + "'" + " AND " +
                COLUMN_FIXTURE_BEGIN + " = " + "'" + fs + "'" + " AND " +
                COLUMN_FIXTURE_END + " = " + "'" + fe + "'";
        Cursor cursor = db.rawQuery(query, null);
        boolean res = cursor.moveToFirst();
        //db.close();
        cursor.close();
        return res;
    }

    public boolean isEnabled(FixtureModel fixtureModel){
        SQLiteDatabase db = getReadableDatabase();
        String fd = fixtureModel.getDay();
        String fs = fixtureModel.getTimeStart();
        String fe = fixtureModel.getTimeEnd();
        String query = "SELECT " + COLUMN_FIXTURE_ENABLED + " FROM " + FIXTURE_TABLE + " WHERE " + COLUMN_FIXTURE_DAY + " = " + "'" + fd + "'" + " AND " +
        COLUMN_FIXTURE_BEGIN + " = " + "'" + fs + "'" + " AND " +
                COLUMN_FIXTURE_END + " = " + "'" + fe + "'";
        Cursor cursor = db.rawQuery(query, null);
        if(!cursor.moveToFirst()){
            db.close();
            cursor.close();
            return false;
        }
        boolean res = cursor.getInt(0) == 1;
        //db.close();
        cursor.close();
        return res;
    }

    public String getNearestTimeslot(int dayOfWeek, String time){
        int buffer = 30;
        String day;
        String timeReturn = "";
        String timeStartPrev = "";
        day = dayOfWeekToString(dayOfWeek);
        Cursor cursor = getTimesDay(day);
        if (!cursor.moveToFirst()){
            Log.d("found", "Nothing found");
            cursor.close();
            return "Select time";
        }
        Log.d("found", "Something found");
        do{
            String timeStart = cursor.getString(2);
            Log.d("time", timeStart);
            String timeEnd = cursor.getString(3);
            if(isEarlier(timeStart, time, buffer) && !timeStart.equals(timeStartPrev)){
                //timeReturn = timeStart + " - " + timeEnd;
                timeReturn = DateHandler.generateTimePeriod(timeStart, timeEnd);
                timeStartPrev = timeStart;
            }
        }while (cursor.moveToNext());

        if(timeReturn.equals("")){
            cursor.moveToFirst();
            //String res = cursor.getString(2) + " - " + cursor.getString(3);
            String res = DateHandler.generateTimePeriod(cursor.getString(2), cursor.getString(3));
            cursor.close();
            return res;
        }
        cursor.close();
        return timeReturn;
    }

    private Cursor getTimesDay(String day){
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT * FROM " + FIXTURE_TABLE + " WHERE " + COLUMN_FIXTURE_DAY + " = " + "'" + day + "' AND " + COLUMN_FIXTURE_ENABLED + " = '1'";
        Log.d("query", query);
        return db.rawQuery(query, null);
    }

    public boolean isEarlier(String time1, String time2, int buffer){
        String[] time1Split = time1.split(":");
        String[] time2Split = time2.split(":");
        while(buffer > 0){
            if(Integer.parseInt(time1Split[1]) < buffer){
                if(buffer >= 60){
                    time1Split[0] = String.valueOf(Integer.parseInt(time1Split[0]) - 1);
                    buffer = buffer - 60;
                }else {
                    time1Split[0] = String.valueOf(Integer.parseInt(time1Split[0]) - 1);
                    time1Split[1] = String.valueOf(60 - ( Integer.parseInt(time1Split[1]) - buffer));
                    buffer = 0;
                }
            }else{
                time1Split[1] = String.valueOf(Integer.parseInt(time1Split[1]) - buffer);
                buffer = 0;
            }
        }
        if(Integer.parseInt(time1Split[0]) < Integer.parseInt(time2Split[0])){
            return true;
        }else if(Integer.parseInt(time1Split[0]) == Integer.parseInt(time2Split[0])){
            if(Integer.parseInt(time1Split[1]) <= Integer.parseInt(time2Split[1])){
                return true;
            }else{
                return false;
            }
        }else {
            return false;
        }
    }

    public String[] getPreviousTime(int day, String timeStart, String timeEnd){
        SQLiteDatabase db = getReadableDatabase();
        String[] dateArray = new String[2];
        boolean found = false;
        boolean prev;

        String query = "SELECT * FROM " + FIXTURE_TABLE + " WHERE " + COLUMN_FIXTURE_DAY + " = '" + dayOfWeekToString(day) + "' AND " +
                COLUMN_FIXTURE_BEGIN + " <= '" + timeStart + "' AND " + COLUMN_FIXTURE_END + " <= '" + timeEnd + "' AND "
                + COLUMN_FIXTURE_ENABLED + " = '1'";

        Cursor cursor = db.rawQuery(query, null);
        if(!cursor.moveToFirst()){
            Log.d("found", "Cursor Empty");
            //db.close();
            cursor.close();
            return new String[2];
        }
        cursor.moveToLast();
        do{
            if(cursor.getString(2).equals(timeStart) && cursor.getString(3).equals(timeEnd)){
                Log.d("found", "found");
                found = true;
            }
            prev = cursor.moveToPrevious();
        }while (prev && !found);

        if(found && prev){
            dateArray[0] = cursor.getString(2);
            dateArray[1] = cursor.getString(3);
        }
        else{
            cursor.moveToLast();
            dateArray[0] = cursor.getString(2);
            dateArray[1] = cursor.getString(3);
        }
        //db.close();
        cursor.close();
        return dateArray;
    }

    public String[] getNextTime(int day, String timeStart, String timeEnd){
        SQLiteDatabase db = getReadableDatabase();
        String[] dateArray = new String[2];
        boolean found = false;
        boolean next;

        String query = "SELECT * FROM " + FIXTURE_TABLE + " WHERE " + COLUMN_FIXTURE_DAY + " = '" + dayOfWeekToString(day) + "' AND " +
                COLUMN_FIXTURE_BEGIN + " >= '" + timeStart + "' AND " + COLUMN_FIXTURE_END + " >= '" + timeEnd + "' AND "
                + COLUMN_FIXTURE_ENABLED + " = '1'";

        Cursor cursor = db.rawQuery(query, null);
        if(!cursor.moveToFirst()){
            Log.d("found", "Cursor Empty");
            //db.close();
            cursor.close();
            return new String[2];
        }

        do{
            if(cursor.getString(2).equals(timeStart) && cursor.getString(3).equals(timeEnd)){
                Log.d("found", "found");
                found = true;
            }
            next = cursor.moveToNext();
        }while (next && !found);

        if(found && next){
            dateArray[0] = cursor.getString(2);
            dateArray[1] = cursor.getString(3);
        }
        else{
            cursor.moveToFirst();
            dateArray[0] = cursor.getString(2);
            dateArray[1] = cursor.getString(3);
        }
        //db.close();
        cursor.close();
        return dateArray;
    }

    public static String dayOfWeekToString(int dayOfWeek){
        String day;
        switch (dayOfWeek){
            case 1:
                day = "Mo";
                break;
            case 2:
                day = "Di";
                break;
            case 3:
                day = "Mi";
                break;
            case 4:
                day = "Do";
                break;
            case 5:
                day = "Fr";
                break;
            case 6:
                day = "Sa";
                break;
            case 7:
                day = "So";
                break;
            default:
                day = "";
                break;
        }
        return day;
    }
}
