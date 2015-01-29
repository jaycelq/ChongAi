package me.qiang.android.chongai.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.MD5;
import me.qiang.android.chongai.util.RequestServer;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.Model.UserSessionManager;

/**
 * A login screen that offers login via phone/password.
 */
public class LoginActivity extends BaseLoginRegisterActivity implements TextWatcher{

    private Button signInButton;

    private UserSessionManager userSessionManager;

    private void setupUI() {
        // Set up the login form.
        mPhoneNumberView = (EditText) findViewById(R.id.phone);
        mPhoneNumberView.addTextChangedListener(this);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.addTextChangedListener(this);

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        signInButton.setClickable(false);

        Button mRegisterButton = (Button) findViewById(R.id.welcome_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startRegisterActivity(LoginActivity.this);
            }
        });

        Button mAnonymousVisitButton = (Button) findViewById(R.id.anonymous_visit);
        mAnonymousVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startMainActivity(LoginActivity.this);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userSessionManager = GlobalApplication.getUserSessionManager();

        if(userSessionManager.isUserLoggedIn() && userSessionManager.hasProfile()) {
            ActivityTransition.startMainActivity(this);
            finish();
        }
        else if(userSessionManager.isUserLoggedIn() && !userSessionManager.hasProfile()) {
            ActivityTransition.startAddProfileActivity(this);
            finish();
        }

        setupUI();
    }

    //TODO: deal with ui change when button clicked or not
    @Override
    public void afterTextChanged(Editable s) {
        if(isInputValid() == null)
            signInButton.setClickable(true);
        else
            signInButton.setClickable(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }


    private JsonHttpResponseHandler newLoginCallback(final String phoneNumber, final String md5Password) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                hideProgressDialog();
                try {
                    if(response.getInt("status") == 0) {
                        JSONObject userJsonObject = response.getJSONObject("body").getJSONObject("user");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        JSONArray petJsonArray = response.getJSONObject("body").getJSONArray("pets");
                        Type PetListType = new TypeToken<ArrayList<Pet>>(){}.getType();
                        ArrayList<Pet> petList = gson.fromJson(petJsonArray.toString(), PetListType);
                        userSessionManager.createUserLoginSession(phoneNumber, md5Password,
                                true, currentUser, petList);
                        ActivityTransition.startMainActivity(LoginActivity.this);
                        LoginActivity.this.finish();
                    }
                    else if(response.getInt("status") == Constants.Login.NO_USER_PROFILE) {
                        JSONObject userJsonObject = response.getJSONObject("error");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        userSessionManager.createUserLoginSession(phoneNumber, md5Password,
                                false, currentUser, null);
                        Log.i("JSON", "" + userSessionManager.getCurrentUser().getUserId());
                        ActivityTransition.startAddProfileActivity(LoginActivity.this);
                        LoginActivity.this.finish();
                    }
                    else {
                        new AlertDialog.Builder(LoginActivity.this).setMessage("手机号或密码错误").
                                setPositiveButton("确定", null).show();
                    }

                } catch (JSONException ex)
                {}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                hideProgressDialog();
                new AlertDialog.Builder(LoginActivity.this).setMessage("网络连接出现问题").
                        setPositiveButton("确定", null).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
                hideProgressDialog();
                new AlertDialog.Builder(LoginActivity.this).setMessage("服务器故障").
                        setPositiveButton("确定", null).show();
            }
        };
    }


    public void attemptLogin() {
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        View focusView = isInputValid();

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            showProgressDialog("正在登录...");
            RequestServer.login(phoneNumber, MD5.md5(password), newLoginCallback(phoneNumber, password));
        }
    }

    private View isInputValid() {
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password))
            focusView = mPasswordView;

        // Check for a valid phone number.
        if (TextUtils.isEmpty(phoneNumber))
            focusView = mPhoneNumberView;
        else if (!isPhoneNumberValid(phoneNumber))
            focusView = mPhoneNumberView;

        return focusView;
    }


}



