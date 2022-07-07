package com.example.turnen.ui.timePicker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.R;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheet extends BottomSheetDialogFragment {

    private View view;
    private RecyclerView recyclerView;
    private String[] days;
    private DayPickerAdapter dayPickerAdapter;
    //private GridLayoutManager gridLayoutManager;
    private FlexboxLayoutManager flexboxLayoutManager;
    public Button btnSelect;
    private Button btnCancel;
    private TimePickerViewModel timePickerViewModel;
    public static String FragmentResultKey = "DaySelect";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.modal_bottom_daypicker_rv, container, false);
        initObjects();

        //recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, 5, true));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //timePickerViewModel.daysSelected = null;
                timePickerViewModel.clear();
                dismiss();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle result = new Bundle();
                //result.putStringArrayList("daysSelected", timePickerViewModel.daysSelected);
                getParentFragmentManager().setFragmentResult(FragmentResultKey, result);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        timePickerViewModel.clear();
        super.onCancel(dialog);
    }

    public void initObjects(){
        days = getResources().getStringArray(R.array.daysOfWeek);
        timePickerViewModel = new ViewModelProvider(requireActivity()).get(TimePickerViewModel.class);
        recyclerView = view.findViewById(R.id.rvDays);
        dayPickerAdapter = new DayPickerAdapter(getContext(), days, timePickerViewModel);
        recyclerView.setAdapter(dayPickerAdapter);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSelect = view.findViewById(R.id.btnSelect);


        //Maybe keep as backup
        //gridLayoutManager = new GridLayoutManager(getContext(), 1);
        //gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);

        //recyclerView.setLayoutManager(gridLayoutManager);

        flexboxLayoutManager = new FlexboxLayoutManager(getContext());
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setFlexWrap(FlexWrap.NOWRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        recyclerView.setLayoutManager(flexboxLayoutManager);
    }


}
