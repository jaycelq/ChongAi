package me.qiang.android.chongai.util;

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

/**
 * Created by LiQiang on 17/1/15.
 */
public class StateExploreManager {
    private List<StateItem> statesList;
    private static StateExploreManager stateExploreManager;
    private GetNewStatesHttpClient getNewStatesHttpClient;

    private StateExploreManager() {
        statesList = new ArrayList<>();
        getNewStatesHttpClient = new GetNewStatesHttpClient();

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
