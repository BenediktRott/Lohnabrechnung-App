package com.example.turnen;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.turnen.data.BillingPeriodModel;
import com.example.turnen.data.DataBaseHelper;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.data.DataBaseHelperBillingPeriod;
import com.example.turnen.ui.HomeFragment.HomeViewModel;
import com.example.turnen.ui.ProfileFragment.ProfileFragment;
import com.example.turnen.ui.SettingsFragment.MyListener;
import com.example.turnen.ui.timePicker.DatesAdapter;
import com.example.turnen.ui.timePicker.FixtureViewModel;
import com.example.turnen.ui.timePicker.PickTimeFragment;
import com.example.turnen.ui.timePicker.TimePickerViewModel;
import com.example.turnen.ui.HomeFragment.HomeFragment;
import com.example.turnen.ui.SettingsFragment.SettingsFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MyListener {

    private static final int RC_SIGN_IN = 0;
    public TextView headerName;
    public TimePickerViewModel timePickerViewModel;
    public FixtureViewModel fixtureViewModel;
    public HomeViewModel homeViewModel;
    public DataBaseHelper dataBaseHelper;
    public DataBaseHelperAttendance dataBaseHelperAttendance;
    public DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod;
    public CSVWriter csvWriter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private View headerView;
    private TextView headerRole;
    private SharedPreferences sharedPreferences;
    private Context context;
    private PickTimeFragment pickTimeFragment;
    private ActionBar actionBar;
    private ActivityResultLauncher<Intent> activityResultLauncherPeriod;
    private SettingsFragment settingsFragment;
    private HomeFragment homeFragment;
    private GoogleSignInClient mGoogleSignInClient;
    private String savedPeriod = null;
    private static final String filename = "export.csv";
    private final ExecutorService executor = MyApp.executorService;
    private Integer loadingFlag = 0; //Default: 0, Objects and view initialized: 1, settings loaded sidebar not changed: 2, Finished: 3


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
        context = this;
        long startTime = System.nanoTime();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sharedPreferences = SettingsFragment.getOrCreateEncryptedSharedPreferences(context);
                synchronized(context){
                    if(loadingFlag == 1){
                        loadingFlag = 3;
                        headerName.setText(sharedPreferences.getString("edtTxtSetName", getResources().getString(R.string.missing)));
                        headerRole.setText(sharedPreferences.getString("edtTxtSetRole", getResources().getString(R.string.missing)));
                        if (headerName.getText().equals("")) {
                            headerName.setText(getString(R.string.missing));
                        } else if (headerRole.getText().equals("")) {
                            headerRole.setText(getString(R.string.missing));
                        }
                    }else{
                        loadingFlag = 2;
                    }
                }
            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        long endTime = System.nanoTime();
        Log.d("TimeMainInflate", "Time was: " + (endTime - startTime)/1000000);




        /*
        //delete export file in CacheDir whenever Activity is started
        File file = new File(context.getCacheDir(), filename);
        if(file.exists()){
            file.delete();
        }

         */

        initObjects();
        initToolbar();

        //check whether settings were already loaded, if so, don't change the flag, else indicate that objects have been initialized
        synchronized (context){
            loadingFlag = loadingFlag == 0 ? 1 : loadingFlag;
        }

        //Set fragment to HomeFragment if newly opened
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, homeFragment).commit();
        }

        navigationView.setNavigationItemSelectedListener(this);

        //Set name and role in header according to sharedPreferences, or, if none is given to R.string.missing

        synchronized (context){
            if(loadingFlag == 2){
                loadingFlag = 3;
                headerName.setText(sharedPreferences.getString("edtTxtSetName", getResources().getString(R.string.missing)));
                headerRole.setText(sharedPreferences.getString("edtTxtSetRole", getResources().getString(R.string.missing)));
                if (headerName.getText().equals("")) {
                    headerName.setText(getString(R.string.missing));
                } else if (headerRole.getText().equals("")) {
                    headerRole.setText(getString(R.string.missing));
                }
            }
        }


        //Hand this activity to Adapter
        DatesAdapter.activity = this;
        //AttendanceAdapter.activity = this;

        //activityResultLauncher for Exporting
        activityResultLauncherPeriod = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    //get passed Uri. If no data is given set uri to null (will be handled by try catch below)
                    Uri uri = data != null ? data.getData() : null;

                    //try to write the savedPeriod to a csvFile at the given Uri
                    OutputStream outputStream;
                    try {
                        outputStream = getContentResolver().openOutputStream(uri);
                        csvWriter.attendanceArrayListToCsv(dataBaseHelperAttendance.getAttendancesPeriod(savedPeriod, context), outputStream, false);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
                savedPeriod = null;
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        //GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        //        .requestEmail()
        //        .build();
        //mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //add the custom MyListener to the SettingsFragment, to listen for changes in
        //EncryptedSharedPreferences (onPreferenceChangeListener currently not working)
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        SettingsFragment.setListener(this);
    }

    @Override
    protected void onDestroy() {
        //delete export file in CacheDir whenever Activity is destroyed
        /*
        File file = new File(context.getCacheDir(), filename);
        if(file.exists()){
            file.delete();
        }

         */
        dataBaseHelper.getReadableDatabase().close();
        dataBaseHelper.getWritableDatabase().close();
        dataBaseHelperAttendance.getReadableDatabase().close();
        dataBaseHelperAttendance.getWritableDatabase().close();
        dataBaseHelperBillingPeriod.getReadableDatabase().close();
        dataBaseHelperBillingPeriod.getWritableDatabase().close();
        super.onDestroy();
    }

    //close drawer on back pressed, if drawer is open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuHome:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, homeFragment).commit();
                toolbar.setTitle("Turnen");
                break;
            case R.id.menuSettings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, settingsFragment).commit();
                toolbar.setTitle("Settings");
                break;
            case R.id.menuSetZeiten:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, pickTimeFragment).commit();
                toolbar.setTitle("Current Fixtures");
                break;
            case R.id.menuProfile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ProfileFragment()).commit();
                toolbar.setTitle("Profile");
                break;
            case R.id.menuExport:
                //prompt with BillingPeriodChooser

                /*
                promptPeriodChooser(new ExportRunnable() {
                    @Override
                    public void run(String period) {
                        //save the given period to variable savedPeriod for later use in activityResultLauncher
                        savedPeriod = period;
                        //get Documents directory as default displayed directory in the Dialog
                        Uri uriPeriod = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                        //launch Activity to choose the desired location
                        csvWriter.createFile(uriPeriod, MainActivity.this, activityResultLauncherPeriod);
                    }
                });
                 */
                BillingPeriodsAlertDialog.launchDialog(dataBaseHelperBillingPeriod, "", this,
                        getSupportFragmentManager(), this, new ExportRunnable() {
                            @Override
                            public void run(String period) {
                                //save the given period to variable savedPeriod for later use in activityResultLauncher
                                savedPeriod = period;
                                //get Documents directory as default displayed directory in the Dialog
                                Uri uriPeriod = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                                //launch Activity to choose the desired location
                                csvWriter.createFile(uriPeriod, MainActivity.this, activityResultLauncherPeriod);
                            }
                        }, null, null);
                break;
            case R.id.menuShare:
                //prompt with BillingPeriodChooser
                /*
                promptPeriodChooser(new ExportRunnable() {
                    //Is run after choosing the desired period
                    @Override
                    public void run(String period) {
                        File file = null;
                        file = new File(context.getCacheDir(), "export.csv");

                        Uri uriCsvFile = null;

                        try {
                            uriCsvFile = FileProvider.getUriForFile(context, "com.example.turnen.fileprovider", file);
                        } catch (IllegalArgumentException e) {
                            Log.e("File Selector",
                                    e.toString());
                            e.printStackTrace();
                        }

                        if (uriCsvFile != null) {
                            //write the current periods attendances to csv
                            OutputStream outputStream;
                            try {
                                outputStream = getContentResolver().openOutputStream(uriCsvFile);
                                csvWriter.attendanceArrayListToCsv(dataBaseHelperAttendance.getAttendancesPeriod(period, context), outputStream, true);
                                outputStream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            createShareDialog(uriCsvFile);

                        } else {
                            Intent shareIntent = new Intent();
                            shareIntent.setDataAndType(null, "");
                            MainActivity.this.setResult(RESULT_CANCELED,
                                    shareIntent);
                        }
                        file.deleteOnExit();
                    }
                });
                 */
                BillingPeriodsAlertDialog.launchDialog(dataBaseHelperBillingPeriod, "", this,
                        getSupportFragmentManager(), this, new ExportRunnable() {
                            //Is run after choosing the desired period
                            @Override
                            public void run(String period) {
                                File file = null;
                                file = new File(context.getCacheDir(), "export.csv");

                                Uri uriCsvFile = null;

                                try {
                                    uriCsvFile = FileProvider.getUriForFile(context, "com.example.turnen.fileprovider", file);
                                } catch (IllegalArgumentException e) {
                                    Log.e("File Selector",
                                            e.toString());
                                    e.printStackTrace();
                                }

                                if (uriCsvFile != null) {
                                    //write the current periods attendances to csv
                                    OutputStream outputStream;
                                    try {
                                        outputStream = getContentResolver().openOutputStream(uriCsvFile);
                                        csvWriter.attendanceArrayListToCsv(dataBaseHelperAttendance.getAttendancesPeriod(period, context), outputStream, true);
                                        outputStream.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    createShareDialog(uriCsvFile);

                                } else {
                                    Intent shareIntent = new Intent();
                                    shareIntent.setDataAndType(null, "");
                                    MainActivity.this.setResult(RESULT_CANCELED,
                                            shareIntent);
                                }
                                file.deleteOnExit();
                            }
                        }, null, null);
                break;
            case R.id.menuTest:
                File file = new File(context.getCacheDir(), "export.csv");
                if(file.exists()){
                    Toast.makeText(context, "File still exists", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Creates a share Dialog for the at the given Uri
     *
     * @param uri the Uri for the File to share
     */
    private void createShareDialog(Uri uri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType(getContentResolver().getType(uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(
                uri,
                getContentResolver().getType(uri));
        MainActivity.this.setResult(Activity.RESULT_OK,
                shareIntent);
        startActivity(Intent.createChooser(shareIntent, null));
    }

    public void initObjects() {

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);
        dataBaseHelper = new DataBaseHelper(MainActivity.this);



        timePickerViewModel = new ViewModelProvider(this).get(TimePickerViewModel.class);
        fixtureViewModel = new ViewModelProvider(this).get(FixtureViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        long startTime = System.nanoTime();
        //sharedPreferences = SettingsFragment.getOrCreateEncryptedSharedPreferences(this);


        headerView = navigationView.getHeaderView(0);
        headerName = headerView.findViewById(R.id.headerName);
        headerRole = headerView.findViewById(R.id.headerRole);

        //dataBaseHelper = new DataBaseHelper(MainActivity.this);
        dataBaseHelperAttendance = new DataBaseHelperAttendance(this);
        dataBaseHelperBillingPeriod = new DataBaseHelperBillingPeriod(this);
        csvWriter = new CSVWriter(sharedPreferences);

        initViewModels();

        homeFragment = new HomeFragment();
        pickTimeFragment = new PickTimeFragment();
        long endTime = System.nanoTime();
        Log.d("TimeMain", "Time was: " + (endTime - startTime)/1000000);
    }

    public void initViewModels(){
        homeViewModel.dataBaseHelperBillingPeriod = dataBaseHelperBillingPeriod;
        homeViewModel.dataBaseHelper = dataBaseHelper;
        homeViewModel.dataBaseHelperAttendance = dataBaseHelperAttendance;
        homeViewModel.month = LocalDate.now().getMonthValue();
        homeViewModel.year = LocalDate.now().getYear();
        homeViewModel.monthsString = getResources().getStringArray(R.array.months);
        homeViewModel.generateActiveDates();
        homeViewModel.setDate(DateHandler.getCurrentDate());
        homeViewModel.setTime(dataBaseHelper.getNearestTimeslot(DateHandler.getCurrentDayOfWeek(), DateHandler.getCurrentTime()));
        homeViewModel.setPeriod(dataBaseHelperBillingPeriod.getNearestPeriod(DateHandler.getCurrentDate()));
        homeViewModel.daysOfWeek = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.daysOfWeek)));
        homeViewModel.calendarDates = DateHandler.getCalendarDates(homeViewModel.year, homeViewModel.month);
    }

    public void initToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        toolbar.setTitleTextColor(getResources().getColor(R.color.menuIcon, getTheme()));
        toolbar.setOnMenuItemClickListener(this::onNavigationItemSelected);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigationDrawerOpen, R.string.navigationDrawerClose);
            //drawerLayout.addDrawerListener(toggle);
            //toggle.syncState();
        }
    }

    public DataBaseHelper getDataBaseHelper() {
        return dataBaseHelper;
    }

    public DataBaseHelperAttendance getDataBaseHelperAttendance() {
        return dataBaseHelperAttendance;
    }

    public DataBaseHelperBillingPeriod getDataBaseHelperBillingPeriod() {
        return dataBaseHelperBillingPeriod;
    }

    //Callback from SharedPreferenceDataStore when preferences were changed
    @Override
    public void callback(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsFragment.edtTxtSetName)) {
            headerName.setText(sharedPreferences.getString(SettingsFragment.edtTxtSetName, ""));
        } else if (key.equals(SettingsFragment.edtTxtSetRole)) {
            headerRole.setText(sharedPreferences.getString(SettingsFragment.edtTxtSetRole, ""));
        } else if (key.equals(SettingsFragment.edtTxtSetIBAN)) {
            EditTextPreference editTextPreference = settingsFragment.findPreference(SettingsFragment.edtTxtSetIBAN);
            String IBAN = sharedPreferences.getString(SettingsFragment.edtTxtSetIBAN, "");
            if (editTextPreference == null) {
                return;
            }
            if ((IBAN.equals("") || IBAN.equals(getString(R.string.notSet)))) {
                editTextPreference.setSummary(getString(R.string.notSet));
                return;
            }
            String summary = formatIBAN(IBAN);
            editTextPreference.setSummary(summary);
            editTextPreference.setDefaultValue("");
        }
    }


    /**
     * Returns a String Array containing the periods of the given billingPeriods and
     * in the last Element contains the currently selected period in R.id.txtViewCurrentPeriodSelected
     * As a default returns -1
     * @param billingPeriods
     * @return
     */
    private String[] getPeriods(ArrayList<BillingPeriodModel> billingPeriods) {
        TextView txtViewCurrentPeriod = findViewById(R.id.txtViewCurrentPeriodSelected);
        String[] items = new String[billingPeriods.size() + 2];
        int checkedPos = -1;
        if (txtViewCurrentPeriod == null) {
            for (int i = 0; i < billingPeriods.size(); i++) {
                items[i] = DateHandler.generatePeriod(billingPeriods.get(i).getDateStart(), billingPeriods.get(i).getDateEnd());
            }
        } else {
            for (int i = 0; i < billingPeriods.size(); i++) {
                items[i] = DateHandler.generatePeriod(billingPeriods.get(i).getDateStart(), billingPeriods.get(i).getDateEnd());
                checkedPos = items[i].contentEquals(txtViewCurrentPeriod.getText()) ? i : checkedPos;
            }
        }
        items[billingPeriods.size()] = getString(R.string.showAll);
        items[billingPeriods.size() + 1] = String.valueOf(checkedPos);
        return items;
    }

    /**
     * Prompts the user with Dialog to choose the billing period
     * @param onSelect ExportRunmable which run function will be executed on Select
     */
    private void promptPeriodChooser(ExportRunnable onSelect) {
        ArrayList<BillingPeriodModel> billingPeriods = dataBaseHelperBillingPeriod.getAllBillingPeriods();

        //split returned Array into items and the last item checkedPos
        String[] retArray = getPeriods(billingPeriods);
        String[] items = Arrays.copyOfRange(retArray, 0, retArray.length - 1);
        int checkedPos = Integer.parseInt(retArray[retArray.length - 1]);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.selectBillingPeriod));
        builder.setSingleChoiceItems(items, checkedPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog) dialog).getListView();
                lv.setTag(which);
            }
        });
        builder.setNeutralButton(getString(R.string.addNew), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.selectDate))
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
                materialDatePicker.show(getSupportFragmentManager(), "tag");
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Object>() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        String date = DateHandler.formatterDate.format(selection);
                        dataBaseHelperBillingPeriod.insert(date);
                        dataBaseHelperBillingPeriod.sort(); //might be possible to remove for better performance
                    }
                });
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog) dialog).getListView();
                int selected;
                if (lw.getTag() == null) {
                    //Tag is empty if no item was clicked so the item must be the previously located checkedPos
                    selected = checkedPos;
                } else {
                    selected = (Integer) lw.getTag();

                }

                if (selected == -1) {
                    Toast.makeText(context, "Nothing selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                onSelect.run(items[selected]);
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog) dialog).getListView();
                TextView txtViewCurrentPeriod = findViewById(R.id.txtViewCurrentPeriodSelected);
                TextView txtViewDate = findViewById(R.id.txtViewDate);
                int selected;

                if (lw.getTag() == null) {
                    //Tag is empty if no item was clicked so the item must be the previously located checkedPos
                    selected = checkedPos;
                } else {
                    selected = (Integer) lw.getTag();
                }

                if (selected == billingPeriods.size()) {
                    Toast.makeText(context, "Can't delete that object", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selected == -1) {
                    Toast.makeText(context, "Nothing selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                dataBaseHelperBillingPeriod.deleteOne(billingPeriods.get(selected));

                if (txtViewDate == null || txtViewCurrentPeriod == null) {

                } else {
                    txtViewCurrentPeriod.setText(dataBaseHelperBillingPeriod.getNearestPeriod(txtViewDate.getText().toString()));
                    homeViewModel.setPeriod(txtViewCurrentPeriod.getText().toString());
                }

            }
        });

        dialog = builder.create();
        dialog.show();
    }


    /**
     * @param iban The IBAN that will be formatted
     * @return Returns the given String with spaces after every fourth character and everything replaced by stars except for the last 6 characters
     */
    public static String formatIBAN(String iban) {
        iban = iban.replaceAll("\\s+", "");
        int cutoff;
        String rest = "";
        String stars;
        if (iban.length() > 6) {
            if (iban.length() <= 10) {
                //assure that at least 4 characters are replaced by stars
                cutoff = 4;
            } else {
                cutoff = iban.length() - 6;
            }
            //create cutoff amount of stars
            stars = String.join("", Collections.nCopies(cutoff, "*"));
            rest = iban.substring(cutoff);
        } else {
            cutoff = iban.length();
            stars = String.join("", Collections.nCopies(cutoff, "*"));
        }

        return (stars + rest).replaceAll("....", "$0" + " ");

    }

}