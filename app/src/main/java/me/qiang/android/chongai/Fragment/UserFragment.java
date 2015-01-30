package me.qiang.android.chongai.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.RequestServer;

/**
 * Created by qiang on 1/28/2015.
 */
public class UserFragment extends BaseFragment {
    private Context context;

    private UserSessionManager userSessionManager;
    private int currentUserId;
    private User user;
    private int userId;
    private int tabId = 0;

    private ArrayList<Pet> userPetList;

    private PullToRefreshListView userProfileList;

    // UI widget in header view
    private View profileHeader;
    private CircleImageView userPhoto;
    private TextView userStateNum;
    private TextView userFansNum;
    private TextView userFollowsNum;

    private View profileDataView;
    private LinearLayout userPetContainer;
    private TextView userLocation;
    private TextView userGender;
    private ImageView userGenderIcon;
    private TextView userSignatrure;


    private ProfileAdapter profileAdapter;

    public UserFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSessionManager = GlobalApplication.getUserSessionManager();
        currentUserId = userSessionManager.getCurrentUser().getUserId();
        userId = getActivity().getIntent().getIntExtra(Constants.User.USER_ID, currentUserId);
        context = getActivity();
        setHasOptionsMenu(userId == currentUserId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_pet:
                ActivityTransition.startAddPetActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.Pet.ADD_PET) {
            if (resultCode == Activity.RESULT_OK) {
                int petId = data.getExtras().getInt(Constants.Pet.PET_ADD_RESULT);
                Pet pet = userSessionManager.getPet(petId);
                if(pet != null) {
                    addBriefPetItem(pet);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        profileAdapter = new ProfileAdapter();

        userProfileList = (PullToRefreshListView) rootView.findViewById(R.id.list);

        profileHeader = getActivity().getLayoutInflater().inflate(R.layout.profile_header, null);
        userPhoto = (CircleImageView) profileHeader.findViewById(R.id.user_photo);
        userStateNum = (TextView) profileHeader.findViewById(R.id.state_num);
        userFansNum = (TextView) profileHeader.findViewById(R.id.fans_num);
        userFollowsNum = (TextView) profileHeader.findViewById(R.id.follow_num);

        profileDataView = inflater.inflate(R.layout.profile_data, container, false);
        userPetContainer = (LinearLayout) profileDataView.findViewById(R.id.pet_container);
        userLocation = (TextView) profileDataView.findViewById(R.id.user_location);
        userGender =(TextView) profileDataView.findViewById(R.id.user_gender);
        userGenderIcon = (ImageView) profileDataView.findViewById(R.id.user_gender_icon);
        userSignatrure = (TextView) profileDataView.findViewById(R.id.user_signature);

        userProfileList.getRefreshableView().addHeaderView(profileHeader);

        userProfileList.setAdapter(profileAdapter);

        rootView.post(new Runnable() {
            @Override
            public void run() {
                RequestServer.getUserInfo(userId, newGetUserInfoCallback());
            }
        });

        return rootView;
    }

    private JsonHttpResponseHandler newGetUserInfoCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new Gson();
                try {
                    JSONObject userObject = response.getJSONObject("body").getJSONObject("user");
                    user = gson.fromJson(userObject.toString(), User.class);
                    Type PetListType = new TypeToken<ArrayList<Pet>>(){}.getType();
                    JSONArray petListObject = response.getJSONObject("body").getJSONArray("pets");
                    userPetList = gson.fromJson(petListObject.toString(), PetListType);
                    Picasso.with(context)
                            .load(user.getUserPhoto())
                            .fit()
                            .centerCrop()
                            .into(userPhoto);
                    userStateNum.setText(user.post_num + "");
                    userFollowsNum.setText(user.follow_num + "");
                    userFansNum.setText(user.fans_num + "");

                    userLocation.setText(user.getUserLocation());
                    if(user.getUserGender() == User.Gender.FEMALE) {
                        userGender.setText("女");
                        userGenderIcon.setImageResource(R.drawable.female);
                    }
                    else {
                        userGender.setText("男");
                        userGenderIcon.setImageResource(R.drawable.male);
                    }
                    userSignatrure.setText(user.signature);
                    for(int i = 0; i < userPetList.size(); i++) {
                        Pet pet = userPetList.get(i);
                        addBriefPetItem(pet);
                    }

                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        };
    }

    private void addBriefPetItem(Pet pet) {
        View petBriefView = getActivity().getLayoutInflater().inflate(R.layout.pet_brief_item, userPetContainer, false);
        CircleImageView petPhoto = (CircleImageView) petBriefView.findViewById(R.id.pet_photo);
        TextView petName = (TextView) petBriefView.findViewById(R.id.pet_name);
        petName.setText(pet.getPetName());
        TextView petType = (TextView) petBriefView.findViewById(R.id.pet_type);
        petType.setText(pet.getPetType());
        userPetContainer.addView(petBriefView);
        Picasso.with(context)
                .load(pet.getPetPhoto())
                .fit()
                .centerCrop()
                .into(petPhoto);
        petPhoto.setTag(pet.getPetId());
        petPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startPetActivity(UserFragment.this, (int) v.getTag());
            }
        });
    }

    public class ProfileAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ProfileAdapter() {
            inflater = getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return 1;
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
            return profileDataView;
        }
    }
}
