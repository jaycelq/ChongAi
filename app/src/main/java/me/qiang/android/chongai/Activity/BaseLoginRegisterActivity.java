package me.qiang.android.chongai.Activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import me.qiang.android.chongai.util.MobileNumber;

/**
 * A login screen that offers login via phone/password.
 */
public class BaseLoginRegisterActivity extends BaseToolbarActivity {

    // UI references.
    protected EditText mPhoneNumberView;
    protected EditText mPasswordView;
    protected View mLoginFormView;
    protected ProgressDialog barProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

    }


    protected boolean isPhoneNumberValid(String phone) {
        return MobileNumber.isMobileNO(phone);
    }

    protected boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6 && password.length() <= 18;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, String message) {
        if(show) {
            barProgressDialog.setMessage(message);
            barProgressDialog.show();
        }
        else {
            barProgressDialog.dismiss();
        }
    }
}



