package me.qiang.android.chongai.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import com.google.gson.annotations.SerializedName;
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
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Fragment.StateFragment;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.Comment;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.Pet;
import me.qiang.android.chongai.util.StateExploreManager;
import me.qiang.android.chongai.util.StateItem;
import me.qiang.android.chongai.util.User;

public class CommentActivity extends ActionBarActivity {

    private StateExploreManager stateExploreManager;

    private DisplayImageOptions options;

    private ListView commentList;
    private View commentHeader;
    private View commentFooter;
    private EditText commentEditText;
    private TextView sendComment;
    private ImageView commentEditExpression;
    private LinearLayout mainLayout;

    private View loading;
    private TextView nomoreComents;
    boolean nomore = false;
    boolean isRefreshing = false;

    private int clickedY;

    boolean softKeyboardState = false;
    boolean listItemClicked = false;

    protected ProgressDialog barProgressDialog;

    private StateItem stateItem;

    private CommentHttpClient commentHttpClient;

    private CommentsManager commentsManager;

    private CommentAdapter commentAdapter;

    private User toUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

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

        stateExploreManager = StateExploreManager.getStateExploreManager();

        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        commentHttpClient = new CommentHttpClient(barProgressDialog);

        sendComment = (TextView) findViewById(R.id.send_comment);
        sendComment.setClickable(false);

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int toUserId = toUser == null ? 0 : toUser.getUserId();
                commentHttpClient.sendComments(stateItem.getStateId(),
                        commentEditText.getText().toString(), toUserId);
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

        commentList = (ListView) findViewById(R.id.list);
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

        commentHeader = getLayoutInflater().inflate(R.layout.state_item, null);
        int position = getIntent().getIntExtra("STATE_POS", 0);
        initHeader(position);

        commentsManager = new CommentsManager(stateItem.getStateId());

        commentList.addHeaderView(commentHeader);

        commentFooter = getLayoutInflater().inflate(R.layout.loading, null);
        commentList.addFooterView(commentFooter);
        commentFooter.setVisibility(View.GONE);
        loading = commentFooter.findViewById(R.id.loading_layout);
        nomoreComents = (TextView) commentFooter.findViewById(R.id.loading_nomore);

        commentAdapter = new CommentAdapter();
        commentList.setAdapter(commentAdapter);
        commentsManager.getNewComments();


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
                            commentsManager.getCommentsMore();
                        }
                        atEnd = true;
                    }

                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
    }

    private void initHeader(int position) {
        final StateFragment.ViewHolder holder = new StateFragment.ViewHolder();
        View view = commentHeader;
        final StateFragment.FollowHttpClient followHttpClient =
                new StateFragment.FollowHttpClient(barProgressDialog);

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

        stateItem = stateExploreManager.get(position);

        ImageLoader.getInstance().
                displayImage(stateItem.getStateOwnerPhoto(), holder.stateOwnerPhoto, options);
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

        ImageLoader.getInstance().
                displayImage(stateItem.getStateImage(), holder.stateBodyImage, options);
        holder.stateBodyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImagePagerActivity(stateItem.getStateImage());
            }
        });

        holder.stateBodyText.setText(stateItem.getStateContent());

        if(holder.stateBodyPraise.getChildCount() > 0)
            holder.stateBodyPraise.removeAllViews();

        for (int i = 0; i < Math.min(8, stateItem.getStatePraisedNum()); i++) {
            CircleImageView praisePhoto = (CircleImageView) getLayoutInflater().
                    inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
            holder.stateBodyPraise.addView(praisePhoto);
            ImageLoader.getInstance().
                    displayImage(stateItem.getPraiseUserPhoto(i), praisePhoto, options);
        }

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CommentActivity.this, PraiseActivity.class));
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
        int[] commentEditTextCoordination = {0, 0};

        commentEditText.getLocationOnScreen(commentEditTextCoordination);

        commentList
                .smoothScrollBy(dest - commentEditTextCoordination[1], 600);
    }

    private void startImagePagerActivity(String imageUrl){
        Intent intent = new Intent(this, ImagePager.class);
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(imageUrl);
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.Extra.IMAGE_POSITION, 0);
        bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_enter, 0);
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
                ImageLoader.getInstance().
                        displayImage(commentItem.getCommentUserPhoto(), holder.commentUserPhoto, options);
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

    public class CommentHttpClient {
        ProgressDialog progressDialog;

        public CommentHttpClient(ProgressDialog dialog) {
            progressDialog = dialog;
        }

        public void getComments(int startStateId) {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("comment_id", startStateId);
            params.put("post_id", stateItem.getStateId());

            HttpClient.post("getComments", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        }

        public void sendComments(int stateId, final String content, int userId) {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("post_id", stateId);
            params.put("content", content);
            params.put("user_id", userId);

            HttpClient.post("comment", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i("JSON", response.toString());
                    try {
                        int commentId = response.getJSONObject("body").getInt("comment_id");
                        User commentUser = GlobalApplication.getUserSessionManager().getUser();
                        Comment newComment = new Comment(commentId,stateItem.getStateId(), commentUser,
                                toUser, content);
                        commentsManager.pushComment(newComment);
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
            });
        }
    }

    public class CommentsManager {
        private int stateId;
        private List<Comment> commentList;

        public CommentsManager(int stateId) {
            this.stateId = stateId;
            commentList = new ArrayList<>();
        }

        public int commentsCount() {
            return commentList.size();
        }

        public Comment getComment(int i) {
            return commentList.get(i);
        }

        public User getCommentUser(int i) {
            return commentList.get(i).getCommentUser();
        }

        public void pushComment(Comment comment) {
            commentList.add(0, comment);
        }

        public void getNewComments() {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            params.put("comment_id", 0);
            params.put("post_id", stateId);

            HttpClient.post("comment/getComment", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i("JSON", response.toString());
                    Gson gson = new Gson();
                    CommentsResult commentsResult = gson.fromJson(response.toString(), CommentsResult.class);

                    Log.i("GSON", commentsResult.newCommentsList.size()+ "");
                    commentList = commentsResult.newCommentsList;

                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, JSONObject errorResponse) {
                    Log.i("JSON", "JSON FAIL");
                }
            });
        }

        public void getCommentsMore() {
            RequestParams params = new RequestParams();
            params.setUseJsonStreamer(true);
            Log.i("comment_id", commentList.get(commentList.size() -1).getCommentId() + " " + isRefreshing);
            params.put("comment_id", commentList.get(commentList.size() -1).getCommentId());
            params.put("post_id", stateId);

            HttpClient.post("comment/getComment", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i("JSON", response.toString());
                    Gson gson = new Gson();
                    CommentsResult commentsResult = gson.fromJson(response.toString(), CommentsResult.class);

                    Log.i("GSON", commentsResult.newCommentsList.size()+ "");
                    if(commentsResult.newCommentsList.size() > 0) {
                        commentList.addAll(commentsResult.newCommentsList);
                        commentAdapter.notifyDataSetChanged();
                    }
                    else {
                        loading.setVisibility(View.GONE);
                        nomoreComents.setVisibility(View.VISIBLE);
                        nomore = true;
                    }
                    isRefreshing = false;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, JSONObject errorResponse) {
                    Log.i("JSON", "JSON FAIL");
                    isRefreshing = false;
                }
            });
        }

        public class CommentsResult {
            public int status;

            @SerializedName("body")
            public List<Comment> newCommentsList;
        }
    }
}
