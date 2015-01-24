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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Activity.StateEdit;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.AlbumHelper;
import me.qiang.android.chongai.util.CameraUtil;


/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridFragment extends AbsListViewBaseFragment implements ListDirFragment.OnListDirSelectedListener{

    private final static int SCAN_OK = 1;
	List<String> imageUrls = new ArrayList<String>();
    public  List<String> mSelectedImage = new ArrayList<String>();

	DisplayImageOptions options;
    private AlbumHelper helper;
    private ImageAdapter adapter;
    private RelativeLayout mBottomLy;
    private TextView albumChoosed;
    private TextView albumImageCount;
    private boolean allImage;

    private ListDirFragment listDirFragment;
    private int positionSelected;

    private File photoFile;
    static final int REQUEST_TAKE_PHOTO = 1;

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

    private void startStateEditActivity(String imageFile) {
        Intent intent = new Intent(getActivity(), StateEdit.class);
        intent.putExtra("STATE_PHOTO", imageFile);
        Log.i("CAMERA_CAPTURE", imageFile);
        startActivity(intent);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = CameraUtil.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("TAKE_PHOTO", ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if(resultCode == Activity.RESULT_OK) {
                try {
                    //startStateEditActivity(photoFile.getCanonicalPath());
                    Intent intent = new Intent();
                    intent.putExtra("IMAGE_PHOTO", photoFile.getCanonicalPath());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i("TAKE_PHOTO", ex.getMessage());

                }
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

                ImageLoader.getInstance()
                        .displayImage("file://" + imageUrls.get(position), imageViewHolder.imageView, options);

                imageView.setColorFilter(null);
                imageView.setTag(position);

                //设置ImageView的点击事件
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //startStateEditActivity(imageUrls.get((int) v.getTag()));
                        Intent intent = new Intent();
                        intent.putExtra("img_url", imageUrls.get((int) v.getTag()));
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                });

            }
            else {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        takePhoto();
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