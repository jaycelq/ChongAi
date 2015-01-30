package me.qiang.android.chongai.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.widget.PetProfileView;

public class PetActivity extends ActionBarActivity {

    private Context context;

    // UI widget
    GridViewWithHeaderAndFooter petPhotoWithHeader;
    PetProfileView headerView;
    private View footerView;
    private View loading;
    private TextView noMoreStates;
    private ImageAdapter mAdapter;

    List<String> imageUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);

        context = this;

        petPhotoWithHeader = (GridViewWithHeaderAndFooter) findViewById(R.id.pet_photo_with_header);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        headerView = (PetProfileView)layoutInflater.inflate(R.layout.pet_profile_item, null);
//        footerView = layoutInflater.inflate(R.layout.loading, null);
//        loading = footerView.findViewById(R.id.loading_layout);
//        noMoreStates = (TextView) footerView.findViewById(R.id.loading_nomore);
        petPhotoWithHeader.addHeaderView(headerView);
//        petPhotoWithHeader.addFooterView(footerView);

        mAdapter = new ImageAdapter();
        petPhotoWithHeader.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pet, menu);
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

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(PetActivity.this);
        }

        @Override
        public int getCount() {
            return imageUrls.size();
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
            ImageViewHolder imageViewHolder;
            View view = convertView;
            int viewType = getItemViewType(position);
            Log.i("getadapter", position+"");
            if (view == null) {
                    view = inflater.inflate(R.layout.item_grid_image, parent, false);
                    imageViewHolder = new ImageViewHolder();
                    assert view != null;
                    imageViewHolder.imageView = (ImageView) view.findViewById(R.id.image);
                    view.setTag(imageViewHolder);
            }

            imageViewHolder = (ImageViewHolder) view.getTag();

            final ImageView imageView = imageViewHolder.imageView;

            Picasso.with(context)
                    .load("file://" + imageUrls.get(position))
                    .fit()
                    .centerCrop()
                    .into(imageViewHolder.imageView);
            imageView.setColorFilter(null);
            imageView.setTag(position);

            return view;

        }
    }

    static class ImageViewHolder {
        ImageView imageView;
//		ImageView item_select;
    }
}
