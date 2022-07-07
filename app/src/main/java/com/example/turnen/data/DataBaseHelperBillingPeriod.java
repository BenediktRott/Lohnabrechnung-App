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

public class DataBaseHelperBillingPeriod extends SQLiteOpenHelper {

    public static final String BILLINGPERIOD_TABLE = "BILLINGPERIOD_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_BILLINGPERIOD_BEGIN = "BILLINGPERIOD_BEGIN";
    public static final String COLUMN_BILLINGPERIOD_END = "BILLINGPERIOD_END";
    public static Context context;

    public DataBaseHelperBillingPeriod(@Nullable Context context) {
        super(context, "billingperiod.db", null, 1);
        DataBaseHelperBillingPeriod.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + BILLINGPERIOD_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BILLINGPERIOD_BEGIN + " TEXT, " + COLUMN_BILLINGPERIOD_END + " TEXT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(BillingPeriodModel billingPeriodModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_BILLINGPERIOD_BEGIN, billingPeriodModel.getDateStart());
        cv.put(COLUMN_BILLINGPERIOD_END, billingPeriodModel.getDateEnd());

        long insert = db.insert(BILLINGPERIOD_TABLE, null, cv);
        return insert != -1;
    }

    public boolean deleteOne(BillingPeriodModel billingPeriodModel) {
        SQLiteDatabase db = getWritableDatabase();

        String queryFill = "SELECT * FROM " + BILLINGPERIOD_TABLE;
        Cursor cursor = db.rawQuery(queryFill, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }

        do {
            if (cursor.getInt(0) == billingPeriodModel.getId()) {
                if (cursor.moveToNext()) {

                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_BILLINGPERIOD_END, billingPeriodModel.getDateEnd());
                    db.update(BILLINGPERIOD_TABLE, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(cursor.getInt(0))});

                }
                break;
            }
        } while (cursor.moveToNext());
        cursor.close();

        String query = "DELETE FROM " + BILLINGPERIOD_TABLE + " WHERE " + COLUMN_ID + " = " + billingPeriodModel.getId();

        Cursor cursorDelete = db.rawQuery(query, null);

        if (cursorDelete.moveToFirst()) {
            //db.close();
            cursorDelete.close();
            return true;
        } else {
            //db.close();
            cursorDelete.close();
            return false;
        }
    }

    public ArrayList<BillingPeriodModel> getAllBillingPeriods() {
        ArrayList<BillingPeriodModel> billingPeriods = new ArrayList<>();
        String query = "SELECT * FROM " + BILLINGPERIOD_TABLE;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

            do {
                int id = cursor.getInt(0);
                String begin = cursor.getString(1);
                String end = cursor.getString(2);
                billingPeriods.add(new BillingPeriodModel(id, begin, end));
            } while (cursor.moveToNext());

        }

        //close cursor and db
        cursor.close();
        //db.close();

        return billingPeriods;
    }

    public boolean sort() {
        Log.d("sortCall", "sortCalled");
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + BILLINGPERIOD_TABLE;
        BillingPeriodModel firstEntry = null;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            db.execSQL("DELETE FROM " + BILLINGPERIOD_TABLE);
            do {
                int id = cursor.getInt(0);
                String begin = DateHandler.switchDate(cursor.getString(1));
                Log.d("sort", "Reversing executed");
                Log.d("MyTag", String.valueOf(cursor.getString(2).equals("")));
                Log.d("MyTag", "We get:" + cursor.getString(2));
                if (!cursor.getString(2).equals("")) {
                    String end = DateHandler.switchDate(cursor.getString(2));
                    addOne(new BillingPeriodModel(id, begin, end));
                } else {
                    firstEntry = new BillingPeriodModel(-1, DateHandler.switchDate(begin), "");
                }
            } while (cursor.moveToNext());

            query = "SELECT * FROM " + BILLINGPERIOD_TABLE + " ORDER BY " + COLUMN_BILLINGPERIOD_BEGIN +
                    " DESC, " + COLUMN_BILLINGPERIOD_END + " DESC";
            Log.d("query", query);
            cursor.close();

            Cursor cursorDelete = db.rawQuery(query, null);
            //TODO: Can't be removed otherwise doesn't work, no clue why
            //Log.d("cursorSize", String.valueOf(cursorDelete.getCount()));
            cursorDelete.moveToFirst();
            db.execSQL("DELETE FROM " + BILLINGPERIOD_TABLE);
            if (firstEntry != null) {
                Log.d("entry", "Last entry found");
                addOne(firstEntry);
            }
            if (cursorDelete.moveToFirst()) {
                do {
                    int id = cursorDelete.getInt(0);
                    String begin = DateHandler.switchDate(cursorDelete.getString(1));
                    String end = DateHandler.switchDate(cursorDelete.getString(2));
                    addOne(new BillingPeriodModel(id, begin, end));
                    Log.d("sort", "Sorting executed");
                } while (cursorDelete.moveToNext());
            }
            cursorDelete.close();
            return true;
        } else {
            cursor.close();
            return false;
        }

    }



    public boolean insert(String date) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + BILLINGPERIOD_TABLE;
        BillingPeriodModel billingPeriod;

        Cursor cursor = db.rawQuery(query, null);

        if (!cursor.moveToFirst()) {
            billingPeriod = new BillingPeriodModel(-1, date, "");
            addOne(billingPeriod);
            cursor.close();
            //db.close();
            return true;
        }

        //check weather given date is after beginning of last period
        if (DateHandler.switchDate(cursor.getString(1)).compareTo(DateHandler.switchDate(date)) <= 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_BILLINGPERIOD_END, date);
            db.update(BILLINGPERIOD_TABLE, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(cursor.getInt(0))});

            billingPeriod = new BillingPeriodModel(-1, date, "");
            addOne(billingPeriod);
            cursor.close();
            //db.close();
            return true;
        }

        //Get to insert position
        while (cursor.moveToNext() && DateHandler.switchDate(cursor.getString(1)).compareTo(DateHandler.switchDate(date)) >= 0) {
            Log.d("whileLoop", cursor.getString(1));
        }

        //check weather inserting to the end
        if (!cursor.move(0)) {
            Log.d("First", "First Element");
            cursor.moveToPrevious();

        } else {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_BILLINGPERIOD_END, date);
            db.update(BILLINGPERIOD_TABLE, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(cursor.getInt(0))});
            cursor.moveToPrevious();
        }
        billingPeriod = new BillingPeriodModel(-1, date, cursor.getString(1));
        addOne(billingPeriod);
        cursor.close();
        //db.close();
        return true;

    }


    public BillingPeriodModel findByBeginning(String begin) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + BILLINGPERIOD_TABLE + " WHERE " + COLUMN_BILLINGPERIOD_BEGIN + " = '" + begin + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        BillingPeriodModel billingPeriodModel = new BillingPeriodModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
        cursor.close();
        return billingPeriodModel;
    }

    public String getNearestPeriod(String date){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + BILLINGPERIOD_TABLE;

        Cursor cursor = db.rawQuery(query, null);

        if(!cursor.moveToFirst()){
            cursor.close();
            return context.getString(R.string.showAll);
        }

        do{
            //check weather beginning at current cursor position is before given date
            if(DateHandler.switchDate(cursor.getString(1)).compareTo(DateHandler.switchDate(date)) <= 0){
                break;
            }
        }while (cursor.moveToNext());

        if (cursor.move(0)){
            //return cursor.getString(1) + " - " + cursor.getString(2);
            String period = DateHandler.generatePeriod(cursor.getString(1), cursor.getString(2));
            cursor.close();
            return period;
        }else {
            cursor.close();
            return context.getString(R.string.showAll);
        }
    }

}
