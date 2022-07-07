package com.example.turnen.ui.HomeFragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.DateHandler;
import com.example.turnen.R;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

public class AttendanceCalendarAdapter extends RecyclerView.Adapter<AttendanceCalendarAdapter.AttendanceViewHolder> {

    public AppCompatActivity activity;
    private Context context;
    private ActionMode actionMode;
    private HomeViewModel homeViewModel;
    private View view;
    public RVCallback rvCallback;
    public int chosenPos = -1;

    //CONSTANTS
    private static final int STATUS_BAR_ACTION_MODE_COLOR_ID = R.color.black;
    private static final int DAYS_TEXT_COLOR_ID = R.color.grey_400;

    private final ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_toolbar_home, menu);
            mode.setTitle("");
            activity.getWindow().setStatusBarColor(context.getColor(STATUS_BAR_ACTION_MODE_COLOR_ID));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.contextualHomeDelete){
                homeViewModel.deleteDatesSelectedFromDatabase();
                activity.getWindow().setStatusBarColor(context.getColor(R.color.statusbarColor));
                actionMode = null;
            }
            homeViewModel.datesSelected = null;
            notifyDataSetChanged();
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            homeViewModel.datesSelected = null;
            notifyDataSetChanged();
            activity.getWindow().setStatusBarColor(context.getColor(R.color.statusbarColor));
            actionMode = null;
        }
    };

    public AttendanceCalendarAdapter(Context context, HomeViewModel homeViewModel, AppCompatActivity activity,
                                     RVCallback rvCallback) {
        this.rvCallback = rvCallback;
        this.activity = activity;
        this.context = context;
        this.homeViewModel = homeViewModel;
        homeViewModel.attendanceCalendarAdapter = this;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        long startTime = System.nanoTime();
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_calendar, parent, false);
        long endTime = System.nanoTime();
        Log.d("TimeInflateCalendar", "Time was: " + (endTime - startTime)/1000000);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        //TODO: dedizierte funktion zum finden einer Attendance wegen Performance

        long startTime = System.nanoTime();

        if(actionMode == null){
            holder.unselect();
        }

        holder.removeOutline();

        Log.d("CalendarDates", Arrays.deepToString(homeViewModel.calendarDates));
        if(position < 7){
            holder.txtViewCalendarDay.setText(homeViewModel.daysOfWeek.get(position));
            holder.txtViewCalendarDay.setTextColor(context.getColor(DAYS_TEXT_COLOR_ID));
            holder.setInactive();
        }else{

            holder.txtViewCalendarDay.setText(String.valueOf(homeViewModel.calendarDates[(position/7) - 1][position%7]));

            //actively setting inactive necessary to avoid strange behaviour on e.g. resizing

            if(homeViewModel.calendarDates[(position/7) - 1][position%7] > position){

                //previous month
                holder.txtViewCalendarDay.setTextColor(context.getColor(R.color.calendarNotCurrentMonth));
                holder.currentMonthDist = -1;
                if(homeViewModel.activeDates[0][homeViewModel.calendarDates[(position/7) - 1][position%7]] >= 1){
                    holder.setActive();
                }else{
                    holder.setInactive();
                    holder.txtViewCalendarDay.setTextColor(context.getColor(R.color.calendarNotCurrentMonth));
                }

            }else if(homeViewModel.calendarDates[(position/7) - 1][position%7] < position - 14){

                //following month
                holder.txtViewCalendarDay.setTextColor(context.getColor(R.color.calendarNotCurrentMonth));
                holder.currentMonthDist = 1;
                if(homeViewModel.activeDates[2][homeViewModel.calendarDates[(position/7) - 1][position%7]] >= 1){
                    holder.setActive();
                }else{
                    holder.setInactive();
                    holder.txtViewCalendarDay.setTextColor(context.getColor(R.color.calendarNotCurrentMonth));
                }

            }else{
                //current month
                holder.currentMonthDist = 0;
                if(homeViewModel.activeDates[1][homeViewModel.calendarDates[(position/7) - 1][position%7]] >= 1){
                    holder.setActive();
                }else{
                    holder.setInactive();
                }
                if(homeViewModel.month == LocalDate.now().getMonthValue() &&
                        homeViewModel.calendarDates[(position/7) - 1][position%7] == LocalDate.now().getDayOfMonth()){

                    holder.setOutline();

                }
            }

            holder.btnCalendar.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(actionMode != null || !holder.active){
                        return false;
                    }

                    actionMode = activity.startSupportActionMode(callback);
                    notifyItemChanged(chosenPos);
                    if(homeViewModel.datesSelected == null){
                        homeViewModel.datesSelected = new ArrayList<>();
                    }
                    homeViewModel.addDateSelected(Integer.parseInt(holder.txtViewCalendarDay.getText().toString()),
                            holder.currentMonthDist);

                    holder.select();
                    return true;
                }
            });

            holder.btnCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(actionMode == null){
                        if(!holder.active){
                            if(homeViewModel.bottomSheetBehavior.getPeekHeight() == 0){
                                Log.d("ChosenPos", "You chose: " + chosenPos);
                                int posOld = chosenPos;
                                chosenPos = holder.getAdapterPosition();
                                holder.chosen();
                                notifyItemChanged(holder.getAdapterPosition());
                                notifyItemChanged(posOld);

                                int[] monthYear = DateHandler.offsetMonth(homeViewModel.month, homeViewModel.year, holder.currentMonthDist);
                                int day = Integer.parseInt(holder.txtViewCalendarDay.getText().toString());

                                homeViewModel.setDatePeriod(monthYear[1], monthYear[0], day);
                                homeViewModel.homeFragment.updateTimeDate();

                            }else{
                                rvCallback.inactiveCallback();
                            }
                        }else{
                            rvCallback.activeCallback(Integer.parseInt(holder.txtViewCalendarDay.getText().toString()), holder.currentMonthDist);
                        }
                        return;
                    }
                    if(holder.selected){
                        homeViewModel.deleteDateSelected(Integer.parseInt(holder.txtViewCalendarDay.getText().toString()),
                                holder.currentMonthDist);
                        holder.unselect();
                    }else{
                        if(holder.active){
                            homeViewModel.addDateSelected(Integer.parseInt(holder.txtViewCalendarDay.getText().toString()),
                                    holder.currentMonthDist);
                            holder.select();
                        }
                    }
                }
            });

            boolean equalsDate = DateHandler.datesEqual(homeViewModel.getDate(),
                    DateHandler.offsetMonth(homeViewModel.month, homeViewModel.year, holder.currentMonthDist),
                    Integer.parseInt(holder.txtViewCalendarDay.getText().toString()));

            if(chosenPos == -1 && equalsDate && actionMode == null){
                chosenPos = holder.getAdapterPosition();
                holder.chosen();
            }

            if(position == chosenPos && actionMode == null){
                Log.d("InChosen", "In Chosen");
                holder.chosen();
            }else{
                //holder.notChosen();
            }

        }

        long endTime = System.nanoTime();
        Log.d("TimeBindCalendar", "Time was: " + (endTime - startTime)/1000000);

    }

    @Override
    public int getItemCount() {
        return (homeViewModel.calendarDates.length + 1)*7;
    }

    public class AttendanceViewHolder extends RecyclerView.ViewHolder{
        public MaterialButton btnCalendar;
        public TextView txtViewCalendarDay;
        public boolean active;
        public boolean outlined;
        public boolean selected;
        public int currentMonthDist = 0; //-1: previous Month, 0: current Month, 1: next Month

        //CONSTANTS
        private static final float INACTIVE_ALPHA = 0.5F;
        private static final int STROKE_WIDTH = 5;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCalendar = itemView.findViewById(R.id.btnCalendar);
            txtViewCalendarDay = itemView.findViewById(R.id.txtViewCalendarDay2);
        }

        public boolean isActive(){ return active;}

        public void toggleActive(){
            if(active){
                setInactive();
            }else{
                setActive();
            }
        }

        public void setActive(){
            active = true;
            if(currentMonthDist == 0){
                btnCalendar.setAlpha(1F);
            }else{
                btnCalendar.setAlpha(INACTIVE_ALPHA);
            }
            btnCalendar.setBackgroundColor(context.getColor(R.color.toolbarColor));
            txtViewCalendarDay.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnPrimary, context.getTheme()));
        }

        public void setInactive(){
            active = false;
            btnCalendar.setAlpha(0F);
            txtViewCalendarDay.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnTransparent, context.getTheme()));
        }

        public void setOutline(){
            if(!active){
                btnCalendar.setAlpha(1.0F);
                //TODO: Get transparent doesn't work currently
                btnCalendar.setBackgroundColor(context.getColor(R.color.background));
                //btnCalendar.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.background));
            }
            btnCalendar.setStrokeWidth(STROKE_WIDTH);
            outlined = true;
        }

        public void removeOutline(){
            if(!active){
                btnCalendar.setAlpha(0.0F);
                btnCalendar.setBackgroundColor(context.getColor(R.color.toolbarColor));
            }
            btnCalendar.setStrokeWidth(0);
            outlined = false;
        }

        public void select(){
            selected = true;
            btnCalendar.setAlpha(1F);

            btnCalendar.setBackgroundColor(getSecondaryColor());
            txtViewCalendarDay.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnPrimary, context.getTheme()));
        }

        public void unselect(){
            selected = false;
            if(active){
                setActive();
            }else{
                setInactive();
            }
        }

        public void chosen(){
            if(!active){
                btnCalendar.setAlpha(1.0F);
                btnCalendar.setBackgroundColor(getSecondaryColor());
                txtViewCalendarDay.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnPrimary, context.getTheme()));
            }else{
                btnCalendar.setStrokeWidth(STROKE_WIDTH);
                btnCalendar.setStrokeColorResource(R.color.calendarHighlight);
            }
        }

        public void notChosen(){
            btnCalendar.setAlpha(1.0F);
            btnCalendar.setBackgroundColor(context.getColor(R.color.background));
            txtViewCalendarDay.setTextColor(context.getResources().getColor(R.color.dayPickerTextColorOnTransparent, context.getTheme()));
        }


        public int getSecondaryColor(){
            TypedValue typedValue = new TypedValue();

            TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorSecondary });
            int color = a.getColor(0, 0);
            a.recycle();
            return color;
        }


    }
}
