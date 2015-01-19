package me.qiang.android.chongai.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Fragment.StateFragment;
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
    private GetNewStatesHttpClient getNewStatesHttpClient;

    private StateExploreManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();

        statesList = new ArrayList<>();
        getNewStatesHttpClient = new GetNewStatesHttpClient();

        Gson gson = new Gson();
        String lastStatesExplored = pref.getString(LAST_STATES_EXPLORED, null);
        if(lastStatesExplored != null) {
            StateRefreshResults stateRefreshResults = gson.fromJson(lastStatesExplored, StateRefreshResults.class);
            statesList = stateRefreshResults.newStatesList;
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

    public void getNewStates(StateFragment.StateAdapter listViewAdapter, PullToRefreshListView listView) {
        getNewStatesHttpClient.getNewStates(listViewAdapter, listView);
    }

    public class StateRefreshResults {
        public int status;

        @SerializedName("body")
        public List<StateItem> newStatesList;
    }

    public class GetNewStatesHttpClient {

        GetNewStatesHttpClient() {
        }

        public void getNewStates(final StateFragment.StateAdapter listViewAdapter, final PullToRefreshListView listView){
            RequestParams params = new RequestParams();
            HttpClient.get("post/getNewPosts", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i("JSON", response.toString());

                    Gson gson = new Gson();
                    StateRefreshResults stateRefreshResults = gson.fromJson(response.toString(), StateRefreshResults.class);

                    Log.i("GSON", stateRefreshResults.newStatesList.size()+ "");
                    statesList = stateRefreshResults.newStatesList;

                    listViewAdapter.notifyDataSetChanged();
                    listView.onRefreshComplete();
                    editor.putString(LAST_STATES_EXPLORED, response.toString());
                    editor.commit();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i("JSON", "JSON FAIL");

                    listView.onRefreshComplete();
                }
            });
        }
    }
}
