package com.example.turnen.ui.timePicker;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.turnen.data.DataBaseHelper;
import com.example.turnen.GridSpacingItemDecoration;
import com.example.turnen.MainActivity;
import com.example.turnen.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;


public class PickTimeFragment extends Fragment {

    public View view;
    public TimePickerViewModel timePickerViewModel;
    public FixtureViewModel fixtureViewModel;
    public DatesAdapter datesAdapter;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private DataBaseHelper dataBaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_pick_time, container, false);
        initObjects();
        initListeners();

        return view;
    }

    public void popDayPicker(View view) {
        ModalBottomSheet modalBottomSheet = new ModalBottomSheet();
        modalBottomSheet.show(requireActivity().getSupportFragmentManager(), "");
    }

    public void popTimePickerStart(View view) {
        Toast.makeText(getContext(), getResources().getString(R.string.TimePickerBeginning), Toast.LENGTH_SHORT).show();

        //Configure and Launch MaterialTimePicker
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder();
        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.setTimeFormat(TimeFormat.CLOCK_24H).build();
        materialTimePickerBuilder.setTitleText(getResources().getString(R.string.TimePickerBeginning));
        materialTimePicker.show(requireActivity().getSupportFragmentManager(), "");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerViewModel.timeStartSelectedHour = materialTimePicker.getHour();
                timePickerViewModel.timeStartSelectedMinute = materialTimePicker.getMinute();
                popTimePickerEnd(view);
            }
        });

        //clear ViewModel in case of cancel
        materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerViewModel.clear();
            }
        });
        materialTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                timePickerViewModel.clear();
            }
        });


    }

    public void popTimePickerEnd(View view) {
        Toast.makeText(getContext(), getResources().getString(R.string.TimePickerEnd), Toast.LENGTH_SHORT).show();

        //Configure and Launch MaterialTimePicker
        MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder();
        MaterialTimePicker materialTimePicker = materialTimePickerBuilder.setTimeFormat(TimeFormat.CLOCK_24H).build();
        materialTimePickerBuilder.setTitleText(getResources().getString(R.string.TimePickerEnd));
        materialTimePicker.show(requireActivity().getSupportFragmentManager(), "");

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerViewModel.timeEndSelectedHour = materialTimePicker.getHour();
                timePickerViewModel.timeEndSelectedMinute = materialTimePicker.getMinute();

                timePickerViewModel.convertToFixtures();

                for (int i = 0; i < timePickerViewModel.fixtures.size(); i++) {
                    dataBaseHelper.addOne(timePickerViewModel.fixtures.get(i));
                }

                dataBaseHelper.sort();
                datesAdapter.notifyDataSetChanged();
                timePickerViewModel.clear();
            }
        });

        //clear ViewModel in case of cancel
        materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerViewModel.clear();
            }
        });
        materialTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                timePickerViewModel.clear();
            }
        });

    }

    public void initListeners() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDayPicker(view);
            }
        });

        //FragmentResultListener for Fragment Result from ModalBottomSheet (selecting days)
        getParentFragmentManager().setFragmentResultListener(ModalBottomSheet.FragmentResultKey, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                //check whether days were selected if yes continue
                if (timePickerViewModel.daysSelected.size() > 0) {
                    popTimePickerStart(view);
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.selectDays), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void initObjects() {
        floatingActionButton = view.findViewById(R.id.floatingActionButtonPickTime);
        timePickerViewModel = new ViewModelProvider(requireActivity()).get(TimePickerViewModel.class);
        fixtureViewModel = new ViewModelProvider(requireActivity()).get(FixtureViewModel.class);
        recyclerView = view.findViewById(R.id.rvTimes);
        MainActivity activity = (MainActivity) requireActivity();
        dataBaseHelper = activity.getDataBaseHelper();
        fixtureViewModel.dataBaseHelper = dataBaseHelper;
        datesAdapter = new DatesAdapter(getContext(), fixtureViewModel);
        recyclerView.setAdapter(datesAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, 32, false));

    }

}
