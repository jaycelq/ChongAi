package me.qiang.android.chongai.Fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.AlbumHelper;
import me.qiang.android.chongai.util.AlbumItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListDirFragment extends Fragment {

    private DisplayImageOptions options;
    private ListView mListDir;
    private List<AlbumItem> albumList;
    private int positionSelected;
    private GridView parentGrid;
    private ListDirApdater listDirApdater;

    private OnListDirSelectedListener listDirSelectedListener;

    private static final String ARG_PARAM= "positionSelected";

    public static ListDirFragment newInstance(int positionSelected) {
        ListDirFragment fragment = new ListDirFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM, positionSelected);
        fragment.setArguments(args);
        return fragment;
    }

    public ListDirFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
               .build();
        positionSelected = getArguments().getInt(ARG_PARAM);
        albumList = AlbumHelper.getHelper().getAlbumItemList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_dir, container, false);
        parentGrid = (GridView) getActivity().findViewById(R.id.grid);
        mListDir = (ListView) rootView.findViewById(R.id.id_list_dir);
        listDirApdater = new ListDirApdater(getActivity());
        mListDir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> imageUrls =  AlbumHelper.getHelper().getImageList(albumList.get(position).getFolderName());
                positionSelected = position;
                listDirApdater.notifyDataSetChanged();
                listDirSelectedListener.setSelectedImages(imageUrls, albumList.get(position).getFolderName(), albumList.get(position).getImageCounts(), position);
            }
        });
        mListDir.setAdapter(listDirApdater);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (parentGrid != null)
            parentGrid.setAlpha(0.3f);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (parentGrid !=null)
            parentGrid.setAlpha(1.0f);
    }

    public void setOnListDirSelectedListener(OnListDirSelectedListener listDirSelectedListener) {
        this.listDirSelectedListener = listDirSelectedListener;
    }

    public interface OnListDirSelectedListener {
        public void setSelectedImages(List<String> imageUrls, String folderName, int folderCount, int position);
    }

    public class ListDirApdater extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater;

        ListDirApdater(Context context) {
            mContext = context;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return albumList.size();
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
                view = inflater.inflate(R.layout.item_list_dir, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.album_item_image = (ImageView) view.findViewById(R.id.album_item_image);
                holder.album_name = (TextView) view.findViewById(R.id.album_name);
                holder.album_item_count = (TextView) view.findViewById(R.id.album_item_count);
                holder.album_select = (ImageView) view.findViewById(R.id.album_select);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance()
                    .displayImage(albumList.get(position).getTopImagePath(), holder.album_item_image, options);
            holder.album_name.setText(albumList.get(position).getFolderName());
            holder.album_item_count.setText(""+albumList.get(position).getImageCounts());
            if(position == positionSelected) {
                holder.album_select.setImageResource(R.drawable.dir_choose);
            }
            else
                holder.album_select.setImageResource(android.R.color.transparent);

            return view;
        }
    }

    public static class ViewHolder {
        ImageView album_item_image;
        TextView album_name;
        TextView album_item_count;
        ImageView album_select;
    }
}
