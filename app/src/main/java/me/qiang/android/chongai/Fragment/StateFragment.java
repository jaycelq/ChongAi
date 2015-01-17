package me.qiang.android.chongai.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Activity.CommentActivity;
import me.qiang.android.chongai.Activity.ImagePager;
import me.qiang.android.chongai.Activity.PraiseActivity;
import me.qiang.android.chongai.Activity.UserAcitivity;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.StateExploreManager;
import me.qiang.android.chongai.util.StateItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class StateFragment extends BaseFragment {

    private StateAdapter mAdapter;

    private PullToRefreshListView pullToRefreshView;

    private OnFragmentInteractionListener mListener;

    private View mFooterView;
    private ProgressBar footerLoading;

    private StateExploreManager stateExploreManager;
    private DisplayImageOptions options;

    private FollowHttpClient followHttpClient;

    protected ProgressDialog barProgressDialog;

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

        followHttpClient = new FollowHttpClient();
        barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);
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
                new GetDataTask().execute();
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.state_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.stateOwnerPhoto = (CircleImageView) view.findViewById(R.id.state_owner_photo);
                holder.stateOwnerName = (TextView) view.findViewById(R.id.state_owner_name);
                holder.stateCreateTime = (TextView) view.findViewById(R.id.state_create_distance);
                holder.isFollowed = (TextView) view.findViewById(R.id.follow);
                holder.stateBodyImage = (ImageView) view.findViewById(R.id.state_body_image);
                holder.stateBodyPraise = (LinearLayout) view.findViewById(R.id.state_body_praise);
                holder.comment = (LinearLayout) view.findViewById(R.id.comment);
                holder.praise = (LinearLayout) view.findViewById(R.id.praise);
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

            ImageLoader.getInstance().displayImage(stateItem.getStateImage(), holder.stateBodyImage, options);
            holder.stateBodyImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startImagePagerActivity(stateItem.getStateImage());
                }
            });

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

            for (int i = 0; i < Math.min(8, stateItem.getStatePraisedNum()); i++) {
                CircleImageView praisePhoto = (CircleImageView) inflater.inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
                holder.stateBodyPraise.addView(praisePhoto);
                ImageLoader.getInstance().displayImage(stateItem.getPraiseUserPhoto(i), praisePhoto, options);
            }

            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), CommentActivity.class));
                }
            });

            holder.praise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), PraiseActivity.class));
                }
            });


            return view;
        }
    }

    static class ViewHolder {
        CircleImageView stateOwnerPhoto;
        TextView stateOwnerName;
        TextView stateCreateTime;
        TextView isFollowed;
        ImageView stateBodyImage;
        LinearLayout stateBodyPraise;
        LinearLayout comment;
        LinearLayout praise;
    }

    public class FollowHttpClient {

        FollowHttpClient() {
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

}
