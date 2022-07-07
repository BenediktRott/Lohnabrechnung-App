package com.example.turnen.ui.HomeFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.BillingPeriodsAlertDialog;
import com.example.turnen.DateHandler;
import com.example.turnen.MainActivity;
import com.example.turnen.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDate;
import java.util.Date;

public class HomeFragment extends Fragment {

    private View view, parentLayout;
    private TextView txtViewDate, txtViewTime, txtViewCurrentPeriod, txtViewCalendarMonth;
    private ImageView btnDateLeft, btnDateRight, btnTimeLeft, btnTimeRight, btnMonthLeft, btnMonthRight;
    private Button buttonEnter, buttonCancel;
    private HomeViewModel homeViewModel;
    private AttendanceCalendarAdapter attendanceCalendarAdapter;
    private RecyclerView recyclerView;
    private ConstraintLayout layoutTop;
    private Vibrator vibrator;
    private MainActivity activity;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private BottomSheetCalendarItem bottomSheet;
    private View standardBottomSheet;

    //callback for when an active Calendar Item is clicked
    private final RVCallback rvCallback = new RVCallback() {
        @Override
        public void activeCallback(int day, int monthOffset) {
            bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_EXPANDED);
            recyclerView = view.findViewById(R.id.rvBottomSheet);

            int[] monthYear = DateHandler.offsetMonth(homeViewModel.month, homeViewModel.year, monthOffset);

            AttendanceAdapter attendanceAdapter = new AttendanceAdapter(requireContext(), homeViewModel, activity,
                    onActionModeClose, day, monthYear[0], monthYear[1]);
            recyclerView.setAdapter(attendanceAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));

        }

        //triggered when clicking an inactive item in AttendanceCalendarAdapter
        @Override
        public void inactiveCallback() {
            if(bottomSheetBehavior.getPeekHeight() != 0){
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

    };

    //callback for when the actionMode of the AttendanceAdapter is closed/destroyed
    private final Runnable onActionModeClose = new Runnable() {
        @Override
        public void run() {
            if(bottomSheetBehavior.getPeekHeight() != 0){
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    };

    //CONSTANTS
    private static final int VIBRATION_TIME = 100;
    private static final int PEEK_HEIGHT_EXPANDED = 500;
    private static final int TIMEPICKER_FORMAT = TimeFormat.CLOCK_24H;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        long startTime = System.nanoTime();
        view = inflater.inflate(R.layout.fragment_home, container, false);

        initObjects();
        initBottomSheet();
        initListeners();

        homeViewModel.homeFragment = this;

        //Create Calendar recyclerView
        recyclerView.setAdapter(attendanceCalendarAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));

        txtViewDate.setText(homeViewModel.getDate());
        txtViewTime.setText(homeViewModel.getTime());
        txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
        txtViewCalendarMonth.setText(homeViewModel.getMonthYearString());

        long endTime = System.nanoTime();
        Log.d("TimeHome", "Time was: " + (endTime - startTime)/1000000);

        return view;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initBottomSheet() {
        //init bottomSheet related objects
        bottomSheet = new BottomSheetCalendarItem();
        standardBottomSheet = view.findViewById(R.id.standard_bottomsheet);
        Log.d("NullTest", String.valueOf(standardBottomSheet == null));
        bottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        homeViewModel.bottomSheetBehavior = bottomSheetBehavior;
        homeViewModel.bottomSheet = bottomSheet;

        //init listeners for closing the bottomSheet;

        //handle backPress, when the bottomSheet is expanded
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(bottomSheetBehavior.getPeekHeight() != 0){
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //handle tapping outside the bottomSheet to close it
        parentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(bottomSheetBehavior.getPeekHeight() != 0 && event.getAction() == MotionEvent.ACTION_UP){
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                v.performClick();
                parentLayout.performClick();
                //TODO: Might have to change to false in case of problems
                return true;
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(bottomSheetBehavior.getPeekHeight() != 0 && event.getAction() == MotionEvent.ACTION_UP){
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                recyclerView.performClick();
                v.performClick();
                return false;
            }
        });
    }


    private void initObjects() {
        //Views
        parentLayout = view.findViewById(R.id.parentLayout);
        txtViewCurrentPeriod = view.findViewById(R.id.txtViewCurrentPeriodSelected);
        layoutTop = view.findViewById(R.id.layoutTop);
        recyclerView = view.findViewById(R.id.rvHome);
        txtViewDate = view.findViewById(R.id.txtViewDate);
        txtViewTime = view.findViewById(R.id.txtViewTime);
        btnDateLeft = view.findViewById(R.id.btnDateLeft);
        btnDateRight = view.findViewById(R.id.btnDateRight);
        btnTimeLeft = view.findViewById(R.id.btnTimeLeft);
        btnTimeRight = view.findViewById(R.id.btnTimeRight);
        buttonEnter = view.findViewById(R.id.btnEnter);
        buttonCancel = view.findViewById(R.id.btnCancel);
        btnMonthRight = view.findViewById(R.id.btnMonthRight);
        btnMonthLeft = view.findViewById(R.id.btnMonthLeft);
        txtViewCalendarMonth = view.findViewById(R.id.txtViewCalendarMonth);

        //Model
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        //Controller
        activity = (MainActivity) requireActivity();
        attendanceCalendarAdapter = new AttendanceCalendarAdapter(requireContext(), homeViewModel, (MainActivity) requireActivity(), rvCallback);

        //Other
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initListeners() {
        txtViewDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.selectDate))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            materialDatePicker.show(getParentFragmentManager(), "tag");
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Object>() {
                @Override
                public void onPositiveButtonClick(Object selection) {
                    homeViewModel.setDate(DateHandler.formatterDate.format(selection));
                    LocalDate selectedDate = DateHandler.toLocalDate(homeViewModel.getDate());
                    homeViewModel.setTime(homeViewModel.dataBaseHelper.getNearestTimeslot(selectedDate.getDayOfWeek().getValue(), DateHandler.formatterTime.format(new Date())));
                    homeViewModel.setPeriod(homeViewModel.dataBaseHelperBillingPeriod.getNearestPeriod(homeViewModel.getDate()));

                    txtViewDate.setText(homeViewModel.getDate());
                    txtViewTime.setText(homeViewModel.getTime());
                    txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                }
            });
        });

        txtViewTime.setOnClickListener(v -> popTimePickerStart());

        btnDateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.previousDate(txtViewDate.getText().toString());
                txtViewDate.setText(homeViewModel.getDate());
                txtViewTime.setText(homeViewModel.getTime());
                txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                attendanceCalendarAdapter.notifyDataSetChanged();
            }
        });
        btnDateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.nextDate(txtViewDate.getText().toString());
                txtViewDate.setText(homeViewModel.getDate());
                txtViewTime.setText(homeViewModel.getTime());
                txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                attendanceCalendarAdapter.notifyDataSetChanged();
            }
        });

        btnTimeLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.previousTime(requireContext());
                txtViewTime.setText(homeViewModel.getTime());
            }
        });
        btnTimeRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.nextTime(requireContext());
                txtViewTime.setText(homeViewModel.getTime());
            }
        });

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.submitAttendance(requireContext());
                attendanceCalendarAdapter.notifyDataSetChanged();
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = homeViewModel.removeAttendance(requireContext());
                if (!b) {
                    //TODO: b doesn't return correct value
                    Toast.makeText(getContext(), "Nothing to delete", Toast.LENGTH_SHORT).show();
                }
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
                attendanceCalendarAdapter.notifyDataSetChanged();


            }
        });

        layoutTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingPeriodsAlertDialog.launchDialog(homeViewModel.dataBaseHelperBillingPeriod, txtViewCurrentPeriod.getText().toString(),
                        activity, getParentFragmentManager(), getContext(), period -> {
                            //onSelect
                            homeViewModel.setPeriod(period);
                            txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                            attendanceCalendarAdapter.notifyDataSetChanged();
                        }, period -> {
                            //onNew
                            homeViewModel.setPeriod(period);
                            txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                            attendanceCalendarAdapter.notifyDataSetChanged();
                        }, period -> {
                            //onDelete
                            homeViewModel.setPeriod(homeViewModel.dataBaseHelperBillingPeriod.getNearestPeriod(txtViewDate.getText().toString()));
                            txtViewCurrentPeriod.setText(homeViewModel.getPeriod());
                            attendanceCalendarAdapter.notifyDataSetChanged();
                        });
            }
        });

        btnMonthRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.nextMonth();
                txtViewCalendarMonth.setText(homeViewModel.getMonthYearString());
                attendanceCalendarAdapter.chosenPos = -1;
                attendanceCalendarAdapter.notifyDataSetChanged();
            }
        });
        btnMonthLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.previousMonth();
                txtViewCalendarMonth.setText(homeViewModel.getMonthYearString());
                attendanceCalendarAdapter.chosenPos = -1;
                attendanceCalendarAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onCancelDialog(){
        homeViewModel.setTime(txtViewTime.getText().toString());
        homeViewModel.setDate(txtViewDate.getText().toString());
    }

    private void popTimePickerStart() {
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder();
        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.setTimeFormat(TimeFormat.CLOCK_24H).build();
        materialTimePickerBuilder.setTitleText(getResources().getString(R.string.TimePickerBeginning));
        materialTimePicker.show(requireActivity().getSupportFragmentManager(), "");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.setTime(DateHandler.generateTime(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                popTimePickerEnd();
            }
        });
        materialTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onCancelDialog();
            }
        });

        materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelDialog();
            }
        });
    }

    private void popTimePickerEnd() {
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder();
        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.setTimeFormat(TIMEPICKER_FORMAT).build();
        materialTimePickerBuilder.setTitleText(getResources().getString(R.string.TimePickerBeginning));
        materialTimePicker.show(requireActivity().getSupportFragmentManager(), "");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.setTime(DateHandler.generateTimePeriod(homeViewModel.getTime(),
                        DateHandler.generateTime(materialTimePicker.getHour(), materialTimePicker.getMinute())));
                txtViewTime.setText(homeViewModel.getTime());
            }
        });
        materialTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onCancelDialog();
            }
        });
        materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelDialog();
            }
        });
    }

    public void updateTimeDate(){
        txtViewDate.setText(homeViewModel.date);
        txtViewTime.setText(homeViewModel.time);
        txtViewCurrentPeriod.setText(homeViewModel.period);
    }

}
