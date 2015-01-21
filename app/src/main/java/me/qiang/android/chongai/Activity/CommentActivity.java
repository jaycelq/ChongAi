package me.qiang.android.chongai.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Fragment.StateFragment;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.Pet;
import me.qiang.android.chongai.util.StateExploreManager;
import me.qiang.android.chongai.util.StateItem;

public class CommentActivity extends ActionBarActivity {

    private StateExploreManager stateExploreManager;

    private DisplayImageOptions options;

    private ListView commentList;
    private View commentHeader;
    private EditText commentEditText;
    private TextView sendComment;
    private ImageView commentEditExpression;
    private LinearLayout mainLayout;

    private int clickedY;

    boolean softKeyboardState = false;

    protected ProgressDialog barProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

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

        stateExploreManager = StateExploreManager.getStateExploreManager();

        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        commentEditText = (EditText) findViewById(R.id.comment_edit_text);
        commentList = (ListView) findViewById(R.id.list);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.getViewTreeObserver().
            addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    int heightDiff = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).
                            getDefaultDisplay().getHeight() - mainLayout.getHeight();

                    if (heightDiff > 300) {
                        if(softKeyboardState == false) {
                            scrollClickedViewToScreenBottom(clickedY);
                            softKeyboardState = true;
                        }
                    }
                    else {
                        if(softKeyboardState == true) {
                            int [] pos = {0, 0};
                            commentHeader.getLocationOnScreen(pos);
                            clickedY = pos[1] + 10 + commentHeader.getHeight();
                            softKeyboardState = false;
                        }
                    }
                }
            });

        commentHeader = getLayoutInflater().inflate(R.layout.state_item, null);
        int position = getIntent().getIntExtra("STATE_POS", 0);
        initHeader(position);

        commentList.addHeaderView(commentHeader);
        commentList.setAdapter(new CommentAdapter());

        commentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int [] pos = {0, 0};
                view.getLocationOnScreen(pos);
                clickedY = pos[1] + 10 + view.getHeight();

                commentEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        commentList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        commentList.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                commentList.setSelectionFromTop(2, 400);
            }
        });
    }

    private void initHeader(int position) {
        final StateFragment.ViewHolder holder = new StateFragment.ViewHolder();
        View view = commentHeader;
        final StateFragment.FollowHttpClient followHttpClient = new StateFragment.FollowHttpClient(barProgressDialog);

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

        final StateItem stateItem = stateExploreManager.get(position);

        ImageLoader.getInstance().displayImage(stateItem.getStateOwnerPhoto(), holder.stateOwnerPhoto, options);
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

        for (int i = 0; i < Math.min(8, stateItem.getStatePraisedNum()); i++) {
            CircleImageView praisePhoto = (CircleImageView) getLayoutInflater().inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
            holder.stateBodyPraise.addView(praisePhoto);
            ImageLoader.getInstance().displayImage(stateItem.getPraiseUserPhoto(i), praisePhoto, options);
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
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
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
            return 9;
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

//            holder.commentUserPhoto.setImageResource(R.drawable.comment_user);
//            holder.stateOwnerPhoto.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startActivity(new Intent(getActivity(), UserAcitivity.class));
//                }
//            });
//            holder.stateBodyImage.setImageResource(R.drawable.pet_dog);
//
//            CircleImageView praisePhoto = (CircleImageView) inflater.inflate(R.layout.praise_photo, holder.stateBodyPraise, false);
//            holder.stateBodyPraise.addView(praisePhoto);


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
