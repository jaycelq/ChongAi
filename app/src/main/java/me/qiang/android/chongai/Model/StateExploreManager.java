package me.qiang.android.chongai.Model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.GlobalApplication;

/**
 * Created by LiQiang on 17/1/15.
 */
public class StateExploreManager {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;
    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "PiPiChongStates";

    // All Shared Preferences Keys
    private static final String LAST_STATES_EXPLORED = "lastStates";

    private List<StateItem> statesList;
    private static StateExploreManager stateExploreManager;

    private Type stateItemListType;

    private StateExploreManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();

        statesList = new ArrayList<>();

        Gson gson = new Gson();
        stateItemListType = new TypeToken<ArrayList<StateItem>>(){}.getType();

        String lastStatesExplored = pref.getString(LAST_STATES_EXPLORED, null);
        if(lastStatesExplored != null) {
            statesList = gson.fromJson(lastStatesExplored, stateItemListType);
        }
        else {
            for (int i = 0; i < 9; i++) {
                statesList.add(new StateItem());
            }
        }
    }

    public static StateExploreManager getStateExploreManager() {
        if (stateExploreManager == null)
            stateExploreManager = new StateExploreManager(GlobalApplication.getAppContext());
        return stateExploreManager;
    }

    public int stateCount() {
        return statesList.size();
    }

    public StateItem get(int i) {
        return statesList.get(i);
    }

    public void push(StateItem stateItem) {
        statesList.add(0, stateItem);
    }

    public void updateStatesList(ArrayList<StateItem> statesList) {
        this.statesList = statesList;
    }

}
