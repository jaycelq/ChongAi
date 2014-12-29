package me.qiang.android.chongai.Fragment;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.qiang.android.chongai.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewStateFragment extends Fragment {


    public CreateNewStateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_popup, container, false);
    }


}
