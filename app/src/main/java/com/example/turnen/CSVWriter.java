package com.example.turnen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.turnen.data.AttendanceModel;
import com.example.turnen.data.DataBaseHelper;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.ui.SettingsFragment.SettingsFragment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

public class CSVWriter {
    private static final int CREATE_FILE = 1;
    private SharedPreferences sharedPreferences;

    public CSVWriter(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Function to convert an Attendance Database to a csv File using the given outputStream.
     * @param dataBaseHelperAttendance Attendance databaseHelper that will be converted to a csv.
     * @param outputStream Already configured Outputstream that will be used to write the file.
     * @param addSignature Set whether a signature containing personal Data should be added.
     */public void dataBaseToCsv(DataBaseHelperAttendance dataBaseHelperAttendance, @NonNull OutputStream outputStream, boolean addSignature) {
        try {
            if(addSignature && sharedPreferences.getBoolean(SettingsFragment.switchPrefSetSignature, false) && signatureNotEmpty()){
                outputStream.write(createSignature().getBytes());
                outputStream.write("\n".getBytes());
            }

            outputStream.write("Datum, Zeit, Dauer, Dauer in min\n".getBytes());

            SQLiteDatabase db = dataBaseHelperAttendance.getReadableDatabase();
            String query = "SELECT * FROM " + DataBaseHelperAttendance.ATTENDANCE_TABLE;

            Cursor cursor = db.rawQuery(query, null);
            if (!cursor.moveToLast()) {
                Log.d("cursor", "Cursor empty" + dataBaseHelperAttendance.getAllAttendances().size());
                return;
            }
            do {
                String date = cursor.getString(1);
                String[] dateSplit = date.split("\\.");
                LocalDate localDate = LocalDate.of(Integer.parseInt(dateSplit[2]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]));
                int dayOfWeek = DayOfWeek.from(localDate).getValue();
                Log.d("cursor", "Cursor not empty" + dataBaseHelperAttendance.getAllAttendances().size());
                String[] time = DataBaseHelperAttendance.getTimeDifference(cursor.getString(2), cursor.getString(3));

                String print = DataBaseHelper.dayOfWeekToString(dayOfWeek) + " " + date + "," + cursor.getString(2) + " - " + cursor.getString(3) + ","
                        + time[0] + "," + time[1] + "\n";

                outputStream.write(print.getBytes());

            } while (cursor.moveToPrevious());

            cursor.close();
            db.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a signature using the values stored in sharedPreferences
     * @return String signature of Format Name, IBAN\n
     */
    private String createSignature() {
        return sharedPreferences.getString(SettingsFragment.edtTxtSetName, "") + "," +
                sharedPreferences.getString(SettingsFragment.edtTxtSetIBAN, "") + ",," +
                sharedPreferences.getString(SettingsFragment.edtTxtSetRole, "") + "," +
                sharedPreferences.getString(SettingsFragment.edtTxtSetSalary, "") +" \n";
    }

    /**
     * Function to convert an Attendance ArrayList to a csv File using the given outputStream
     * @param attendanceModels ArrayList of AttendanceModels to be written to csv
     * @param outputStream Already configured Outputstream that will be used to write the file
     * @param addSignature Set whether a signature containing personal Data should be added.
     */
    public void attendanceArrayListToCsv(ArrayList<AttendanceModel> attendanceModels, OutputStream outputStream, boolean addSignature) {
        try {
            if(addSignature && sharedPreferences.getBoolean(SettingsFragment.switchPrefSetSignature, false) && signatureNotEmpty()){
                outputStream.write(createSignature().getBytes());
                outputStream.write("\n".getBytes());
            }

            outputStream.write("Datum, Zeit, Dauer, Dauer in min\n".getBytes());
            if (attendanceModels.size() == 0) {
                return;
            }

            for (int i = 0; i < attendanceModels.size(); i++) {
                String date = attendanceModels.get(i).getDate();
                String[] dateSplit = date.split("\\.");
                LocalDate localDate = LocalDate.of(Integer.parseInt(dateSplit[2]), Integer.parseInt(dateSplit[1]), Integer.parseInt(dateSplit[0]));
                int dayOfWeek = DayOfWeek.from(localDate).getValue();
                String[] time = DataBaseHelperAttendance.getTimeDifference(attendanceModels.get(i).getBegin(), attendanceModels.get(i).getEnd());

                String print = DataBaseHelper.dayOfWeekToString(dayOfWeek) + " " + date + "," + attendanceModels.get(i).getBegin() + " - " + attendanceModels.get(i).getEnd() + ","
                        + time[0] + "," + time[1] + "\n";

                outputStream.write(print.getBytes());
            }

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean signatureNotEmpty() {
        if(sharedPreferences.getString(SettingsFragment.edtTxtSetIBAN, "").equals("")
            && sharedPreferences.getString(SettingsFragment.edtTxtSetName, "").equals("")
            && sharedPreferences.getString(SettingsFragment.edtTxtSetRole, "").equals("")
            && sharedPreferences.getString(SettingsFragment.edtTxtSetSalary, "").equals("")){
            return false;
        }
        return true;
    }

    /**
     * Creates an Activity to choose the desired location to store a file. Launches the activityResultLauncher afterwards
     * @param pickerInitialUri URI for the directory that should be opened as per default
     * @param activity AppCompatActivity
     * @param activityResultLauncher ActivityResultLauncher<Intent> that will be called after the user chose a location
     */
    public void createFile(Uri pickerInitialUri, AppCompatActivity activity, ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "test.csv");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);


        activityResultLauncher.launch(intent);
    }


    public void alterDocument(Uri uri, AppCompatActivity activity) {
        try {
            ParcelFileDescriptor pfd = activity.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n").getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            Log.d("alter", "FileNotFound");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("alter", "IOException");
            e.printStackTrace();
        }
    }
}
