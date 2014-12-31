package me.qiang.android.chongai.Fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import me.qiang.android.chongai.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewStateFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;
    private ImageButton takePhoto;
    private ImageButton pickPhoto;

    public CreateNewStateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.create_new_popup, container, false);
        takePhoto = (ImageButton) rootView.findViewById(R.id.take_photo);
        pickPhoto = (ImageButton) rootView.findViewById(R.id.pick_photo);

        rootView.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        pickPhoto.setOnClickListener(this);

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

    public interface OnFragmentInteractionListener {
        public void onCreateNewFragmentClick(int id);
    }

}
