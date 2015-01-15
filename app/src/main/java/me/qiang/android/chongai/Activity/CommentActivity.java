package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.R;

public class CommentActivity extends ActionBarActivity {

    private PullToRefreshListView commentList;
    private View commentHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentList = (PullToRefreshListView) findViewById(R.id.list);

        commentHeader = getLayoutInflater().inflate(R.layout.state_item, null);

        commentList.getRefreshableView().addHeaderView(commentHeader);
        commentList.setAdapter(new CommentAdapter());

        commentList.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                commentList.getRefreshableView().setSelectionFromTop(2, 400);
            }
        });
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
