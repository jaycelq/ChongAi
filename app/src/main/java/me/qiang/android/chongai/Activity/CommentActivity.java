package me.qiang.android.chongai.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Fragment.StateFragment;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Comment;
import me.qiang.android.chongai.Model.CommentsManager;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.RequestServer;

public class CommentActivity extends BaseToolbarActivity {
    // UserSessionManager to get the info of the current user
    private UserSessionManager userSessionManager;
    private User currentUser;

    private Context context;

    // CommentsManger to manage the comments attached to stateItem
    private CommentsManager commentsManager;

    // The current stateItem
    private StateItem stateItem;

    // UI widget and adapter
    private View commentHeader;
    private TextView commentNum;
    private ListView commentList;
    private CommentAdapter commentAdapter;

    // UI widget to send state
    private TextView sendComment;
    private EditText commentEditText;
    //TODO: add expression in user
    private ImageView commentEditExpression;

    // MainLayout used to detect soft keyboard popup
    private LinearLayout mainLayout;

    // UI in the listView footer
    private View commentFooter;
    private View loading;
    private TextView noMoreComments;

    // Flag to load more, when nomore comments or is refreshing stop get more
    boolean nomore = false;
    boolean isRefreshing = false;

    // Used to calculate distance to scroll when soft keyboard pop up
    private int clickedY;

    boolean softKeyboardState = false;
    boolean listItemClicked = false;

    protected ProgressDialog barProgressDialog;

    private User toUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        setToolbarTile("评论");
        enableBackButton();

        int position = getIntent().getIntExtra(Constants.StateManager.STATE_INDEX, 0);
        stateItem = StateExploreManager.getStateExploreManager().get(position);

        userSessionManager = GlobalApplication.getUserSessionManager();
        currentUser = userSessionManager.getCurrentUser();
        commentsManager = new CommentsManager(stateItem.getStateId());
        context = this;

        initCommentList();

        sendComment = (TextView) findViewById(R.id.send_comment);
        sendComment.setClickable(false);

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int toUserId = toUser == null ? 0 : toUser.getUserId();
                String content = commentEditText.getText().toString();

                RequestServer.sendComment(stateItem.getStateId(), content,
                        toUserId, newSendCommentCallback(content));
                hideSoftKeyboard();
                commentEditText.setText("");
            }
        });

        commentEditText = (EditText) findViewById(R.id.comment_edit_text);
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (content.length() > 0) {
                    sendComment.setTextColor(getResources().getColor(android.R.color.white));
                    sendComment.setClickable(true);
                } else {
                    sendComment.setTextColor(0xffe0e0e0);
                    sendComment.setClickable(false);
                }
            }
        });

    }

    private void initCommentList() {
        commentList = (ListView) findViewById(R.id.list);

        //Init ListView header
        commentHeader = getLayoutInflater().inflate(R.layout.state_item, null);
        initHeader();
        commentList.addHeaderView(commentHeader);

        //Init ListView footer
        commentFooter = getLayoutInflater().inflate(R.layout.loading, null);
        loading = commentFooter.findViewById(R.id.loading_layout);
        noMoreComments = (TextView) commentFooter.findViewById(R.id.loading_nomore);
        commentList.addFooterView(commentFooter);

        //set ListView adapter
        commentAdapter = new CommentAdapter();
        commentList.setAdapter(commentAdapter);

        // setOnItemClickListener to determine whom current user is reply to and
        // get the y coordinate of the clicked item bottom used to calculate the distance to scroll
        commentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int[] pos = {0, 0};
                view.getLocationOnScreen(pos);
                clickedY = pos[1] + 10 + view.getHeight();
                listItemClicked = true;
                commentEditText.requestFocus();
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
                if(position == 0 || position >= commentsManager.commentsCount()) {
                    toUser = null;
                    commentEditText.setHint("");
                }
                else {
                    toUser = commentsManager.getCommentUser(position - 1);
                    commentEditText.setHint("回复" + toUser.getUserName() + ":");
                }
            }
        });

        commentList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });

        commentList.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean atEnd = true;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0) {
                    int lastVisibleItem = firstVisibleItem + visibleItemCount;
                    Log.i("onScroll", lastVisibleItem + " " + totalItemCount);
                    View lastView = view.getChildAt(visibleItemCount - 1);
                    if ((lastVisibleItem < totalItemCount) ||
                            ((lastVisibleItem == totalItemCount) &&
                                    ((view.getHeight()) < lastView.getBottom()))
                            ) {
                        // not at end
                        Log.i("onScroll", view.getHeight() + " " + lastView.getBottom());
                        atEnd = false;
                    }
                    else {
                        Log.i("onScroll", "atEnd");
                        if(atEnd == false && nomore == false && isRefreshing == false) {
                            isRefreshing = true;
                            commentFooter.setVisibility(View.VISIBLE);
                            int commentCount = commentsManager.commentsCount();
                            int commentId = commentCount == 0 ? 0
                                    :commentsManager.getComment(commentCount -1).getCommentId();
                            RequestServer.getComments(commentId, stateItem.getStateId(),
                                    newGetMoreCommentsCallback());
                        }
                        atEnd = true;
                    }

                }
            }

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
        });

        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        int heightDiff = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).
                                getDefaultDisplay().getHeight() - mainLayout.getHeight();

                        if (heightDiff > 300) {
                            if (softKeyboardState == false && listItemClicked == true) {
                                scrollClickedViewToScreenBottom(clickedY);
                                softKeyboardState = true;
                                listItemClicked = false;
                            }
                        } else {
                            if (softKeyboardState == true) {
                                softKeyboardState = false;
                            }
                        }
                    }
                });
    }

    private void initHeader() {
        final StateFragment.ViewHolder holder = new StateFragment.ViewHolder();
        View view = commentHeader;

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
        commentNum = holder.stateCommentNum;
        holder.praise = (LinearLayout) view.findViewById(R.id.praise);
        holder.statePraiseNum = (TextView) view.findViewById(R.id.state_praise_num);
        holder.likeState = (ImageView) view.findViewById(R.id.like_state);

        Picasso.with(context).
                load(stateItem.getStateOwnerPhoto())
                .fit()
                .centerCrop()
                .into(holder.stateOwnerPhoto);
        holder.stateOwnerPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CommentActivity.this , UserAcitivity.class));
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

        Picasso.with(context)
                .load(stateItem.getStateImage())
                .fit()
                .centerCrop()
                .into(holder.stateBodyImage);
        holder.stateBodyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startImagePagerActivity(CommentActivity.this,
                        stateItem.getStateImage());
            }
        });

        holder.stateBodyText.setText(stateItem.getStateContent());

        if(holder.stateBodyPraise.getChildCount() > 0)
            holder.stateBodyPraise.removeAllViews();

        for (int i = 0; i < Math.min(8, stateItem.getStatePraisedNum()); i++) {
            CircleImageView praisePhoto = (CircleImageView) getLayoutInflater().
                    inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
            holder.stateBodyPraise.addView(praisePhoto);
            Picasso.with(context)
                    .load(stateItem.getPraiseUserPhoto(i))
                    .fit()
                    .centerCrop()
                    .into(praisePhoto);
        }

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] pos = {0, 0};
                commentHeader.getLocationOnScreen(pos);
                clickedY = pos[1] + 10 + commentHeader.getHeight();
                listItemClicked = true;
                commentEditText.requestFocus();
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
                toUser = null;
                commentEditText.setHint("");
            }
        });

        holder.praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                if(stateItem.getLikeState() == false) {
                    CircleImageView praisePhoto = (CircleImageView) getLayoutInflater()
                            .inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
                    praisePhoto.setTag(currentUser.getUserId());
                    Picasso.with(context)
                            .load(currentUser.getUserPhoto())
                            .fit()
                            .centerCrop()
                            .into(praisePhoto);
                    holder.stateBodyPraise.addView(praisePhoto, 0);
                    holder.likeState.setImageResource(R.drawable.like);
                    holder.statePraiseNum.setVisibility(View.VISIBLE);
                    holder.statePraiseNum.setText(stateItem.getStatePraisedNum() + 1 + "");
                    stateItem.setLikeState(true);
                    stateItem.addPraiseUser(currentUser);
                    RequestServer.like(stateItem.getStateId(), newLikeCallback(v));
                    return;
                }
                else if(stateItem.getLikeState() == true) {
                    holder.likeState.setImageResource(R.drawable.not_like);
                    holder.statePraiseNum.setText(stateItem.getStatePraisedNum() - 1 + "");
                    if (stateItem.getStatePraisedNum() <= 1)
                        holder.statePraiseNum.setVisibility(View.GONE);
                    for(int i = 0; i < holder.stateBodyPraise.getChildCount(); i++) {
                        if(holder.stateBodyPraise.getChildAt(i).getTag() != null &&
                                (int) holder.stateBodyPraise.getChildAt(i).getTag() ==
                                       currentUser.getUserId()) {
                            holder.stateBodyPraise.removeViewAt(i);
                        }
                    }
                    stateItem.setLikeState(false);
                    stateItem.decreasePraiseUser(currentUser.getUserId());
                    RequestServer.unlike(stateItem.getStateId(), newLikeCallback(v));
                    return;
                }
            }
        });

        holder.statePraiseNum.setVisibility(View.GONE);
        if(stateItem.getStatePraisedNum() > 0) {
            holder.statePraiseNum.setVisibility(View.VISIBLE);
            holder.statePraiseNum.setText(stateItem.getStatePraisedNum() + "");
        }

        holder.stateCommentNum.setVisibility(View.GONE);
        if(stateItem.getStateCommentsNum() > 0) {
            holder.stateCommentNum.setVisibility(View.VISIBLE);
            holder.stateCommentNum.setText(stateItem.getStateCommentsNum() + "");
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void scrollClickedViewToScreenBottom(int dest) {
        int[] commentEditTextCoordinate = {0, 0};

        commentEditText.getLocationOnScreen(commentEditTextCoordinate);

        commentList
                .smoothScrollBy(dest - commentEditTextCoordinate[1], 600);
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

    private JsonHttpResponseHandler newSendCommentCallback(final String content) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                try {
                    int commentId = response.getJSONObject("body").getInt("comment_id");
                    User commentUser = userSessionManager.getCurrentUser();
                    Comment newComment = new Comment(commentId,stateItem.getStateId(), commentUser,
                            toUser, content);
                    commentsManager.pushComment(newComment);
                    commentNum.setVisibility(View.VISIBLE);
                    commentNum.setText(stateItem.getStateCommentsNum() + 1 + "");
                    commentAdapter.notifyDataSetChanged();
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
            }
        };
    }

    private JsonHttpResponseHandler newGetNewCommentsCallback() {
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
                        Type commentItemListType = new TypeToken<ArrayList<Comment>>(){}.getType();
                        ArrayList<Comment> newCommentsList =
                                gson.fromJson(body.toString(), commentItemListType);
                        Log.i("GSON", newCommentsList.size() + "");
                        commentsManager.updateCommentsList(newCommentsList);
                        commentAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
            }
        };
    }

    private JsonHttpResponseHandler newGetMoreCommentsCallback() {
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
                        Type commentItemListType = new TypeToken<ArrayList<Comment>>(){}.getType();
                        ArrayList<Comment> newCommentsList =
                                gson.fromJson(body.toString(), commentItemListType);
                        Log.i("GSON", newCommentsList.size() + "");

                        Log.i("GSON", newCommentsList.size()+ "");
                        if(newCommentsList.size() > 0) {
                            commentsManager.addComments(newCommentsList);
                            commentAdapter.notifyDataSetChanged();
                        }
                        else {
                            loading.setVisibility(View.GONE);
                            noMoreComments.setVisibility(View.VISIBLE);
                            nomore = true;
                        }
                        isRefreshing = false;
                    }
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                isRefreshing = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", "JSON FAIL");
                isRefreshing = false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CommentAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        CommentAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getCount() {
            return commentsManager.commentsCount();
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
                view = inflater.inflate(R.layout.comment_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.commentUserPhoto = (CircleImageView) view.findViewById(R.id.comment_user_photo);
                holder.commentUserName = (TextView) view.findViewById(R.id.comment_user_name);
                holder.commentUserGender = (ImageView) view.findViewById(R.id.comment_user_gender);
                holder.commentContent = (TextView) view.findViewById(R.id.comment_content);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Comment commentItem = commentsManager.getComment(position);

            if(holder.commentUserPhoto.getTag() == null ||
                    !holder.commentUserPhoto.getTag().equals(commentItem.getCommentUserPhoto())) {
                Picasso.with(context)
                        .load(commentItem.getCommentUserPhoto())
                        .fit()
                        .centerCrop()
                        .into(holder.commentUserPhoto);
                holder.commentUserPhoto.setTag(commentItem.getCommentUserPhoto());
            }
            holder.commentUserName.setText(commentItem.getCommentUserName());

            if(commentItem.getCommentUserGender() == User.Gender.MALE)
                holder.commentUserGender.setImageResource(R.drawable.male);

            if(commentItem.getToUser() != null) {
                String toUserName = commentItem.getToUser().getUserName();
                SpannableString toUser = new SpannableString(toUserName);
                toUser.setSpan(new ForegroundColorSpan(0xFF8EB03A), 0,
                        toUser.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                ClickableSpan toUserClickSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        startActivity(new Intent(CommentActivity.this, UserAcitivity.class));
                    }
                };
                toUser.setSpan(toUserClickSpan, 0,
                        toUser.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                holder.commentContent.setText(
                        TextUtils.concat("回复", toUser, ": ", commentItem.getContent()));
            }
            else
                holder.commentContent.setText(commentItem.getContent());

            holder.commentContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean ret = false;
                    CharSequence text = ((TextView) v).getText();
                    Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
                    TextView widget = (TextView) v;
                    int action = event.getAction();

                    if (action == MotionEvent.ACTION_UP ||
                            action == MotionEvent.ACTION_DOWN) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        x -= widget.getTotalPaddingLeft();
                        y -= widget.getTotalPaddingTop();

                        x += widget.getScrollX();
                        y += widget.getScrollY();

                        Layout layout = widget.getLayout();
                        int line = layout.getLineForVertical(y);
                        int off = layout.getOffsetForHorizontal(line, x);

                        ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);

                        if (link.length != 0) {
                            if (action == MotionEvent.ACTION_UP) {
                                link[0].onClick(widget);
                            }
                            ret = true;
                        }
                    }
                    return ret;
                }
            });

            return view;
        }
    }

    static class ViewHolder {
        CircleImageView commentUserPhoto;
        TextView commentUserName;
        ImageView commentUserGender;
        TextView commentContent;
    }
}
