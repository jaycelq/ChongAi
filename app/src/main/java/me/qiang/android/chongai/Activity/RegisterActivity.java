package me.qiang.android.chongai.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.markushi.ui.CircleButton;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.MD5;
import me.qiang.android.chongai.util.MobileNumber;

//TODO: deal with resend verify code in UI
public class RegisterActivity extends BaseLoginRegisterActivity implements TextWatcher{

    private EditText mPasswordConfirmView;
    private EditText verifyCodeView;
    private TextView mRegisterResult;
    private Button sendVerifyCodeButton;
    private CircleButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        setToolbarTile("注册");

        // Set up the login form.
        mPhoneNumberView = (EditText) findViewById(R.id.phone);
        mPhoneNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(MobileNumber.isMobileNO(s.toString())) {
                    sendVerifyCodeButton.setClickable(true);
                }
            }
        });
        mPhoneNumberView.addTextChangedListener(this);

        mRegisterResult = (TextView) findViewById(R.id.register_result);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.addTextChangedListener(this);

        mPasswordConfirmView = (EditText) findViewById(R.id.password_confirm);
        mPasswordConfirmView.addTextChangedListener(this);

        verifyCodeView = (EditText) findViewById(R.id.verify_code);
        verifyCodeView.addTextChangedListener(this);

        sendVerifyCodeButton = (Button) findViewById(R.id.send_verify_code);
        sendVerifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mPhoneNumberView.getText().toString();
                v.setClickable(false);
                verifyPhoneNumber(phoneNumber);
                new CountDownTimer(60000, 1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        sendVerifyCodeButton.setText(millisUntilFinished/1000 + "秒后重试");
                    }

                    @Override
                    public void onFinish() {
                        sendVerifyCodeButton.setText("发送验证码");
                        sendVerifyCodeButton.setClickable(true);
                    }
                }.start();
            }
        });
        sendVerifyCodeButton.setClickable(false);

        registerButton = (CircleButton) findViewById(R.id.email_sign_in_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        registerButton.setClickable(false);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        // Store values at the time of the login attempt.
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();
        String verifyCode = verifyCodeView.getText().toString();

        View focusView = isInputValid();

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true, "正在注册...");
            register(phoneNumber, password, verifyCode);
        }
    }

    private boolean isVerifyCodeValid(String verifyCode){
        Pattern p = Pattern.compile("^\\d{6}$");
        Matcher m = p.matcher(verifyCode);
        return m.matches();
    }

    private View isInputValid() {
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();
        String verifyCode = verifyCodeView.getText().toString();

        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
            focusView = mPasswordView;


        // Check if password confirm is equal to password
        if(!TextUtils.equals(password, passwordConfirm))
            focusView = mPasswordConfirmView;



        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber))
            focusView = mPhoneNumberView;
        else if (!isPhoneNumberValid(phoneNumber))
            focusView = mPhoneNumberView;

        if(TextUtils.isEmpty(verifyCode) || !isVerifyCodeValid(verifyCode))
            focusView = verifyCodeView;

        return focusView;
    }


    public void register(String phoneNumber, String password, String verifyCode){
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("phone_number", phoneNumber);
        params.put("password", MD5.md5(password));
        params.put("verify_sms", verifyCode);
        HttpClient.post("login/verify", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
                try {
                    if(response.getInt("status") == 0) {
                        mRegisterResult.setVisibility(View.VISIBLE);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                        }, 800);
                    }
                    showProgress(false, "");
                } catch (JSONException ex)
                {}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("JSON", "JSON FAIL");
                showProgress(false, "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("JSON", responseString);
            }
        });
    }

    public void verifyPhoneNumber(String phoneNumber) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("phone_number", phoneNumber);
        params.put("verify_sms", 0);
        HttpClient.post("login/verify", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSON", response.toString());
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
        if(isInputValid() == null)
            registerButton.setClickable(true);
        else
            registerButton.setClickable(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
