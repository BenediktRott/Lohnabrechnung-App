package com.example.turnen.ui.HomeFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.turnen.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetCalendarItem extends BottomSheetDialogFragment {

    public View view;

    public BottomSheetCalendarItem() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bottomsheet_calendar_item, container, false);
        Log.d("onCreateView", "In OnCreateView");
        return view;
    }

}
