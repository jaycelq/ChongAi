package me.qiang.android.chongai.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiQiang on 17/1/15.
 */
public class StateExploreManager {
    private List<StateItem> statesList;
    private static StateExploreManager stateExploreManager;

    private StateExploreManager() {
        statesList = new ArrayList<>();

        //TODO: Persistent Storage on last time item
        for (int i = 0; i < 9; i++) {
            statesList.add(new StateItem());
        }
    }

    public static StateExploreManager getStateExploreManager() {
        if (stateExploreManager == null)
            stateExploreManager = new StateExploreManager();
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

}
