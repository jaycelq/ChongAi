package me.qiang.android.chongai.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.CompressUploadTask;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.User;
import me.qiang.android.chongai.util.UserSessionManager;

public class AddProfileActivity extends BaseToolbarActivity implements TextWatcher{
    private UserSessionManager userSessionManager;

    private DisplayImageOptions options;

    // UI widget
    private RadioGroup genderRadioGroup;
    private EditText profileName;
    private EditText profileLocation;
    private EditText profileSignature;
    private CircleImageView profilePhoto;
    private Button saveProfile;

    private String avatarUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        setToolbarTile("我的资料");
        enableBackButton();

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

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
                pickImageFromAlbum();
            }
        });

        saveProfile = (Button) findViewById(R.id.profile_complete);
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveProfile();
            }
        });
        saveProfile.setClickable(false);

        userSessionManager = GlobalApplication.getUserSessionManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.Album.PICK_IMAGE:
                if(resultCode == RESULT_OK) {
                    avatarUrl = data.getExtras().getString("img_url");
                    Log.i("PHOTO_URL", avatarUrl);
                    ImageLoader.getInstance()
                            .displayImage("file://" + avatarUrl, profilePhoto, options);
                    if(isInputValid() == null)
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

    public void pickImageFromAlbum() {
        Intent intent = new Intent(this, CustomAlbum.class);
        startActivityForResult(intent, Constants.Album.PICK_IMAGE);
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

        View focusView = isInputValid();

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            CompressUploadTask compressUploadTask = new CompressUploadTask() {
                @Override
                protected void onPostExecute(InputStream inputStream) {
                    User user = new User(userName, userGender,
                            userLocation, userSignature, userPhotoUrl);
                    saveProfile(user, inputStream);
                }
            };
            compressUploadTask.execute(avatarUrl);
        }
    }

    private View isInputValid() {
        String userName = profileName.getText().toString();
        String userLocation = profileLocation.getText().toString();
        String userSignature = profileSignature.getText().toString();
        String userPhotoUrl = avatarUrl;

        View focusView = null;

        if (TextUtils.isEmpty(userName) || !isUserNameValid(userName))
            focusView = profileName;

        if (TextUtils.isEmpty(userLocation))
            focusView = profileLocation;
        else if (!isUserLocationValid(userLocation))
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

    private void startMainActivity() {
        Intent registerIntent = new Intent(this, MainActivity.class);
        startActivity(registerIntent);
        this.finish();
    }

    private void saveProfile(User user, InputStream avatarPic) {
        RequestParams params = new RequestParams();

        final Gson gson = new Gson();
        String userInfo = gson.toJson(user, User.class);

        params.put("avatar_pic", avatarPic, avatarUrl);
        params.put("user", userInfo);
        HttpClient.post("user/newProfile", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                try {
                    if (response.getInt("status") == 0) {
                        JSONObject userJsonObject = response.getJSONObject("body");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        userSessionManager.updateUserProfile(true, currentUser);
                        startMainActivity();
                    }
                } catch (JSONException ex) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
            }
        });
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(isInputValid() == null) {
            Log.i("ADD_PROFILE", "clickable");
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
