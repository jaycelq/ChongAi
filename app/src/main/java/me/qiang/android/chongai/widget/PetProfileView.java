package me.qiang.android.chongai.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Activity.BaseToolbarActivity;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.IMEI;
import me.qiang.android.chongai.util.RequestServer;

/**
 * Created by LiQiang on 26/1/15.
 */
public class PetProfileView extends RelativeLayout {
    private Context context;

    private UserSessionManager userSessionManager;

    public CircleImageView petPhoto;
    public TextView petMetaName;
    public TextView petMetaType;
    public CircleImageView petOwnerPhoto;
    public TextView petDeviceStatus;
    public TextView petHobby;
    public TextView petAge;
    public TextView petSkill;
    public TextView petName;

    private AlertDialog imeiInputDialog;

    private Pet currentPet;

    public PetProfileView(Context context) {
        this(context, null);
        this.context = context;
    }

    public PetProfileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public PetProfileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        userSessionManager = GlobalApplication.getUserSessionManager();

        LayoutInflater.from(context).inflate(R.layout.pet_profile, this, true);

        petPhoto = (CircleImageView) findViewById(R.id.pet_photo);
        petMetaName = (TextView) findViewById(R.id.pet_meta_name);
        petMetaType = (TextView) findViewById(R.id.pet_meta_type);
        petOwnerPhoto = (CircleImageView) findViewById(R.id.pet_owner_photo);
        petDeviceStatus = (TextView) findViewById(R.id.pet_device_status);
        petHobby = (TextView) findViewById(R.id.pet_hobby);
        petAge = (TextView) findViewById(R.id.pet_age);
        petSkill = (TextView) findViewById(R.id.pet_skill);
        petName = (TextView) findViewById(R.id.pet_name);
    }

    public void updatePetProfileView(final Pet pet) {
        Picasso.with(context)
                .load(pet.getPetPhoto())
                .fit()
                .centerCrop()
                .into(petPhoto);

        currentPet = pet;

        petMetaName.setText(pet.getPetName());
        petName.setText(pet.getPetName());

        petMetaType.setText(pet.getPetType());

        if (pet.getPetUserId() != userSessionManager.getCurrentUser().getUserId()) {
            petDeviceStatus.setVisibility(View.GONE);
            petOwnerPhoto.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(pet.getPetOwner().getUserPhoto())
                    .fit()
                    .centerCrop()
                    .into(petOwnerPhoto);
            petOwnerPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            petDeviceStatus.setVisibility(VISIBLE);
            petOwnerPhoto.setVisibility(View.GONE);
            if (IMEI.isIMEIValid(pet.getImei())) {
                petDeviceStatus.setText("点击更改设备");
            } else {
                petDeviceStatus.setText("点击绑定设备");
            }
            petDeviceStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    final View imeiInputView = inflater.inflate(R.layout.imei_input, null);
                    final EditText imeiInput = (EditText) imeiInputView.findViewById(R.id.imei_input);
                    final AlertDialog.Builder imeiInputDialogBuilder = new AlertDialog
                            .Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
                    imeiInputDialogBuilder.setTitle("输入IMEI号：");
                    imeiInputDialogBuilder.setView(imeiInputView);
                    imeiInputDialogBuilder.setPositiveButton("确认", null);
                    imeiInputDialogBuilder.setNegativeButton("取消", null);

                    imeiInputDialog = imeiInputDialogBuilder.create();

                    imeiInputDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialog) {

                            final Button positiveButton = imeiInputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            final int buttonColor = positiveButton.getCurrentTextColor();
                            positiveButton.setTextColor(Color.GRAY);
                            positiveButton.setClickable(false);
                            imeiInput.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if(IMEI.isIMEIValid(s.toString())) {
                                        positiveButton.setClickable(true);
                                        positiveButton.setTextColor(buttonColor);
                                    }
                                    else {
                                        positiveButton.setClickable(false);
                                        positiveButton.setTextColor(Color.GRAY);
                                    }
                                }
                            });
                            positiveButton.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    // TODO Do something
                                    String imei = imeiInput.getText().toString();
                                    ((BaseToolbarActivity)context).showProgressDialog("正在处理...");
                                    RequestServer.bindLocationDevice(pet.getPetId(),
                                            imei, newBindImeiCallback(pet.getPetId()));
                                }
                            });
                        }
                    });

                    imeiInputDialog.show();

                }
            });
        }

        petHobby.setText(pet.getPetHobby());
        petAge.setText(Constants.Pet.PET_AGE[pet.getPetAgeIndex()]);
        petSkill.setText(pet.getPetSkill());
    }

    private JsonHttpResponseHandler newBindImeiCallback(final int petId) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                ((BaseToolbarActivity)context).hideProgressDialog();
                Gson gson = new Gson();
                try {
                    if (response.getInt("status") == 0) {
                        JSONObject petJsonObject = response.getJSONObject("body");
                        Pet pet = gson.fromJson(petJsonObject.toString(), Pet.class);
                        userSessionManager.updatePet(petId, pet);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.Pet.PET_UPDATE_RESULT, pet.getPetId());
                        imeiInputDialog.dismiss();
                    }
                } catch (JSONException ex) {
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                ((BaseToolbarActivity)context).hideProgressDialog();
                new AlertDialog.Builder(context).setMessage("网络连接出现问题").
                        setPositiveButton("确定", null).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
                ((BaseToolbarActivity)context).hideProgressDialog();
                new AlertDialog.Builder(context).setMessage("服务器故障,请稍后重试").
                        setPositiveButton("确定", null).show();
            }
        };
    }

}
