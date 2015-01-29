package me.qiang.android.chongai.Activity;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

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
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.CompressUploadTask;
import me.qiang.android.chongai.util.RequestServer;

public class AddPetActivity extends BaseToolbarActivity implements TextWatcher{

    private UserSessionManager userSessionManager;

    Context context;

    // UI widget
    private RadioGroup genderRadioGroup;
    private EditText petNameView;
    private EditText petTypeView;
    private Spinner petAgeView;
    private EditText petHobbyView;
    private EditText petSkillView;
    private EditText petDeviceImeiView;
    private CircleImageView petPhoto;
    private Button saveProfile;

    private ArrayAdapter<String> adapter;

    private String avatarUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        setToolbarTile("添加宠物");
        enableBackButton();

        genderRadioGroup = (RadioGroup) findViewById(R.id.pet_gender);

        petNameView = (EditText) findViewById(R.id.pet_name);
        petNameView.addTextChangedListener(this);

        petTypeView = (EditText) findViewById(R.id.pet_type);
        petNameView.addTextChangedListener(this);

        petAgeView = (Spinner) findViewById(R.id.pet_age);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, Constants.Pet.PET_AGE);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petAgeView.setAdapter(adapter);

        petSkillView = (EditText) findViewById(R.id.pet_skill);
        petSkillView.addTextChangedListener(this);

        petHobbyView = (EditText) findViewById(R.id.pet_hobby);
        petHobbyView.addTextChangedListener(this);

        petDeviceImeiView = (EditText) findViewById(R.id.pet_device_imei);

        petPhoto = (CircleImageView) findViewById(R.id.pet_photo);
        petPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.pickImageFromAlbum(AddPetActivity.this);
            }
        });

        saveProfile = (Button) findViewById(R.id.profile_complete);
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAddPet();
            }
        });
        saveProfile.setClickable(false);

        userSessionManager = GlobalApplication.getUserSessionManager();
        context = this;

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
                            .into(petPhoto);
                    if(isInputValid() == null)
                        saveProfile.setClickable(true);
                }
        }
    }

    private View isInputValid() {
        String petName = petNameView.getText().toString();
        String petType = petTypeView.getText().toString();
        String petHobby = petHobbyView.getText().toString();
        String petSkill = petSkillView.getText().toString();
        String petPhotoUrl = avatarUrl;

        View focusView = null;

        if (TextUtils.isEmpty(petName) || !isPetNameValid(petName))
            focusView = petNameView;

        if (TextUtils.isEmpty(petType) || !isPetTypeValid(petType))
            focusView = petTypeView;

        if (TextUtils.isEmpty(petHobby) || !isPetHobbyValid(petHobby))
            focusView = petHobbyView;

        if (TextUtils.isEmpty(petSkill) || !isPetSkillValid(petSkill))
            focusView = petSkillView;

        if (TextUtils.isEmpty(petPhotoUrl) || !isPetPhotoUrlValid(petPhotoUrl))
            focusView = petPhoto;

        return focusView;
    }

    private boolean isPetNameValid(String petName) {
        return petName.length() > 0;
    }

    private boolean isPetTypeValid(String petType) {
        return petType.length() > 0;
    }

    private boolean isPetHobbyValid(String petHobby) {
        return petHobby.length() > 0;
    }

    private boolean isPetSkillValid(String petSkill) {
        return petSkill.length() > 0;
    }

    private boolean isPetPhotoUrlValid(String petPhotoUrl) {
        return petPhotoUrl.length() > 0;
    }

    private void attemptAddPet() {
        final String petName = petNameView.getText().toString();
        final String petType = petTypeView.getText().toString();
        final String petHobby = petHobbyView.getText().toString();
        final String petSkill = petSkillView.getText().toString();
        final String petDeviceImei = petDeviceImeiView.getText().toString();
        final Pet.Gender petGender;
        final String petAge;
        String petPhotoUrl = avatarUrl;

        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.female:
                petGender = Pet.Gender.FEMALE;
                break;
            default:
                petGender = Pet.Gender.MALE;
                break;
        }

        petAge = Constants.Pet.PET_AGE[petAgeView.getSelectedItemPosition()];

        View focusView = isInputValid();

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            showProgressDialog("正在处理...");
            CompressUploadTask compressUploadTask = new CompressUploadTask() {
                @Override
                protected void onPostExecute(InputStream inputStream) {
                    Pet pet = new Pet(petName, petGender, petType, petHobby,
                            petAge, petSkill, petDeviceImei);
                    RequestServer.addPet(pet, inputStream, avatarUrl, newAddPetCallback());
                }
            };
            compressUploadTask.execute(avatarUrl);
        }

    }

    private JsonHttpResponseHandler newAddPetCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                try {
                    if (response.getInt("status") == 0) {
                        JSONObject petJsonObject = response.getJSONObject("body");
                        Pet pet = gson.fromJson(petJsonObject.toString(), Pet.class);
                        userSessionManager.addPet(pet);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.Pet.PET_ADD_RESULT, pet.getPetId());
                        hideProgressDialog();
                        AddPetActivity.this.setResult(Activity.RESULT_OK, intent);
                        AddPetActivity.this.finish();
                    }
                } catch (JSONException ex) {
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                hideProgressDialog();
                new AlertDialog.Builder(AddPetActivity.this).setMessage("网络连接出现问题").
                        setPositiveButton("确定", null).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
                hideProgressDialog();
                new AlertDialog.Builder(AddPetActivity.this).setMessage("服务器故障,请稍后重试").
                        setPositiveButton("确定", null).show();
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_pet, menu);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(isInputValid() == null) {
            saveProfile.setClickable(true);
        }
        else
            saveProfile.setClickable(false);
    }
}
