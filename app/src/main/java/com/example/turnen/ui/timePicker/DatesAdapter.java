package com.example.turnen.ui.timePicker;

import android.content.Context;
import android.content.res.TypedArray;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turnen.data.FixtureModel;
import com.example.turnen.R;

import java.util.ArrayList;

public class DatesAdapter extends RecyclerView.Adapter<DatesAdapter.DatesViewHolder> {

    private Context context;
    //private DataBaseHelper dataBaseHelper;
    private ActionMode actionMode;
    public static AppCompatActivity activity;
    private FixtureViewModel fixtureViewModel;

    public DatesAdapter(Context ct, FixtureViewModel fVM) {
        context = ct;
        //dataBaseHelper = db;
        fixtureViewModel = fVM;
    }

    @NonNull
    @Override
    public DatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_fixture, parent, false);
        return new DatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatesViewHolder holder, int position) {

        //FixtureModel currentFixture = dataBaseHelper.getAllFixtures().get(position);
        FixtureModel currentFixture = fixtureViewModel.getFixture(position);
        //holder.txtViewDaySelected.setText(dataBaseHelper.getAllFixtures().get(position).getDay());
        //holder.txtViewTimeStart.setText(dataBaseHelper.getAllFixtures().get(position).getTimeStart());
        //holder.txtViewTimeEnd.setText(dataBaseHelper.getAllFixtures().get(position).getTimeEnd());
        holder.txtViewDaySelected.setText(currentFixture.getDay());
        holder.txtViewTimeStart.setText(currentFixture.getTimeStart());
        holder.txtViewTimeEnd.setText(currentFixture.getTimeEnd());

        if (actionMode == null) {
            holder.unselect();
        }

        /*
        if(dataBaseHelper.isEnabled(currentFixture)){
            holder.enable();
        }else{
            holder.disable();
        }

         */

        if(fixtureViewModel.isEnabled(currentFixture)){
            holder.enable();
        }else{
            holder.disable();
        }

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (actionMode != null) {
                    return false;
                }
                actionMode = activity.startSupportActionMode(callback);
                if (fixtureViewModel.fixturesSelected == null) {
                    fixtureViewModel.fixturesSelected = new ArrayList<>();
                }
                fixtureViewModel.fixturesSelected.add(currentFixture);
                holder.select();
                return true;
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionMode == null) {
                    return;
                }
                if (holder.isSelected()) {
                    fixtureViewModel.fixturesSelected.remove(currentFixture);
                    holder.unselect();
                } else {
                    if (fixtureViewModel.fixturesSelected == null) {
                        fixtureViewModel.fixturesSelected = new ArrayList<>();
                    }
                    fixtureViewModel.fixturesSelected.add(currentFixture);
                    holder.select();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fixtureViewModel.dataBaseHelper.getAllFixtures().size();
    }

    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_toolbar_menu, menu);
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
            int itemId = item.getItemId();
            if (itemId == R.id.contextualDelete) {
                //for (int i = 0; i < fixtureViewModel.fixturesSelected.size(); i++) {
                //    dataBaseHelper.deleteOne(fixtureViewModel.fixturesSelected.get(i));
                //}
                fixtureViewModel.deleteSelected();
                activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.grey_900, context.getTheme()));
                actionMode = null;
            } else if (itemId == R.id.contextualDisable) {
                /*
                for (int i = 0; i < fixtureViewModel.fixturesSelected.size(); i++) {
                    if (dataBaseHelper.isEnabled(fixtureViewModel.fixturesSelected.get((i)))) {
                        dataBaseHelper.setDisabled(fixtureViewModel.fixturesSelected.get(i));
                    } else {
                        dataBaseHelper.setEnabled(fixtureViewModel.fixturesSelected.get(i));
                    }
                }
                 */
                fixtureViewModel.disableSelected();
            }
            fixtureViewModel.clear();
            notifyDataSetChanged();
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fixtureViewModel.clear();
            notifyDataSetChanged();
            activity.getWindow().setStatusBarColor(context.getResources().getColor(R.color.statusbarColor, context.getTheme()));
            actionMode = null;
        }
    };

    public class DatesViewHolder extends RecyclerView.ViewHolder {

        public TextView txtViewDaySelected;
        public TextView txtViewTimeStart;
        public TextView txtViewTimeEnd;
        public ConstraintLayout fixtureLayout, innerLayout;
        public View view;
        private boolean selected;
        private ImageView imgView;

        public DatesViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            txtViewDaySelected = itemView.findViewById(R.id.txtViewBillingPeriodDate);
            txtViewTimeStart = itemView.findViewById(R.id.txtViewTimeStart);
            txtViewTimeEnd = itemView.findViewById(R.id.txtViewTimeEnd);
            fixtureLayout = itemView.findViewById(R.id.fixtureLayout);
            innerLayout = itemView.findViewById(R.id.fixtureLayoutInner);
            imgView = itemView.findViewById(R.id.imgViewBillingPeriodIcon);
        }

        public boolean isSelected() {
            return selected;
        }

        public void select() {
            selected = true;
            txtViewDaySelected.setTextColor(getSecondaryColor());
            txtViewTimeEnd.setTextColor(getSecondaryColor());
            txtViewTimeStart.setTextColor(getSecondaryColor());
            int ps = fixtureLayout.getPaddingStart();
            int pe = fixtureLayout.getPaddingEnd();
            int pt = fixtureLayout.getPaddingTop();
            int pb = fixtureLayout.getPaddingBottom();
            fixtureLayout.setBackgroundResource(R.drawable.circle_corner_highlight);
            fixtureLayout.setPadding(ps, pt, pe, pb);
            imgView.setImageResource(R.drawable.ic_event_highlight);
        }

        public void unselect() {
            selected = false;

            txtViewDaySelected.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            txtViewTimeEnd.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            txtViewTimeStart.setTextColor(context.getResources().getColor(R.color.fixtureTextColor, context.getTheme()));
            int ps = fixtureLayout.getPaddingStart();
            int pe = fixtureLayout.getPaddingEnd();
            int pt = fixtureLayout.getPaddingTop();
            int pb = fixtureLayout.getPaddingBottom();
            fixtureLayout.setBackgroundResource(R.drawable.circle_corner);
            fixtureLayout.setPadding(ps, pt, pe, pb);
            imgView.setImageResource(R.drawable.ic_event);
        }

        public int getSecondaryColor(){
            TypedValue typedValue = new TypedValue();

            TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorSecondary });
            int color = a.getColor(0, 0);
            a.recycle();
            return color;
        }

        public void enable(){
            innerLayout.setForeground(null);
        }

        public void disable(){
            innerLayout.setForeground(AppCompatResources.getDrawable(context, R.drawable.strikethrough));
        }

    }


}
