package me.qiang.android.chongai.PopUpWindow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.AlbumItem;

public class ListImageDirPopupWindow extends PopupWindow
{
	private ListView mListDir;
    private View mConvertView;
    private Context mContext;
    private List<AlbumItem> albumList;
    private DisplayImageOptions options;
    private int positionSelected;
    public PopUpWindowAdapter adapter;
    private boolean dismiss;

	public ListImageDirPopupWindow(View convertView, int width, int height,
                                   List<AlbumItem> albumList)
	{
		super(convertView, width, height, true);
        this.mConvertView = convertView;
        this.mContext = convertView.getContext();
        this.albumList = albumList;
        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i("POPUPWINDOW", "onTouchListener");
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
                {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        initViews();
	}

    public void setPositionSelected(int positionSelected) {
        this.positionSelected = positionSelected;
        adapter.notifyDataSetChanged();
    }

	public void initViews()
	{
		mListDir = (ListView) mConvertView.findViewById(R.id.id_list_dir);
        adapter = new PopUpWindowAdapter();
		mListDir.setAdapter(adapter);
	}

    public class PopUpWindowAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        PopUpWindowAdapter() {
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
