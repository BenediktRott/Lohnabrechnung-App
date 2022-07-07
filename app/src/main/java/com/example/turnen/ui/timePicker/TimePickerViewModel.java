package com.example.turnen.ui.timePicker;

import androidx.lifecycle.ViewModel;

import com.example.turnen.DateHandler;
import com.example.turnen.data.FixtureModel;

import java.util.ArrayList;

/**
 * ViewModel to store the data chosen in the ModalBottomSheet for picking the Days for a new Fixture
 */
public class TimePickerViewModel extends ViewModel {

    public ArrayList<String> daysSelected;
    public int timeStartSelectedHour, timeStartSelectedMinute, timeEndSelectedHour, timeEndSelectedMinute;
    public ArrayList<FixtureModel> fixtures;

    /**
     * Converts the currently stored data to an ArrayList of FixtureModels (doesn't clear afterwards)
     * @return Returns the generated ArrayList<FixtureModel>
     */
    public ArrayList<FixtureModel> convertToFixtures(){
        String timeStart = DateHandler.generateTime(timeStartSelectedHour, timeStartSelectedMinute);
        String timeEnd = DateHandler.generateTime(timeEndSelectedHour, timeEndSelectedMinute);
        if(fixtures == null){
            fixtures = new ArrayList<>();
        }
        for(int i = 0; i< daysSelected.size(); i++){
            //Generates new fixtures always as enabled
            fixtures.add(new FixtureModel(-1, daysSelected.get(i), timeStart, timeEnd, 1));
        }
        return fixtures;
    }

    /**
     * Clears all attributes of the ViewModel
     */
    public void clear(){
        timeStartSelectedHour = timeStartSelectedMinute = timeEndSelectedHour = timeEndSelectedMinute = 0;
        daysSelected = null;
        fixtures = null;
    }

    public String printFixtures(ArrayList<FixtureModel> fixtures){
        String res = "";
        for(int i = 0; i < fixtures.size(); i++){
            res = res + fixtures.get(i).toString();
        }
        return res;
    }

    public ArrayList<String> getTimesStart(){
        ArrayList<String> res = new ArrayList<>();
        if (fixtures == null){
            return null;
        }
        for(int i = 0; i < fixtures.size(); i++){
            res.add(fixtures.get(i).getTimeStart());
        }
        return res;
    }

    public ArrayList<String> getTimesEnd(){
        ArrayList<String> res = new ArrayList<>();
        if (fixtures == null){
            return null;
        }
        for(int i = 0; i < fixtures.size(); i++){
            res.add(fixtures.get(i).getTimeEnd());
        }
        return res;
    }

}
