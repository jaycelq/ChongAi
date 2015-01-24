package me.qiang.android.chongai.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.EditText;

import me.qiang.android.chongai.util.MobileNumber;

/**
 * A login screen that offers login via phone/password.
 */
public class BaseLoginRegisterActivity extends BaseToolbarActivity {

    // UI references.
    protected EditText mPhoneNumberView;
    protected EditText mPasswordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barProgressDialog = new ProgressDialog(this);

    }


    protected boolean isPhoneNumberValid(String phone) {
        return MobileNumber.isMobileNO(phone);
    }

    protected boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6 && password.length() <= 18;
    }

}



