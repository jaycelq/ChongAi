package me.qiang.android.chongai.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Activity.AddProfileActivity;
import me.qiang.android.chongai.Activity.ImagePager;
import me.qiang.android.chongai.Activity.MainActivity;
import me.qiang.android.chongai.Activity.RegisterActivity;
import me.qiang.android.chongai.Constants;

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

    public static void startImagePagerActivity(Context context, List<String> imageUrls, int position){
        Intent intent = new Intent(context, ImagePager.class);
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.Extra.IMAGE_POSITION, position);
        bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
