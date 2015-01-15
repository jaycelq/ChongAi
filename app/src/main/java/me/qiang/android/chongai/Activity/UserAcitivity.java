package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import me.qiang.android.chongai.R;

public class UserAcitivity extends ActionBarActivity {

    private PullToRefreshListView userProfileList;
    private View profileHeader;
    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_acitivity);
        profileAdapter = new ProfileAdapter();

        userProfileList = (PullToRefreshListView) findViewById(R.id.list);

        profileHeader = getLayoutInflater().inflate(R.layout.profile_header, null);

        userProfileList.getRefreshableView().addHeaderView(profileHeader);
        userProfileList.setAdapter(profileAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_acitivity, menu);
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

    public class ProfileAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ProfileAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return 1;
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
            return inflater.inflate(R.layout.profile_data, parent, false);
        }
    }
}
