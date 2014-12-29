package me.qiang.android.chongai.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class BottomPopupFragment extends Fragment {

    private ImageView close;

    public BottomPopupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_navigation_popup, container, false);
        close = (ImageView) rootView.findViewById(R.id.close);
        return rootView;
    }


    @Override
    public Animation onCreateAnimation (int transit, boolean enter, int nextAnim) {
        Animation anim;
        if (enter) {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_fade_in);
        } else {
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_fade_out);
        }

        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                }

            public void onAnimationRepeat(Animation animation) { }

            public void onAnimationStart(Animation animation) {
                close.startAnimation(
                    AnimationUtils.loadAnimation(getActivity(), R.anim.close_button_rotate) );
            }
        });

        return anim;
    }
}
