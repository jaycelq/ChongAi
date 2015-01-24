package me.qiang.android.chongai.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Activity.CommentActivity;
import me.qiang.android.chongai.Activity.ImagePager;
import me.qiang.android.chongai.Activity.UserAcitivity;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.Pet;
import me.qiang.android.chongai.util.StateExploreManager;
import me.qiang.android.chongai.util.StateItem;

public class StateFragment extends BaseFragment {

    // UI widget
    private PullToRefreshListView pullToRefreshView;
    private View mFooterView;
    private ProgressBar footerLoading;
    protected ProgressDialog barProgressDialog;

    // Adapter and state manager related to list view
    private StateAdapter mAdapter;
    private StateExploreManager stateExploreManager;

    private DisplayImageOptions options;

    private FollowHttpClient followHttpClient;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        followHttpClient = new FollowHttpClient(barProgressDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_state_list, container, false);
        pullToRefreshView = (PullToRefreshListView) rootView.findViewById(R.id.my_list);

        mFooterView = inflater.inflate(R.layout.loading, null);
        footerLoading = (ProgressBar) mFooterView.findViewById(R.id.loading);
        pullToRefreshView.getRefreshableView().addFooterView(mFooterView);
        footerLoading.getIndeterminateDrawable().setColorFilter(0xff7FB446,
                android.graphics.PorterDuff.Mode.SRC_ATOP);
        mFooterView.setVisibility(View.GONE);

        mAdapter = new StateAdapter();
        pullToRefreshView.setAdapter(mAdapter);

        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                stateExploreManager.getNewStates(mAdapter, pullToRefreshView);
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

    private void startImagePagerActivity(String imageUrl){
        Intent intent = new Intent(getActivity(), ImagePager.class);
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(imageUrl);
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.Extra.IMAGE_POSITION, 0);
        bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.zoom_enter, 0);
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a bckground job.
            try {
                Log.i("AndroidPullToRefresh", "do pull");
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mAdapter.notifyDataSetChanged();

            // Call onRefreshComplete when the list has been refreshed.
            pullToRefreshView.onRefreshComplete();

            super.onPostExecute(result);
        }
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
                        followHttpClient.follow(stateItem.getStateOwnerId(), holder.isFollowed);
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
                    startImagePagerActivity(stateItem.getStateImage());
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
                        LikeHttpClient.like(inflater, stateItem.getStateId(), holder, stateItem, options);
                        CircleImageView praisePhoto = (CircleImageView) inflater.inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
                        praisePhoto.setTag(4);
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
                        LikeHttpClient.unlike(inflater, stateItem.getStateId(), holder, stateItem, options);
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

    public static class FollowHttpClient {
        ProgressDialog barProgressDialog;
        public FollowHttpClient(ProgressDialog dialog) {
            barProgressDialog = dialog;
        }

        public void follow(int userId, final TextView follow){
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("act", "follow");
            params.put("userId", userId);
            HttpClient.post("login", params, new JsonHttpResponseHandler() {
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
            });
        }
    }

    public static class LikeHttpClient {
        public static void like(final LayoutInflater inflater,
                                int stateId, final ViewHolder holder, final StateItem stateItem, final DisplayImageOptions options) {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("post_id", stateId);
            HttpClient.post("like", params, new JsonHttpResponseHandler() {
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

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.i("JSON", responseString);
                }
            });
        }

        public static void unlike(final LayoutInflater inflater,
                                  int stateId, final ViewHolder holder, final StateItem stateItem,
                                  final DisplayImageOptions options) {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("post_id", stateId);
            HttpClient.post("like/unlike", params, new JsonHttpResponseHandler() {
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
            });
        }
    }

}
