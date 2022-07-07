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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.DateHandler;
import com.example.turnen.data.AttendanceModel;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.R;

import java.util.ArrayList;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public AppCompatActivity activity;
    private Context context;
    //private DataBaseHelperAttendance dataBaseHelperAttendance;
    private HomeViewModel homeViewModel;
    private Runnable onActionModeClose;
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_toolbar_home, menu);
            mode.setTitle("");
            activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.black, context.getTheme()));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.contextualHomeDelete){
                homeViewModel.deleteAttendancesSelectedFromDatabase();
                activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.statusbarColor, context.getTheme()));
                homeViewModel.attendanceAdapterActionMode = null;
            }
            homeViewModel.attendancesSelected = null;

            //TODO: make more efficient
            onActionModeClose.run();
            homeViewModel.activeDates = homeViewModel.generateActiveDates();
            homeViewModel.attendanceCalendarAdapter.notifyDataSetChanged();
            homeViewModel.attendanceAdapter.notifyDataSetChanged();
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            onActionModeClose.run();
            homeViewModel.attendancesSelected = null;
            homeViewModel.attendanceAdapter.notifyDataSetChanged();
            activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.statusbarColor, context.getTheme()));
            homeViewModel.attendanceAdapterActionMode = null;
        }
    };

    public AttendanceAdapter(Context context, HomeViewModel homeViewModel, AppCompatActivity activity,
                             Runnable onActionModeClose, int day, int month, int year) {
        homeViewModel.attendanceAdapterDay = day;
        homeViewModel.attendanceAdapterMonth = month;
        homeViewModel.attendanceAdapterYear = year;
        homeViewModel.attendanceAdapter = this;
        homeViewModel.attendancesDay = homeViewModel.dataBaseHelperAttendance
                .getAttendancesDay(homeViewModel.attendanceAdapterDay, homeViewModel.attendanceAdapterMonth, homeViewModel.attendanceAdapterYear);
        this.onActionModeClose = onActionModeClose;
        this.activity = activity;
        this.context = context;
        this.homeViewModel = homeViewModel;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        //TODO: dedizierte funktion zum finden einer Attendance wegen Performance

        AttendanceModel currentAttendance = homeViewModel.attendancesDay.get(position);

        holder.txtViewDate.setText(currentAttendance.getDate());
        holder.txtViewTime.setText(DateHandler.generateTimePeriod(currentAttendance.getBegin(), currentAttendance.getEnd()));

        if(homeViewModel.attendanceAdapterActionMode == null){
            holder.unselect();
        }else if(homeViewModel.isAttendanceSelected(currentAttendance)){
            holder.select();
        }else {
            holder.unselect();
        }

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(homeViewModel.attendanceAdapterActionMode != null){
                    return false;
                }
                homeViewModel.attendanceAdapterActionMode = activity.startSupportActionMode(callback);

                if(homeViewModel.attendancesSelected == null){
                    homeViewModel.attendancesSelected = new ArrayList<>();
                }

                homeViewModel.attendancesSelected.add(currentAttendance);
                holder.select();
                return true;
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(homeViewModel.attendanceAdapterActionMode == null){
                    return;
                }
                Log.d("IsSelctedTest", "Currently: " + holder.isSelected());
                if(holder.isSelected()){
                    homeViewModel.unselectAttendance(currentAttendance);
                    holder.unselect();
                }else {
                    if(homeViewModel.attendancesSelected == null){
                        homeViewModel.attendancesSelected = new ArrayList<>();
                    }
                    homeViewModel.attendancesSelected.add(currentAttendance);
                    holder.select();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return homeViewModel.dataBaseHelperAttendance
                .getAttendancesDay(homeViewModel.attendanceAdapterDay, homeViewModel.attendanceAdapterMonth, homeViewModel.attendanceAdapterYear)
                .size();
    }

    public class AttendanceViewHolder extends RecyclerView.ViewHolder{
        public View view;
        public ImageView imageView;
        public TextView txtViewDate, txtViewTime;
        public ConstraintLayout outerLayout, innerLayout;
        boolean selected;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            txtViewDate = itemView.findViewById(R.id.txtViewBillingPeriodDate);
            txtViewTime = itemView.findViewById(R.id.txtViewBillingPeriodTime);
            outerLayout = itemView.findViewById(R.id.billingPeriodLayout);
            innerLayout = itemView.findViewById(R.id.billingPeriodLayoutInner);
            imageView = itemView.findViewById(R.id.imgViewBillingPeriodIcon);
            view = itemView;
        }

        public boolean isSelected(){ return selected;}

        public void select(){
            selected = true;
            txtViewDate.setTextColor(getSecondaryColor());
            txtViewTime.setTextColor(getSecondaryColor());

            int ps = outerLayout.getPaddingStart();
            int pe = outerLayout.getPaddingEnd();
            int pt = outerLayout.getPaddingTop();
            int pb = outerLayout.getPaddingBottom();
            outerLayout.setBackgroundResource(R.drawable.circle_corner_highlight);
            outerLayout.setPadding(ps, pt, pe, pb);

            imageView.setImageResource(R.drawable.ic_event_highlight);
        }

        public void unselect(){
            selected = false;

            txtViewDate.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            txtViewTime.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));

            int ps = outerLayout.getPaddingStart();
            int pe = outerLayout.getPaddingEnd();
            int pt = outerLayout.getPaddingTop();
            int pb = outerLayout.getPaddingBottom();
            outerLayout.setBackgroundResource(R.drawable.circle_corner);
            outerLayout.setPadding(ps, pt, pe, pb);
            imageView.setImageResource(R.drawable.ic_event);
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
