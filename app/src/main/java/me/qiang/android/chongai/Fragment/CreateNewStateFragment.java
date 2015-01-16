package me.qiang.android.chongai.Fragment;


import android.app.Activity;
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

import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.BlurBackground;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewStateFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;
    private ImageView takePhoto;
    private TextView takePhotoText;
    private ImageView pickPhoto;
    private TextView pickPhotoText;
    private ImageView takeVideo;
    private TextView takeVideoText;

    public CreateNewStateFragment() {
        // Required empty public constructor

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onClick(View v) {
        mListener.onCreateNewFragmentClick(v.getId());
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

    public interface OnFragmentInteractionListener {
        public void onCreateNewFragmentClick(int id);
    }

}
