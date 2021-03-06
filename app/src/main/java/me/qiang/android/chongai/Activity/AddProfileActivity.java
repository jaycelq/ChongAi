package me.qiang.android.chongai.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.CompressUploadTask;
import me.qiang.android.chongai.util.RequestServer;

public class AddProfileActivity extends BaseToolbarActivity implements TextWatcher{
    private UserSessionManager userSessionManager;

    Context context;

    // UI widget
    private RadioGroup genderRadioGroup;
    private EditText profileName;
    private EditText profileLocation;
    private EditText profileSignature;
    private CircleImageView profilePhoto;
    private Button saveProfile;

    private int userId = 0;
    private User currentUser;

    private String avatarUrl = null;
    private Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        enableBackButton();

        userId = getIntent().getIntExtra(Constants.User.USER_ID, 0);
        if(userId == 0)
            setToolbarTile("我的资料");
        else
            setToolbarTile("更新资料");

        saveProfile = (Button) findViewById(R.id.profile_complete);
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userId == 0)
                    attemptSaveProfile();
                else
                    attemptUpdateProfile();
            }
        });
        saveProfile.setClickable(false);

        genderRadioGroup = (RadioGroup) findViewById(R.id.profile_gender);

        profileName = (EditText) findViewById(R.id.profile_name);
        profileName.addTextChangedListener(this);

        profileLocation = (EditText) findViewById(R.id.profile_location);
        profileLocation.addTextChangedListener(this);

        profileSignature = (EditText) findViewById(R.id.profile_signature);
        profileSignature.addTextChangedListener(this);

        profilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.pickImageFromAlbum(AddProfileActivity.this);
            }
        });

        userSessionManager = GlobalApplication.getUserSessionManager();
        context = this;

        if(userId != 0) {
            currentUser = userSessionManager.getCurrentUser();
            assert userId == currentUser.getUserId();

            Picasso.with(context)
                    .load(currentUser.getUserPhoto())
                    .fit()
                    .centerCrop()
                    .into(profilePhoto);

            if(currentUser.getUserGender() == User.Gender.FEMALE)
                genderRadioGroup.check(R.id.female);
            else
                genderRadioGroup.check(R.id.male);

            profileName.setText(currentUser.getUserName());
            profileLocation.setText(currentUser.getUserLocation());
            profileSignature.setText(currentUser.getUserLocation());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.Image.PICK_IMAGE:
                if(resultCode == RESULT_OK) {
                    avatarUrl = data.getExtras().getString(Constants.Image.IMAGE_RESULT);
                    Log.i("PHOTO_URL", avatarUrl);
                    Picasso.with(context)
                            .load("file://" + avatarUrl)
                            .fit()
                            .centerCrop()
                            .into(profilePhoto);
                    if(isInputValid(avatarUrl) == null)
                        saveProfile.setClickable(true);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void attemptUpdateProfile() {
        final String userName = profileName.getText().toString();
        final String userLocation = profileLocation.getText().toString();
        final String userSignature = profileSignature.getText().toString();
        final User.Gender userGender;
        final String userPhotoUrl;

        if(avatarUrl != null) {
            userPhotoUrl = avatarUrl;
        }
        else
            userPhotoUrl = currentUser.getUserPhoto();

        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.female:
                userGender = User.Gender.FEMALE;
                break;
            default:
                userGender = User.Gender.MALE;
                break;
        }

        View focusView = isInputValid(userPhotoUrl);

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            showProgressDialog("正在处理...");
            if(avatarUrl != null) {
                CompressUploadTask compressUploadTask = new CompressUploadTask() {
                    @Override
                    protected void onPostExecute(InputStream inputStream) {
                        User user = new User(userId, userName, userGender,
                                userLocation, userSignature, userPhotoUrl);
                        RequestServer.updateProfile(user, inputStream, avatarUrl, newSaveProfileCallback());
                    }
                };
                compressUploadTask.execute(avatarUrl);
            }
            else {
                User user = new User(userId, userName, userGender,
                        userLocation, userSignature, userPhotoUrl);
                RequestServer.updateProfile(user, newSaveProfileCallback());
            }
        }
    }

    public void attemptSaveProfile() {
        final String userName = profileName.getText().toString();
        final String userLocation = profileLocation.getText().toString();
        final String userSignature = profileSignature.getText().toString();
        final User.Gender userGender;
        final String userPhotoUrl = avatarUrl;

        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.female:
                userGender = User.Gender.FEMALE;
                break;
            default:
                userGender = User.Gender.MALE;
                break;
        }

        View focusView = isInputValid(userPhotoUrl);

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            showProgressDialog("正在处理...");
            CompressUploadTask compressUploadTask = new CompressUploadTask() {
                @Override
                protected void onPostExecute(InputStream inputStream) {
                    User user = new User(userName, userGender,
                            userLocation, userSignature, userPhotoUrl);
                    RequestServer.saveProfile(user, inputStream, avatarUrl, newSaveProfileCallback());
                }
            };
            compressUploadTask.execute(avatarUrl);
        }
    }

    private View isInputValid(String userAvatar) {
        String userName = profileName.getText().toString();
        String userLocation = profileLocation.getText().toString();
        String userSignature = profileSignature.getText().toString();
        String userPhotoUrl = userAvatar;

        View focusView = null;

        if (TextUtils.isEmpty(userName) || !isUserNameValid(userName))
            focusView = profileName;

        if (TextUtils.isEmpty(userLocation) || !isUserLocationValid(userLocation))
            focusView = profileLocation;

        if (TextUtils.isEmpty(userSignature) || !isUserSignatureValid(userSignature))
            focusView = profileSignature;

        if (TextUtils.isEmpty(userPhotoUrl) || !isUserPhotoUrlValid(userPhotoUrl))
            focusView = profilePhoto;

        return focusView;
    }

    private boolean isUserNameValid(String userName) {
        return userName.length() > 0;
    }

    private boolean isUserLocationValid(String userLocation) {
        return userLocation.length() > 0;
    }

    private boolean isUserSignatureValid(String userSignature) {
        return userSignature.length() > 0;
    }

    private boolean isUserPhotoUrlValid(String userPhotoUrl) {
        return userPhotoUrl.length() > 0;
    }


    private JsonHttpResponseHandler newSaveProfileCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                try {
                    if (response.getInt("status") == 0) {
                        JSONObject userJsonObject = response.getJSONObject("body");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        userSessionManager.updateUserProfile(true, currentUser);
                        ActivityTransition.startMainActivity(AddProfileActivity.this);
                        AddProfileActivity.this.finish();
                    }
                } catch (JSONException ex) {
                }
                hideProgressDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                hideProgressDialog();
                new AlertDialog.Builder(AddProfileActivity.this).setMessage("网络连接出现问题").
                        setPositiveButton("确定", null).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
                hideProgressDialog();
                new AlertDialog.Builder(AddProfileActivity.this).setMessage("服务器故障,请稍后重试").
                        setPositiveButton("确定", null).show();
            }
        };
    }

    @Override
    public void afterTextChanged(Editable s) {
        String userAvatar;

        if(userId == 0)
            userAvatar = avatarUrl;
        else if (userId != 0 && avatarUrl != null)
            userAvatar = avatarUrl;
        else
            userAvatar = currentUser.getUserPhoto();

        if(isInputValid(userAvatar) == null) {
            saveProfile.setClickable(true);
        }
        else
            saveProfile.setClickable(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
}
