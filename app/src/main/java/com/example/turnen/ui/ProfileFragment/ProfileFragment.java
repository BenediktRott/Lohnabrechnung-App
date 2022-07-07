package com.example.turnen.ui.ProfileFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.MainActivity;
import com.example.turnen.R;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.data.DataBaseHelperBillingPeriod;
import com.example.turnen.ui.SettingsFragment.SettingsFragment;

public class ProfileFragment extends Fragment {
    private TextView txtViewName, txtViewRole, txtViewSalary, txtViewHours;
    private RecyclerView recyclerView;
    private View view;
    private SharedPreferences sharedPreferences;
    private DataBaseHelperAttendance dataBaseHelperAttendance;
    private DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        txtViewName = view.findViewById(R.id.txtViewProfileName);
        txtViewRole = view.findViewById(R.id.txtViewProfileRole);
        txtViewSalary = view.findViewById(R.id.txtViewProfileSalary);
        txtViewHours = view.findViewById(R.id.txtViewProfileHours);
        recyclerView = view.findViewById(R.id.rvHours);

        sharedPreferences = SettingsFragment.getOrCreateEncryptedSharedPreferences(getContext());
        MainActivity activity = (MainActivity)requireActivity();
        dataBaseHelperAttendance = activity.getDataBaseHelperAttendance();
        dataBaseHelperBillingPeriod = activity.getDataBaseHelperBillingPeriod();

        txtViewName.setText(sharedPreferences.getString(SettingsFragment.edtTxtSetName, ""));
        txtViewRole.setText(sharedPreferences.getString(SettingsFragment.edtTxtSetRole, ""));
        txtViewSalary.setText(sharedPreferences.getString(SettingsFragment.edtTxtSetSalary, ""));
        txtViewHours.setText(dataBaseHelperAttendance.getTotalHours());

        ProfileAdapter profileAdapter = new ProfileAdapter(getContext(), dataBaseHelperBillingPeriod, dataBaseHelperAttendance);
        profileAdapter.activity = (MainActivity) getActivity();
        recyclerView.setAdapter(profileAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        return view;
    }
}
