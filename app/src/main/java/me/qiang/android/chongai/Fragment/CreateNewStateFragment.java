package me.qiang.android.chongai.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import me.qiang.android.chongai.Activity.StateEdit;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.BlurBackground;
import me.qiang.android.chongai.util.CameraUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewStateFragment extends Fragment implements View.OnClickListener{

    private ImageView takePhoto;
    private TextView takePhotoText;
    private ImageView pickPhoto;
    private TextView pickPhotoText;
    private ImageView takeVideo;
    private TextView takeVideoText;

    private File photoFile = null;

    public CreateNewStateFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bitmap background = BlurBackground.getBitmapFromView(container);
        View rootView = inflater.inflate(R.layout.create_new_popup, container, false);
        background = BlurBackground.fastblur(getActivity(), background, 5);
        BitmapDrawable ob = new BitmapDrawable(getResources(), background);
        ob.setColorFilter(Color.rgb(150, 150, 150), android.graphics.PorterDuff.Mode.MULTIPLY);
        rootView.setBackgroundDrawable(ob);
        takePhoto = (ImageView) rootView.findViewById(R.id.take_photo);
        takePhotoText = (TextView) rootView.findViewById(R.id.take_photo_text);
        pickPhoto = (ImageView) rootView.findViewById(R.id.pick_photo);
        pickPhotoText = (TextView) rootView.findViewById(R.id.pick_photo_text);
        takeVideo = (ImageView) rootView.findViewById(R.id.take_video);
        takeVideoText = (TextView) rootView.findViewById(R.id.take_video_text);

        rootView.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        pickPhoto.setOnClickListener(this);
        takePhoto.setOnClickListener(this);

        return rootView;
    }

    @Override
    public Animation onCreateAnimation (int transit, final boolean enter, int nextAnim) {
        Animation anim;
        if (enter) {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_fade_in);
        } else {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        }

        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) { }

            public void onAnimationStart(Animation animation) {
                if(enter) {
                    Animation zoomInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in);

                    zoomInAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_fade_in);
                            fadeInAnimation.setDuration(200);
                            takePhotoText.startAnimation(fadeInAnimation);
                            pickPhotoText.startAnimation(fadeInAnimation);
                            takeVideoText.startAnimation(fadeInAnimation);
                            takePhotoText.post(new Runnable() {
                                @Override
                                public void run() {
                                    takePhotoText.setVisibility(View.VISIBLE);
                                }
                            });
                            pickPhotoText.post(new Runnable() {
                                @Override
                                public void run() {
                                    pickPhotoText.setVisibility(View.VISIBLE);
                                }
                            });
                            takeVideoText.post(new Runnable() {
                                @Override
                                public void run() {
                                    takeVideoText.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    takePhoto.startAnimation(zoomInAnimation);
                    pickPhoto.startAnimation(zoomInAnimation);
                    takeVideo.startAnimation(zoomInAnimation);
                }
            }
        });

        return anim;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.pop_up_bg:
                getActivity().onBackPressed();
                break;
            case R.id.pick_photo:
                ActivityTransition.pickImageFromAlbum(this);
                break;
            case R.id.take_photo:
                // Create the File where the photo should go
                try {
                    photoFile = CameraUtil.createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i("TAKE_PHOTO", ex.getMessage());
                }
                ActivityTransition.takePhoto(this, photoFile);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == Constants.Image.TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    startStateEditActivity(photoFile.getCanonicalPath());
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i("TAKE_PHOTO", ex.getMessage());
                }
            }
            else {
                photoFile.delete();
            }
        }
        else if(requestCode == Constants.Image.PICK_IMAGE) {
            if(resultCode == Activity.RESULT_OK) {
                String photoUrl = data.getExtras().getString(Constants.Image.IMAGE_RESULT);
                startStateEditActivity(photoUrl);
            }
        }
    }

    private void startStateEditActivity(String imageFile) {
        Intent intent = new Intent(getActivity(), StateEdit.class);
        intent.putExtra(Constants.Image.IMAGE_RESULT, imageFile);
        Log.i("CAMERA_CAPTURE", imageFile);
        startActivity(intent);
    }

}
