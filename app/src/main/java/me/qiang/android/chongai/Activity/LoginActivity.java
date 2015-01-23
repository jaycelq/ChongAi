package me.qiang.android.chongai.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.MD5;
import me.qiang.android.chongai.util.User;
import me.qiang.android.chongai.util.UserSessionManager;

/**
 * A login screen that offers login via phone/password.
 */
// TODO: change UI to adapter phone and password satisfaction
public class LoginActivity extends BaseLoginRegisterActivity implements TextWatcher{

    private UserSessionManager userSessionManager;

    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userSessionManager = GlobalApplication.getUserSessionManager();

        if(userSessionManager.isUserLoggedIn() && userSessionManager.hasProfile())
            startMainActivity();
        else if(userSessionManager.isUserLoggedIn() && !userSessionManager.hasProfile())
            startAddProfileActivity();

        // Set up the login form.
        mPhoneNumberView = (EditText) findViewById(R.id.phone);

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

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.welcome_register);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterActivity();
            }
        });

        Button mAnonymousVisitButton = (Button) findViewById(R.id.anonymous_visit);
        mAnonymousVisitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        showProgress(false, "");
        Log.i("Login", "onCreate");
    }

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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        View focusView = isInputValid();

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true, "正在登录...");
            login(phoneNumber, MD5.md5(password));
        }
    }

    private View isInputValid() {
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
            focusView = mPasswordView;

        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber))
            focusView = mPhoneNumberView;
        else if (!isPhoneNumberValid(phoneNumber))
            focusView = mPhoneNumberView;

        return focusView;
    }

    private void startRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void startMainActivity() {
        Intent registerIntent = new Intent(this, MainActivity.class);
        startActivity(registerIntent);
        this.finish();
    }

    private void startAddProfileActivity() {
        Intent addProfileIntent = new Intent(this, AddProfileActivity.class);
        startActivity(addProfileIntent);
        this.finish();
    }

    public void login(final String phoneNumber, final String md5Password){
        JSONObject userInfo = new JSONObject();
        try {
            userInfo.put("phone_number", phoneNumber);
            userInfo.put("password", md5Password);
        } catch (JSONException ex) {
            // 键为null或使用json不支持的数字格式(NaN, infinities)
            throw new RuntimeException(ex);
        }
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("act", "login");
        params.put("userinfo", userInfo);
        HttpClient.post("login", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                showProgress(false, "");
                try {
                    if(response.getInt("status") == 0) {
                        JSONObject userJsonObject = response.getJSONObject("body");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        userSessionManager.createUserLoginSession(phoneNumber, md5Password,
                                true, currentUser);
                        startMainActivity();
                    }
                    else if(response.getInt("status") == Constants.Login.NO_USER_PROFILE) {
                        JSONObject userJsonObject = response.getJSONObject("error");
                        User currentUser = gson.fromJson(userJsonObject.toString(), User.class);
                        userSessionManager.createUserLoginSession(phoneNumber, md5Password,
                                false, currentUser);
                        Log.i("JSON", "" + userSessionManager.getCurrentUser().getUserId());
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
                showProgress(false, "");
                new AlertDialog.Builder(LoginActivity.this).setMessage("网络连接出现问题").
                        setPositiveButton("确定", null).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
            }
        });
    }
}



