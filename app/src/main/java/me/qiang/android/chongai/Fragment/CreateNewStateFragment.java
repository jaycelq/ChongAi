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

import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.BlurBackground;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewStateFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;
    private ImageView takePhoto;
    private ImageView pickPhoto;
    private ImageView takeVideo;

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
        pickPhoto = (ImageView) rootView.findViewById(R.id.pick_photo);
        takeVideo = (ImageView) rootView.findViewById(R.id.take_video);

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
            Log.i("POPUP", "leaving");
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        }

        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) { }

            public void onAnimationStart(Animation animation) {
                if(enter) {
                    takePhoto.startAnimation(
                            AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in));
                    pickPhoto.startAnimation(
                            AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in));
                    takeVideo.startAnimation(
                            AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in));
                }
            }
        });

        return anim;
    }

    public interface OnFragmentInteractionListener {
        public void onCreateNewFragmentClick(int id);
    }

}
