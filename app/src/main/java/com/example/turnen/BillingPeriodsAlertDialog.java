package com.example.turnen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.example.turnen.data.BillingPeriodModel;
import com.example.turnen.data.DataBaseHelperBillingPeriod;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.util.ArrayList;

public abstract class BillingPeriodsAlertDialog {


    public static void launchDialog(DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod,
                                    String currentPeriod, Activity activity, FragmentManager fragmentManager,
                                    Context context,
                                    ExportRunnable onSelect, @Nullable ExportRunnable onNew, @Nullable ExportRunnable onDelete){

        ArrayList<BillingPeriodModel> billingPeriods = dataBaseHelperBillingPeriod.getAllBillingPeriods();
        String[] items = new String[billingPeriods.size() + 1];
        int checkedPos = -1;
        for (int i = 0; i < billingPeriods.size(); i++) {
            items[i] = DateHandler.generatePeriod(billingPeriods.get(i).getDateStart(), billingPeriods.get(i).getDateEnd());
            checkedPos = items[i].contentEquals(currentPeriod) ? i : checkedPos;
            Log.d("checked", String.valueOf(checkedPos));
        }
        items[billingPeriods.size()] = activity.getString(R.string.showAll);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(activity.getString(R.string.selectBillingPeriod));
        builder.setSingleChoiceItems(items, checkedPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog) dialog).getListView();
                lv.setTag(which);
            }
        });

        builder.setPositiveButton(activity.getString(R.string.select), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog) dialog).getListView();
                int selected;
                if (lw.getTag() == null) {
                    int checkedPosInner = -1;
                    for (int i = 0; i < billingPeriods.size(); i++) {
                        checkedPosInner = items[i].contentEquals(currentPeriod) ? i : checkedPosInner;
                    }
                    selected = checkedPosInner;
                } else {
                    selected = (Integer) lw.getTag();
                }

                if (selected == -1) {
                    Toast.makeText(context, "Nothing selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                String selectedPeriod = items[selected];
                //txtViewCurrentPeriod.setText(items[selected]);
                //homeViewModel.setPeriod(txtViewCurrentPeriod.getText().toString());
                //attendanceAdapter.notifyDataSetChanged();
                onSelect.run(selectedPeriod);
            }
        });

        if(onNew != null){
            builder.setNeutralButton("New", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText(activity.getString(R.string.selectDate))
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build();
                    materialDatePicker.show(fragmentManager, "tag");

                    materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Object>() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            String date = DateHandler.formatterDate.format(selection);
                            dataBaseHelperBillingPeriod.insert(date);
                            dataBaseHelperBillingPeriod.sort();
                            String selectedPeriod = DateHandler.generatePeriod(
                                    dataBaseHelperBillingPeriod.findByBeginning(date).getDateStart(),
                                    dataBaseHelperBillingPeriod.findByBeginning(date).getDateEnd());
                            onNew.run(selectedPeriod);
                        }
                    });
                }
            });
        }

        if(onDelete != null){
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView lw = ((AlertDialog) dialog).getListView();

                    int selected;
                    if (lw.getTag() == null) {
                        int checkedPosInner = -1;
                        for (int i = 0; i < billingPeriods.size(); i++) {
                            //String[] itemsInner = new String[billingPeriods.size()];
                            //itemsInner[i] = DateHandler.generatePeriod(billingPeriods.get(i).getDateStart(), billingPeriods.get(i).getDateEnd());
                            checkedPosInner = items[i].contentEquals(currentPeriod) ? i : checkedPosInner;
                        }
                        selected = checkedPosInner;
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
                    onDelete.run(items[selected]);
                }
            });
        }


        dialog = builder.create();
        dialog.show();
    }
}
