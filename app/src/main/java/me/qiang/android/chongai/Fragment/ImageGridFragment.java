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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.AlbumHelper;
import me.qiang.android.chongai.util.CameraUtil;


/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridFragment extends AbsListViewBaseFragment implements ListDirFragment.OnListDirSelectedListener{

    private final static int SCAN_OK = 1;
	List<String> imageUrls = new ArrayList<String>();
    public  List<String> mSelectedImage = new ArrayList<String>();

    private AlbumHelper helper;
    private ImageAdapter adapter;
    private RelativeLayout mBottomLy;
    private TextView albumChoosed;
    private TextView albumImageCount;
    private boolean allImage;

    private ListDirFragment listDirFragment;
    private int positionSelected;

    private File photoFile;

    private Context context;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_OK:
                    adapter = new ImageAdapter();
                    listView.setAdapter(adapter);
                    break;
            }
        }

    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        context = getActivity();
        init();
	}

    private void init() {
        helper = AlbumHelper.getHelper();
        helper.init(getActivity());
        getImages(Constants.ALLIMAGE);
        allImage = true;
    }

    private void getImages(final String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                helper.reloadImages();
                imageUrls = helper.getImageList(folder);
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
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final Picasso picasso = Picasso.with(context);
                if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    picasso.resumeTag(context);
                } else {
                    picasso.pauseTag(context);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mBottomLy = (RelativeLayout) rootView.findViewById(R.id.id_bottom_ly);
        albumChoosed = (TextView) rootView.findViewById(R.id.id_choose_dir);
        albumImageCount = (TextView) rootView.findViewById(R.id.id_total_count);
    }

    private void initEvent()
    {
        mBottomLy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listDirFragment = (ListDirFragment) getChildFragmentManager().findFragmentByTag("DIRLIST");
                if (listDirFragment != null && listDirFragment.isVisible()) {
                    getActivity().onBackPressed();
                }
                else {
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.dir_list_slide_in, 0, 0, R.anim.dir_list_slide_out);

                    if (listDirFragment == null) {
                        Log.i("TAG", "CREATE NEW FRAGMENT");
                        listDirFragment = ListDirFragment.newInstance(positionSelected);
                    }
                    listDirFragment.setOnListDirSelectedListener(ImageGridFragment.this);
                    ft.add(R.id.list_dir_container, listDirFragment, "DIRLIST");

                    ft.addToBackStack(null);

                    // Start the animated transition.
                    ft.commit();
                }
            }
        });
    }

    @Override
    public void setSelectedImages(List<String> imageUrls, String folderName, int folderCount, int position) {
        this.imageUrls = imageUrls;
        adapter.notifyDataSetChanged();
        albumChoosed.setText(folderName);
        albumImageCount.setText(folderCount+ "张");
        positionSelected = position;
        if(folderName.equals(Constants.ALLIMAGE))
            allImage = true;
        else
            allImage = false;
        getActivity().onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.i("OnActivityResult", "return from take photo");
        if (requestCode == Constants.Image.TAKE_PHOTO) {
            if(resultCode == Activity.RESULT_OK) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.Image.IMAGE_RESULT, photoFile.getCanonicalPath());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                } catch (IOException ex) {
                    Log.i("TAKE_PHOTO", ex.getMessage());
                }
            }
            else {
                photoFile.delete();
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		ImageAdapter() {
			inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getCount() {
			return allImage ? imageUrls.size() + 1 : imageUrls.size();
		}

        @Override
        public int getViewTypeCount() {
            Log.i("AllImage", allImage + "");
            return allImage ? 2 : 1;
        }

        @Override
        public int getItemViewType(int position) {
            if(allImage) {
                if(position == 0)
                    return 1;
                else
                    return 0;
            }
            else
                return 0;
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
            final CameraViewHolder cameraViewHolder;
            View view = convertView;
            int viewType = getItemViewType(position);
            Log.i("getadapter", position+"");
            if (view == null) {
                if (viewType == 0) {
                    view = inflater.inflate(R.layout.item_grid_image, parent, false);
                    imageViewHolder = new ImageViewHolder();
                    assert view != null;
                    imageViewHolder.imageView = (ImageView) view.findViewById(R.id.image);
                    view.setTag(imageViewHolder);
                } else {
                    view = inflater.inflate(R.layout.item_grid_camera, parent, false);
                    cameraViewHolder = new CameraViewHolder();
                    assert view != null;
                    view.setTag(cameraViewHolder);
                }
            }

            if (viewType == 0) {
                Log.i("ImageGrid", allImage + " " + imageUrls.get(0));
                imageViewHolder = (ImageViewHolder) view.getTag();

                final ImageView imageView = imageViewHolder.imageView;

                if(allImage)
                    position -= 1;

                Picasso.with(context)
                        .load("file://" + imageUrls.get(position))
                        .fit()
                        .centerCrop()
                        .into(imageViewHolder.imageView);
                imageView.setColorFilter(null);
                imageView.setTag(position);

                //设置ImageView的点击事件
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.Image.IMAGE_RESULT, imageUrls.get((int) v.getTag()));
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                });

            }
            else {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Create the File where the photo should go
                        try {
                            photoFile = CameraUtil.createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.i("TAKE_PHOTO", ex.getMessage());
                        }
                        ActivityTransition.takePhoto(ImageGridFragment.this, photoFile);
                    }
                });
            }
            return  view;
        }
	}

	static class ImageViewHolder {
		ImageView imageView;
//		ImageView item_select;
	}

    static class CameraViewHolder {
        ImageView cameraView;
        TextView cameraText;
    }
}