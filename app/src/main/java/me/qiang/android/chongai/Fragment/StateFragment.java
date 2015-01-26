package me.qiang.android.chongai.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import me.qiang.android.chongai.Activity.CommentActivity;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.RequestServer;
import me.qiang.android.chongai.widget.StateItemView;

public class StateFragment extends BaseFragment {

    // UI widget
    private PullToRefreshListView pullToRefreshView;
    private View mFooterView;
    private View loading;
    private TextView noMoreStates;
    private ProgressBar footerLoading;
    private ProgressDialog barProgressDialog;

    // Adapter and state manager related to list view
    private StateAdapter mAdapter;
    private StateExploreManager stateExploreManager;

    private Context context;

    private boolean noMore = false;

    public StateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        stateExploreManager = StateExploreManager.getStateExploreManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_state_list, container, false);

        barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        pullToRefreshView = (PullToRefreshListView) rootView.findViewById(R.id.my_list);
        mFooterView = inflater.inflate(R.layout.loading, null);
        loading = mFooterView.findViewById(R.id.loading_layout);
        noMoreStates = (TextView) mFooterView.findViewById(R.id.loading_nomore);
        footerLoading = (ProgressBar) mFooterView.findViewById(R.id.loading);
        pullToRefreshView.getRefreshableView().addFooterView(mFooterView);
        // set the color of progress dialog
        footerLoading.getIndeterminateDrawable().setColorFilter(0xff7FB446,
                android.graphics.PorterDuff.Mode.SRC_ATOP);

        mAdapter = new StateAdapter();
        pullToRefreshView.setAdapter(mAdapter);

        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getNewStates();
            }
        });

        if(mAdapter.getCount() == 0)
            RequestServer.getStates(stateExploreManager.getLastStateId(), newGetMoreStatesCallback());

        pullToRefreshView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if(noMore == false) {
                    loading.setVisibility(View.VISIBLE);
                    noMoreStates.setVisibility(View.GONE);
                    RequestServer.getStates(stateExploreManager.getLastStateId(), newGetMoreStatesCallback());
                }
            }
        });

        pullToRefreshView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final Picasso picasso = Picasso.with(context);
                if(scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    picasso.resumeTag(context);
                }
                else {
                    picasso.pauseTag(context);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        Log.i("State_fragment", "onresume");
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public void getNewStates(){
        RequestServer.getStates(0, newGetNewStatesCallback());
    }

    private JsonHttpResponseHandler newGetNewStatesCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                try {
                    Log.i("JSON", "" + response.getInt("status"));
                    if(response.getInt("status") == 0) {
                        JSONArray body = response.getJSONArray("body");
                        Type stateItemListType = new TypeToken<ArrayList<StateItem>>(){}.getType();
                        ArrayList<StateItem> newStatesList =
                                gson.fromJson(body.toString(), stateItemListType);
                        Log.i("GSON", newStatesList.size() + "");
                        stateExploreManager.updateStatesList(newStatesList);
                        pullToRefreshView.onRefreshComplete();
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                pullToRefreshView.onRefreshComplete();
            }
        };
    }

    private JsonHttpResponseHandler newGetMoreStatesCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                try {
                    Log.i("JSON", "" + response.getInt("status"));
                    if(response.getInt("status") == 0) {
                        JSONArray body = response.getJSONArray("body");
                        Type stateItemListType = new TypeToken<ArrayList<StateItem>>(){}.getType();
                        ArrayList<StateItem> newStatesList =
                                gson.fromJson(body.toString(), stateItemListType);
                        Log.i("GSON", newStatesList.size() + "");

                        if(newStatesList.size() > 0) {
                            stateExploreManager.appendStatesList(newStatesList);
                            mAdapter.notifyDataSetChanged();
                        }
                        else {
                            loading.setVisibility(View.GONE);
                            noMoreStates.setVisibility(View.VISIBLE);
                            noMore = true;
                        }

                    }
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
            }
        };
    }

    private JsonHttpResponseHandler newFollowCallback(final TextView follow) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                barProgressDialog.dismiss();
                follow.setText("√ 已关注");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                barProgressDialog.dismiss();
            }

        };
    }

    private JsonHttpResponseHandler newLikeCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                try {
                    if (response.getInt("status") == 0) {

                    }
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
            }
        };
    }

    public class StateAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        StateAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return stateExploreManager.stateCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.state_item_view, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.stateItemView = (StateItemView)view;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final StateItem stateItem = stateExploreManager.get(position);

            holder.stateItemView.updateStateItemView(stateItem);

            holder.stateItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCommentActivity(position);
                }
            });
            return view;
        }
    }

    private void startCommentActivity(int pos) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra(Constants.StateManager.STATE_INDEX, pos);
        startActivity(intent);
    }

    public static class ViewHolder {
        public StateItemView stateItemView;
    }
}
