package com.example.turnen.ui.timePicker;

import androidx.lifecycle.ViewModel;

import com.example.turnen.data.DataBaseHelper;
import com.example.turnen.data.FixtureModel;

import java.util.ArrayList;

public class FixtureViewModel extends ViewModel {

    public ArrayList<FixtureModel> fixturesSelected;
    public DataBaseHelper dataBaseHelper;

    public void clear(){
        fixturesSelected = null;
    }

    public FixtureModel getFixture(int position){
        return dataBaseHelper.getAllFixtures().get(position);
    }

    public boolean isEnabled(FixtureModel fixtureModel){
        return dataBaseHelper.isEnabled(fixtureModel);
    }

    public void deleteSelected(){
        for (int i = 0; i < fixturesSelected.size(); i++) {
            dataBaseHelper.deleteOne(fixturesSelected.get(i));
        }
    }

    public void disableSelected(){
        for (FixtureModel c : fixturesSelected) {
            if (dataBaseHelper.isEnabled(c)) {
                dataBaseHelper.setDisabled(c);
            } else {
                dataBaseHelper.setEnabled(c);
            }
        }
    }
}
