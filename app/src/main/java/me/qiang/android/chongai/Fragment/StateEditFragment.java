package me.qiang.android.chongai.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Activity.CustomAlbum;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.widget.ExpandableGridView;

public class StateEditFragment extends AbsListViewBaseFragment {

    List<String> imageUrls = new ArrayList<String>();

    DisplayImageOptions options;
    ImageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_state_edit, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        listView = (ExpandableGridView) rootView.findViewById(R.id.listView);
        ((ExpandableGridView)listView).setExpanded(true);
        imageUrls.add("drawable://" + R.drawable.icon_addpic_unfocused);
        adapter = new ImageAdapter();
        ((GridView) listView).setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == imageUrls.size() - 1) {
                    Intent intent = new Intent(getActivity(), CustomAlbum.class);
                    intent.putStringArrayListExtra(Constants.Extra.IMAGE_SELECTED, (ArrayList)imageUrls);
                    startActivityForResult(intent, Constants.Extra.ALBUM_MUTIPLE_SELECT);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.Extra.ALBUM_MUTIPLE_SELECT && resultCode == Activity.RESULT_OK) {
            imageUrls = data.getStringArrayListExtra(Constants.Extra.IMAGE_SELECTED);
            imageUrls.add("drawable://" + R.drawable.icon_addpic_unfocused);
            adapter.notifyDataSetChanged();
        }
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            int size = imageUrls.size();
            if (size > 9) size = 9;
            return size;
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
                view = inflater.inflate(R.layout.state_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.state_image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance()
                    .displayImage(imageUrls.get(position), holder.imageView, options);
//            ImageSize targetSize = new ImageSize(150, 150);
//            Bitmap bmp = ImageLoader.getInstance()
//                    .loadImageSync(imageUrls.get(position), targetSize, options);
//            holder.imageView.setImageBitmap(bmp);

            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }
}