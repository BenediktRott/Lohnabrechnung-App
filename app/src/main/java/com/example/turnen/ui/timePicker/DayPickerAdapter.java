package com.example.turnen.ui.timePicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class DayPickerAdapter extends RecyclerView.Adapter<DayPickerAdapter.DayPickerViewHolder> {

    private String[] days;
    private Context context;
    private View view;
    //public ArrayList<String> daysSelected;
    public TimePickerViewModel timePickerViewModel;
    private ArrayList<String> daysOfWeek;

    public DayPickerAdapter(Context ct, String[] data, TimePickerViewModel t) {
        timePickerViewModel = t;
        context = ct;
        days = data;
    }

    @NonNull
    @Override
    public DayPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //daysSelected = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_daypicker, parent, false);
        DayPickerViewHolder holder = new DayPickerViewHolder(view);

        daysOfWeek = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.daysOfWeek)));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DayPickerViewHolder holder, int position) {
        holder.textView.setText(days[position]);

        if(timePickerViewModel.daysSelected == null){
            timePickerViewModel.daysSelected = new ArrayList<>();
        }
        
        for(int i = 0; i < timePickerViewModel.daysSelected.size(); i++){
            if(timePickerViewModel.daysSelected.get(i).equals(daysOfWeek.get(position))){
                holder.select();

            }
        }
        
        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //switch alpha and create according Toast Messages
                if (holder.isSelected()){
                    holder.unselect();
                    //daysSelected.remove(holder.textView.getText().toString());
                    timePickerViewModel.daysSelected.remove(holder.textView.getText().toString());
                }
                else if(!holder.isSelected()){
                    holder.select();
                    //daysSelected.add(holder.textView.getText().toString());
                    timePickerViewModel.daysSelected.add(holder.textView.getText().toString());
                }else{
                    Toast.makeText(v.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    public class DayPickerViewHolder extends RecyclerView.ViewHolder{

        FloatingActionButton fab;
        TextView textView;
        boolean isSelected;

        public DayPickerViewHolder(@NonNull View itemView) {
            super(itemView);
            fab = itemView.findViewById(R.id.fabDay);
            textView = itemView.findViewById(R.id.fabTxtViewDay);
            isSelected = false;
        }


        public void select() {
            isSelected = true;
            fab.setAlpha(1.0F);
            textView.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnPrimary, context.getTheme()));
        }

        public void unselect(){
            isSelected = false;
            fab.setAlpha(0F);
            textView.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnTransparent, context.getTheme()));
        }

        public boolean isSelected() {
            return isSelected;
        }
    }


}
