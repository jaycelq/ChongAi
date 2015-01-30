package me.qiang.android.chongai.Fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.util.ArrayList;

import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomNavigationFragment extends BaseFragment {

    UserSessionManager userSessionManager;
    private OnFragmentInteractionListener mListener;
    private RadioGroup tabRadioGroup;
    private ImageView addState;

    public BottomNavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSessionManager = GlobalApplication.getUserSessionManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.bottom_navigation, container, false);
        addState = (ImageView) rootView.findViewById(R.id.add_state);
        addState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Pet> petList = userSessionManager.getPetList();
                if(petList.size() == 0) {
                    showDialog(getActivity());
                }
                else
                    onButtonPressed(v.getId());
            }
        });

        tabRadioGroup = (RadioGroup) rootView.findViewById(R.id.tab_menu);
        tabRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                        mListener.onFragmentInteraction(checkedId);
            }
        });

        return rootView;
    }

    //显示基本的AlertDialog
    private void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("尚未添加宠物，现在添加？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityTransition.startAddPetActivity(BottomNavigationFragment.this);
                    }
                });
        builder.show();
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

    public void onButtonPressed(int id) {
        if (mListener != null) {
            mListener.onFragmentInteraction(id);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int id);
    }
}
