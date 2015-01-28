package me.qiang.android.chongai.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Activity.BaseToolbarActivity;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.RequestServer;

/**
 * Created by LiQiang on 26/1/15.
 */
public class StateItemView extends LinearLayout {
    private Context context;

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

    public StateItemView(Context context) {
        this(context, null);
        this.context = context;
    }

    public StateItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public StateItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.state_item, this, true);

        stateOwnerPhoto = (CircleImageView) findViewById(R.id.state_owner_photo);
        stateOwnerName = (TextView) findViewById(R.id.state_owner_name);
        stateOwnerLocation = (TextView) findViewById(R.id.state_owner_location);
        stateCreateTime = (TextView) findViewById(R.id.state_create_distance);
        isFollowed = (TextView) findViewById(R.id.follow);
        statePetName = (TextView) findViewById(R.id.state_pet_name);
        statePetType = (TextView) findViewById(R.id.state_pet_type);
        stateBodyImage = (ImageView) findViewById(R.id.state_body_image);
        stateBodyText = (TextView) findViewById(R.id.state_body_text);
        stateBodyPraise = (LinearLayout) findViewById(R.id.state_body_praise);
        comment = (LinearLayout) findViewById(R.id.comment);
        stateCommentNum = (TextView) findViewById(R.id.state_comment_num);
        praise = (LinearLayout) findViewById(R.id.praise);
        statePraiseNum = (TextView) findViewById(R.id.state_praise_num);
        likeState = (ImageView) findViewById(R.id.like_state);
    }

    public void updateStateItemView(final StateItem stateItem) {
        Picasso.with(context)
                .load(stateItem.getStateOwnerPhoto())
                .fit()
                .centerCrop()
                .into(stateOwnerPhoto);
        stateOwnerPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startUserActivity(context, stateItem.getStateOwnerId());
            }
        });

        stateOwnerName.setText(stateItem.getStateOwnerName());

        stateOwnerLocation.setText(stateItem.getStateOwnerLocation());

        if(stateItem.isFollowedStateOwner())
            isFollowed.setText("√ 已关注");
        else
            isFollowed.setText("+ 关注");

        isFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFollowed.setClickable(false);
                if(!stateItem.isFollowedStateOwner()) {
                    Log.i("Follow", "onclick");
                    ((BaseToolbarActivity)context).showProgressDialog("正在处理...");
                    RequestServer.follow(stateItem.getStateOwnerId(), newFollowCallback(isFollowed));
                }
                //TODO: deal with cancel follow action
            }
        });

        statePetName.setText(stateItem.getStatePetName());
        statePetType.setText(stateItem.getStatePetType());

        if(stateItem.getStatePetGender() == Pet.Gender.FEMALE) {
            ((GradientDrawable)statePetName.getBackground()).setColor(0xFFFF939A);
            ((GradientDrawable)statePetType.getBackground()).setColor(0xFFFF939A);
        }

        Picasso.with(context)
                .load(stateItem.getStateImage())
                .fit()
                .centerCrop()
                .into(stateBodyImage);
        stateBodyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startImagePagerActivity(context,
                        stateItem.getStateImage());
            }
        });

        stateBodyText.setText(stateItem.getStateContent());

        int lastPraiseUserId = stateItem.getStatePraisedNum() == 0 ? 0 : stateItem.getPraiseUserId(0);
        if(stateBodyPraise.getTag() == null ||
                !(((HashMap<String, Integer>)stateBodyPraise.getTag())
                        .get(Constants.StateItem.STATE_ID) == stateItem.getStateId()) ||
                !(((HashMap<String, Integer>)stateBodyPraise.getTag())
                        .get(Constants.StateItem.LAST_PRAISE_USER) == lastPraiseUserId)) {
            if (stateBodyPraise.getChildCount() > 0)
                stateBodyPraise.removeAllViews();

            for (int i = 0; i < Math.min(7, stateItem.getStatePraisedNum()); i++) {
                CircleImageView praisePhoto = (CircleImageView) LayoutInflater
                        .from(context).inflate(R.layout.praise_photo, stateBodyPraise, false);
                praisePhoto.setTag(stateItem.getPraiseUserId(i));
                stateBodyPraise.addView(praisePhoto);
                Picasso.with(context)
                        .load(stateItem.getPraiseUserPhoto(i))
                        .fit().
                        centerCrop()
                        .into(praisePhoto);
            }

            if (stateItem.getStatePraisedNum() > 7) {
                ImageView praisePhoto = (ImageView) LayoutInflater
                        .from(context).inflate(R.layout.icon_more, stateBodyPraise, false);
                stateBodyPraise.addView(praisePhoto);
            }
            HashMap<String, Integer> hashMap = new HashMap<>();
            hashMap.put(Constants.StateItem.STATE_ID, stateItem.getStateId());
            hashMap.put(Constants.StateItem.LAST_PRAISE_USER, lastPraiseUserId);
            stateBodyPraise.setTag(hashMap);
        }

        praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stateItem.getLikeState() == false) {
                    CircleImageView praisePhoto = (CircleImageView) LayoutInflater
                            .from(context).inflate(R.layout.praise_photo, stateBodyPraise, false);
                    praisePhoto.setTag(GlobalApplication.getUserSessionManager().getCurrentUser().getUserId());
                    Picasso.with(context)
                            .load(GlobalApplication.getUserSessionManager().getCurrentUser().getUserPhoto())
                            .fit()
                            .centerCrop()
                            .into(praisePhoto);
                    stateBodyPraise.addView(praisePhoto, 0);
                    likeState.setImageResource(R.drawable.like);
                    statePraiseNum.setVisibility(View.VISIBLE);
                    statePraiseNum.setText(stateItem.getStatePraisedNum() + 1 + "");
                    stateItem.setLikeState(true);
                    stateItem.addPraiseUser(GlobalApplication.getUserSessionManager().getCurrentUser());
                    RequestServer.like(stateItem.getStateId(), newLikeCallback(v));
                    return;
                }
                else if(stateItem.getLikeState() == true) {
                    likeState.setImageResource(R.drawable.not_like);
                    statePraiseNum.setText(stateItem.getStatePraisedNum() - 1 + "");
                    if (stateItem.getStatePraisedNum() <= 1)
                        statePraiseNum.setVisibility(View.GONE);
                    for(int i = 0; i < stateBodyPraise.getChildCount(); i++) {
                        if(stateBodyPraise.getChildAt(i).getTag() != null &&
                                (int) stateBodyPraise.getChildAt(i).getTag() ==
                                        GlobalApplication.getUserSessionManager().getCurrentUser().getUserId()) {
                            stateBodyPraise.removeViewAt(i);
                        }
                    }
                    stateItem.setLikeState(false);
                    stateItem.decreasePraiseUser(GlobalApplication.getUserSessionManager().getCurrentUser().getUserId());
                    RequestServer.unlike(stateItem.getStateId(), newLikeCallback(v));
                    return;
                }
            }
        });

        statePraiseNum.setVisibility(View.GONE);
        if(stateItem.getStatePraisedNum() > 0) {
            statePraiseNum.setVisibility(View.VISIBLE);
            statePraiseNum.setText(stateItem.getStatePraisedNum() + "");
        }

        if(stateItem.getLikeState()) {
            likeState.setImageResource(R.drawable.like);
        }
        else {
            likeState.setImageResource(R.drawable.not_like);
        }

        stateCommentNum.setVisibility(View.GONE);
        if(stateItem.getStateCommentsNum() > 0) {
            stateCommentNum.setVisibility(View.VISIBLE);
            stateCommentNum.setText(stateItem.getStateCommentsNum() + "");
        }
    }

    private JsonHttpResponseHandler newFollowCallback(final TextView follow) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                ((BaseToolbarActivity)context).hideProgressDialog();
                follow.setText("√ 已关注");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                ((BaseToolbarActivity)context).hideProgressDialog();
            }

        };
    }

    private JsonHttpResponseHandler newLikeCallback(final View view) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                view.setClickable(true);
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
                view.setClickable(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                view.setClickable(true);
            }
        };
    }

    public void setStateCommentNum(int commentNum) {
        stateCommentNum.setText(commentNum + "");
    }

    public void setOnCommentClickListener(OnClickListener onClickListener) {
        comment.setOnClickListener(onClickListener);
    }

}
