package com.example.turnen.ui.ProfileFragment;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.R;
import com.example.turnen.data.BillingPeriodModel;
import com.example.turnen.data.DataBaseHelperAttendance;
import com.example.turnen.data.DataBaseHelperBillingPeriod;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private ArrayList<BillingPeriodModel> billingPeriods;
    private Context context;
    private DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod;
    private DataBaseHelperAttendance dataBaseHelperAttendance;
    private ActionMode actionMode;
    public AppCompatActivity activity;
    private ArrayList<ProfileViewHolder> selectedProfileViewHolders;


    public ProfileAdapter(Context context, DataBaseHelperBillingPeriod dataBaseHelperBillingPeriod, DataBaseHelperAttendance dataBaseHelperAttendance) {
        this.context = context;
        this.dataBaseHelperBillingPeriod = dataBaseHelperBillingPeriod;
        this.dataBaseHelperAttendance = dataBaseHelperAttendance;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_profile, parent, false);
        selectedProfileViewHolders = new ArrayList<>();
        this.billingPeriods = dataBaseHelperBillingPeriod.getAllBillingPeriods();
        return new ProfileAdapter.ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        holder.getTxtViewDate().setText(billingPeriods.get(position).getDate());
        holder.getTxtViewTime().setText(dataBaseHelperAttendance.getHoursPeriod(billingPeriods.get(position).getPeriod(), context));

        //TODO: Use ViewModel to store currently selected ViewHolders
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(actionMode != null){
                    return false;
                }

                actionMode = activity.startSupportActionMode(new androidx.appcompat.view.ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.contextual_toolbar_home, menu);
                        mode.setTitle("");
                        activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.black, context.getTheme()));
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                        if(item.getItemId() == R.id.contextualHomeDelete){
                            for(ProfileViewHolder c : selectedProfileViewHolders){
                                String[] begin = c.getTxtViewDate().getText().toString().replaceAll("\\s+", "").split("-");
                                BillingPeriodModel billingPeriodModel = dataBaseHelperBillingPeriod.findByBeginning(begin[0]);
                                dataBaseHelperBillingPeriod.deleteOne(billingPeriodModel);
                            }
                            billingPeriods = dataBaseHelperBillingPeriod.getAllBillingPeriods();
                            activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.statusbarColor, context.getTheme()));
                            actionMode = null;
                        }
                        notifyDataSetChanged();
                        selectedProfileViewHolders = new ArrayList<>();
                        mode.finish();
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                        holder.unselect();

                        for(ProfileViewHolder c : selectedProfileViewHolders){
                            c.unselect();
                        }

                        selectedProfileViewHolders = new ArrayList<>();
                        activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.statusbarColor, context.getTheme()));
                        actionMode = null;
                        notifyDataSetChanged();
                    }
                });

                holder.select();
                selectedProfileViewHolders.add(holder);
                return true;
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(actionMode == null){
                    return;
                }
                if(holder.isSelected()){
                    holder.unselect();
                    selectedProfileViewHolders.remove(holder);
                }else {
                    holder.select();
                    selectedProfileViewHolders.add(holder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataBaseHelperBillingPeriod.getAllBillingPeriods().size();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder{

        private TextView txtViewDate;
        private TextView txtViewTime;
        public View view;
        private boolean selected;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            txtViewDate = itemView.findViewById(R.id.txtViewProfileRVDate);
            txtViewTime = itemView.findViewById(R.id.txtViewProfileRVTime);
            this.view = itemView;
        }

        public TextView getTxtViewDate() {
            return txtViewDate;
        }

        public TextView getTxtViewTime() {
            return txtViewTime;
        }

        public boolean isSelected() {
            return selected;
        }

        public void select(){
            txtViewDate.setTextColor(getSecondaryColor());
            txtViewTime.setTextColor(getSecondaryColor());
            selected = true;
        }

        public void unselect(){
            txtViewTime.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            txtViewDate.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            selected = false;
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
