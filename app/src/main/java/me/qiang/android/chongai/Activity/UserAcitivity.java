package me.qiang.android.chongai.Activity;

import android.os.Bundle;

import me.qiang.android.chongai.Fragment.UserFragment;
import me.qiang.android.chongai.R;

public class UserAcitivity extends BaseToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_acitivity);

        showBackButton();
        enableBackButton();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new UserFragment())
                    .commit();
        }

    }

}
