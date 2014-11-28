/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package me.qiang.android.chongai.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.qiang.android.chongai.Activity.ImagePager;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.PopUpWindow.ListImageDirPopupWindow;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.AlbumHelper;
import me.qiang.android.chongai.util.AlbumItem;


/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridFragment extends AbsListViewBaseFragment {

    private final static int SCAN_OK = 1;
	List<String> imageUrls = new ArrayList<String>();
    public  List<String> mSelectedImage = new LinkedList<String>();

	DisplayImageOptions options;
    private AlbumHelper helper;
    private ImageAdapter adapter;
    private ListImageDirPopupWindow albumPopUpWindow;
    private int mScreenHeight;
    private List<AlbumItem> albumList;
    private ListView albumListView;
    private RelativeLayout mBottomLy;
    private TextView albumChoosed;
    private TextView albumImageCount;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_OK:
                    adapter = new ImageAdapter();
                    listView.setAdapter(adapter);
                    if(albumPopUpWindow == null)
                        initPopUpWindow();
                    break;
            }
        }

    };

    private void initPopUpWindow() {
        View container = LayoutInflater.from(getActivity())
                .inflate(R.layout.list_dir, null);
        albumPopUpWindow = new ListImageDirPopupWindow(container,
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
                albumList);
        albumPopUpWindow.setPositionSelected(0);
        albumPopUpWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {

            @Override
            public void onDismiss()
            {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1.0f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        albumListView = (ListView) container.findViewById(R.id.id_list_dir);
        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                albumPopUpWindow.setPositionSelected(position);
                getImages(albumList.get(position).getFolderName());
                albumChoosed.setText(albumList.get(position).getFolderName());
                albumImageCount.setText(albumList.get(position).getImageCounts() + "张");
                albumPopUpWindow.dismiss();
            }
        });

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
        init();
	}

    private void init() {
        helper = AlbumHelper.getHelper();
        helper.init(getActivity());
        getImages(Constants.ALLIMAGE);
    }

    private void getImages(final String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                imageUrls = helper.getImageList(folder);
                albumList = helper.getAlbumItemList();
                mHandler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fr_image_grid, container, false);
        initView(rootView);
        initEvent();
		return rootView;
	}

    private void initView(View rootView) {
        listView = (GridView) rootView.findViewById(R.id.grid);
        mBottomLy = (RelativeLayout) rootView.findViewById(R.id.id_bottom_ly);
        albumChoosed = (TextView) rootView.findViewById(R.id.id_choose_dir);
        albumImageCount = (TextView) rootView.findViewById(R.id.id_total_count);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenHeight = outMetrics.heightPixels;
    }

    private void initEvent()
    {
        /**
         * 为底部的布局设置点击事件，弹出popupWindow
         */
        mBottomLy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(albumPopUpWindow != null) {
                    albumPopUpWindow
                            .setAnimationStyle(R.style.anim_popup_dir);
                    albumPopUpWindow.showAsDropDown(mBottomLy, 0, 0);

                    // 设置背景颜色变暗
                    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                    lp.alpha = .3f;
                    getActivity().getWindow().setAttributes(lp);
                }
            }
        });
    }

	public class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		ImageAdapter() {
			inflater = LayoutInflater.from(getActivity());
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
			final ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.item_grid_image, parent, false);
				holder = new ViewHolder();
				assert view != null;
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.item_select = (ImageView) view.findViewById(R.id.id_item_select);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

            final ImageView item_select = holder.item_select;
            final ImageView imageView = holder.imageView;

            ImageLoader.getInstance()
                    .displayImage(imageUrls.get(position), holder.imageView, options);
            item_select.setImageResource(R.drawable.picture_unselected);


            imageView.setColorFilter(null);
            imageView.setTag(position);
            //设置ImageView的点击事件
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(getActivity(), ImagePager.class);
                    Bundle bundle=new Bundle();
                    bundle.putInt(Constants.Extra.IMAGE_POSITION, (Integer)v.getTag());
                    bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            item_select.setColorFilter(null);
            item_select.setTag(position);
            item_select.setOnClickListener(new View.OnClickListener()
            {
                //选择，则将图片变暗，反之则反之
                @Override
                public void onClick(View v)
                {
                    int position = (int) v.getTag();
                    if (mSelectedImage.contains(imageUrls.get(position)))
                    {
                        mSelectedImage.remove(imageUrls.get(position));
                        item_select.setImageResource(R.drawable.picture_unselected);
                        imageView.setColorFilter(null);
                    } else
                    // 未选择该图片
                    {
                        if(mSelectedImage.size() < 9) {
                            mSelectedImage.add(imageUrls.get(position));
                            item_select.setImageResource(R.drawable.pictures_selected);
                            imageView.setColorFilter(Color.parseColor("#77000000"));
                        }
                        else {
                            Toast.makeText(getActivity(), "最多只能选择9张图片", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            });

            if (mSelectedImage.contains(imageUrls.get(position)))
            {
                holder.item_select.setImageResource(R.drawable.pictures_selected);
                imageView.setColorFilter(Color.parseColor("#77000000"));
            }



			return view;
		}
	}

	static class ViewHolder {
		ImageView imageView;
		ImageView item_select;
	}
}