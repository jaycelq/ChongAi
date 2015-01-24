package me.qiang.android.chongai.util;

import android.content.Context;
import android.content.Intent;

import me.qiang.android.chongai.Activity.AddProfileActivity;
import me.qiang.android.chongai.Activity.MainActivity;
import me.qiang.android.chongai.Activity.RegisterActivity;

/**
 * Created by LiQiang on 24/1/15.
 */
public class ActivityTransition {
    public static void startRegisterActivity(Context context) {
        Intent registerIntent = new Intent(context, RegisterActivity.class);
        context.startActivity(registerIntent);
    }

    public static void startMainActivity(Context context) {
        Intent registerIntent = new Intent(context, MainActivity.class);
        context.startActivity(registerIntent);
    }

    public static void startAddProfileActivity(Context context) {
        Intent addProfileIntent = new Intent(context, AddProfileActivity.class);
        context.startActivity(addProfileIntent);
    }
}
