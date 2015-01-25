package me.qiang.android.chongai.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Activity.CommentActivity;
import me.qiang.android.chongai.Activity.UserAcitivity;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.RequestServer;

public class StateFragment extends BaseFragment {

    // UI widget
    private PullToRefreshListView pullToRefreshView;
    private View mFooterView;
    private ProgressBar footerLoading;
    private ProgressDialog barProgressDialog;

    // Adapter and state manager related to list view
    private StateAdapter mAdapter;
    private StateExploreManager stateExploreManager;

    private DisplayImageOptions options;

    public StateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stateExploreManager = StateExploreManager.getStateExploreManager();

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_state_list, container, false);

        barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        pullToRefreshView = (PullToRefreshListView) rootView.findViewById(R.id.my_list);
        mFooterView = inflater.inflate(R.layout.loading, null);
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

        pullToRefreshView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                mFooterView.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
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
                view = inflater.inflate(R.layout.state_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.stateOwnerPhoto = (CircleImageView) view.findViewById(R.id.state_owner_photo);
                holder.stateOwnerName = (TextView) view.findViewById(R.id.state_owner_name);
                holder.stateOwnerLocation = (TextView) view.findViewById(R.id.state_owner_location);
                holder.stateCreateTime = (TextView) view.findViewById(R.id.state_create_distance);
                holder.isFollowed = (TextView) view.findViewById(R.id.follow);
                holder.statePetName = (TextView) view.findViewById(R.id.state_pet_name);
                holder.statePetType = (TextView) view.findViewById(R.id.state_pet_type);
                holder.stateBodyImage = (ImageView) view.findViewById(R.id.state_body_image);
                holder.stateBodyText = (TextView) view.findViewById(R.id.state_body_text);
                holder.stateBodyPraise = (LinearLayout) view.findViewById(R.id.state_body_praise);
                holder.comment = (LinearLayout) view.findViewById(R.id.comment);
                holder.stateCommentNum = (TextView) view.findViewById(R.id.state_comment_num);
                holder.praise = (LinearLayout) view.findViewById(R.id.praise);
                holder.statePraiseNum = (TextView) view.findViewById(R.id.state_praise_num);
                holder.likeState = (ImageView) view.findViewById(R.id.like_state);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final StateItem stateItem = stateExploreManager.get(position);

            ImageLoader.getInstance().displayImage(stateItem.getStateOwnerPhoto(), holder.stateOwnerPhoto, options);
            holder.stateOwnerPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), UserAcitivity.class));
                }
            });

            holder.stateOwnerName.setText(stateItem.getStateOwnerName());

            holder.stateOwnerLocation.setText(stateItem.getStateOwnerLocation());

            if(stateItem.isFollowedStateOwner())
                holder.isFollowed.setText("√ 已关注");
            else
                holder.isFollowed.setText("+ 关注");

            holder.isFollowed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!stateItem.isFollowedStateOwner()) {
                        Log.i("Follow", "onclick");
                        barProgressDialog.setMessage("正在处理...");
                        barProgressDialog.show();
                        RequestServer.follow(stateItem.getStateOwnerId(), newFollowCallback(holder.isFollowed));
                    }
                    //TODO: deal with cancel follow action
                }
            });

            holder.statePetName.setText(stateItem.getStatePetName());
            holder.statePetType.setText(stateItem.getStatePetType());

            if(stateItem.getStatePetGender() == Pet.Gender.FEMALE) {
                ((GradientDrawable)holder.statePetName.getBackground()).setColor(0xFFFF939A);
                ((GradientDrawable)holder.statePetType.getBackground()).setColor(0xFFFF939A);
            }

            ImageLoader.getInstance().displayImage(stateItem.getStateImage(), holder.stateBodyImage, options);
            holder.stateBodyImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> imageUrls = new ArrayList<String>();
                    imageUrls.add(stateItem.getStateImage());
                    ActivityTransition.startImagePagerActivity(getActivity(), imageUrls, 0);
                }
            });

            holder.stateBodyText.setText(stateItem.getStateContent());

            if(holder.stateBodyPraise.getChildCount() > 0)
                holder.stateBodyPraise.removeAllViews();

            for (int i = 0; i < Math.min(7, stateItem.getStatePraisedNum()); i++) {
                CircleImageView praisePhoto = (CircleImageView) inflater.inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
                praisePhoto.setTag(stateItem.getPraiseUserId(i));
                holder.stateBodyPraise.addView(praisePhoto);
                ImageLoader.getInstance().displayImage(stateItem.getPraiseUserPhoto(i), praisePhoto, options);
            }

            if(stateItem.getStatePraisedNum() > 7) {
                ImageView praisePhoto = (ImageView) inflater.inflate(R.layout.icon_more, holder.stateBodyPraise, false);
                holder.stateBodyPraise.addView(praisePhoto);
            }

            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCommentActivity(position);
                }
            });

            holder.praise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(stateItem.getLikeState() == false) {
                        RequestServer.like(stateItem.getStateId(), newLikeCallback());
                        CircleImageView praisePhoto = (CircleImageView) inflater.inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
                        praisePhoto.setTag(GlobalApplication.getUserSessionManager().getCurrentUser().getUserId());
                        ImageLoader.getInstance().displayImage(GlobalApplication.getUserSessionManager().getCurrentUser().getUserPhoto(), praisePhoto, options);
                        holder.stateBodyPraise.addView(praisePhoto, 0);
                        holder.likeState.setImageResource(R.drawable.like);
                        holder.statePraiseNum.setVisibility(View.VISIBLE);
                        holder.statePraiseNum.setText(stateItem.getStatePraisedNum() + 1 + "");
                        stateItem.setLikeState(true);
                        stateItem.addPraiseUser(GlobalApplication.getUserSessionManager().getCurrentUser());
                        return;
                    }
                    else if(stateItem.getLikeState() == true) {
                        RequestServer.unlike(stateItem.getStateId(), newLikeCallback());
                        holder.likeState.setImageResource(R.drawable.not_like);
                        holder.statePraiseNum.setText(stateItem.getStatePraisedNum() - 1 + "");
                        if (stateItem.getStatePraisedNum() <= 1)
                            holder.statePraiseNum.setVisibility(View.GONE);
                        for(int i = 0; i < holder.stateBodyPraise.getChildCount(); i++) {
                            if(holder.stateBodyPraise.getChildAt(i).getTag() != null &&
                                    (int) holder.stateBodyPraise.getChildAt(i).getTag() ==
                                    GlobalApplication.getUserSessionManager().getCurrentUser().getUserId()) {
                                holder.stateBodyPraise.removeViewAt(i);
                            }
                        }
                        stateItem.setLikeState(false);
                        stateItem.decreasePraiseUser(GlobalApplication.getUserSessionManager().getCurrentUser().getUserId());
                        return;
                    }
                }
            });

            holder.statePraiseNum.setVisibility(View.GONE);
            if(stateItem.getStatePraisedNum() > 0) {
                holder.statePraiseNum.setVisibility(View.VISIBLE);
                holder.statePraiseNum.setText(stateItem.getStatePraisedNum() + "");
            }

            if(stateItem.getLikeState()) {
                holder.likeState.setImageResource(R.drawable.like);
            }
            else {
                holder.likeState.setImageResource(R.drawable.not_like);
            }

            holder.stateCommentNum.setVisibility(View.GONE);
            if(stateItem.getStateCommentsNum() > 0) {
                holder.stateCommentNum.setVisibility(View.VISIBLE);
                holder.stateCommentNum.setText(stateItem.getStateCommentsNum() + "");
            }

            return view;
        }
    }

    private void startCommentActivity(int pos) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra("STATE_POS", pos);
        startActivity(intent);
    }

    public static class ViewHolder {
        public CircleImageView stateOwnerPhoto;
        public TextView stateOwnerName;
        public TextView stateOwnerLocation;
        public TextView stateCreateTime;
        public TextView isFollowed;
        public TextView statePetName;
        public TextView statePetType;
        public ImageView stateBodyImage;
        public TextView stateBodyText;
        public LinearLayout stateBodyPraise;
        public LinearLayout comment;
        public TextView stateCommentNum;
        public LinearLayout praise;
        public TextView statePraiseNum;
        public ImageView likeState;
    }
}
