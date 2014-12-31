package me.qiang.android.chongai.Fragment;


import android.app.Activity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomPopupFragment extends Fragment implements View.OnClickListener{

    private ImageView close;
    private OnFragmentInteractionListener mListener;

    public BottomPopupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_navigation_popup, container, false);
        close = (ImageView) rootView.findViewById(R.id.close);
        close.setOnClickListener(this);
        rootView.setOnClickListener(this);

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
                    close.startAnimation(
                            AnimationUtils.loadAnimation(getActivity(), R.anim.close_button_rotate));
                }
            }
        });

        return anim;
    }

    @Override
    public void onClick(View v) {
        Animation anim;
        anim = AnimationUtils.loadAnimation(getActivity(), R.anim.close_button_rotate_reverse);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListener.onCloseButtonClicked();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        close.setImageResource(R.drawable.ic_add_black_48dp);
        close.startAnimation(anim);
    }

    public interface OnFragmentInteractionListener {
        public void onCloseButtonClicked();
    }
}
