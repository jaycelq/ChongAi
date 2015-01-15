package me.qiang.android.chongai.Activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.R;

public class PraiseActivity extends ActionBarActivity {

    private PullToRefreshListView commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_praise);

        commentList = (PullToRefreshListView) findViewById(R.id.list);

        commentList.setAdapter(new PraiseAdapter());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_praise, menu);
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

    public class PraiseAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        PraiseAdapter() {
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
                view = inflater.inflate(R.layout.praise_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.praiseUserPhoto = (CircleImageView) view.findViewById(R.id.praise_user_photo);
                holder.praiseUserName = (TextView) view.findViewById(R.id.praise_user_name);
                holder.praiseUserGender = (ImageView) view.findViewById(R.id.praise_user_gender);
                holder.praiseUserLocation = (TextView) view.findViewById(R.id.praise_user_location);
                holder.praiseUserPets = (LinearLayout) view.findViewById(R.id.praise_user_pets);
                holder.praiseUserRelation = (ImageView) view.findViewById(R.id.praise_user_relation);
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
        CircleImageView praiseUserPhoto;
        TextView praiseUserName;
        ImageView praiseUserGender;
        TextView praiseUserLocation;
        LinearLayout praiseUserPets;
        ImageView praiseUserRelation;
    }
}