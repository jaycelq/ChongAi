package me.qiang.android.chongai.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.qiang.android.chongai.Activity.AddPetActivity;
import me.qiang.android.chongai.Activity.AddProfileActivity;
import me.qiang.android.chongai.Activity.CustomAlbum;
import me.qiang.android.chongai.Activity.ImagePager;
import me.qiang.android.chongai.Activity.MainActivity;
import me.qiang.android.chongai.Activity.PetActivity;
import me.qiang.android.chongai.Activity.RegisterActivity;
import me.qiang.android.chongai.Activity.UserAcitivity;
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

    public static void startAddProfileActivity(Fragment fragment, int userId) {
        Intent addProfileIntent = new Intent(fragment.getActivity(), AddProfileActivity.class);
        addProfileIntent.putExtra(Constants.User.USER_ID, userId);
        fragment.startActivityForResult(addProfileIntent, Constants.User.UPDATE_USER);
    }

    public static void startImagePagerActivity(Context context, List<String> imageUrls, int position){
        Intent intent = new Intent(context, ImagePager.class);
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.Extra.IMAGE_POSITION, position);
        bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void startUserActivity(Context context, int userId) {
        Intent userIntent = new Intent(context, UserAcitivity.class);
        userIntent.putExtra(Constants.User.USER_ID, userId);
        context.startActivity(userIntent);
    }

    public static void startImagePagerActivity(Context context, String imageUrl){
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add(imageUrl);
        Intent intent = new Intent(context, ImagePager.class);
        Bundle bundle=new Bundle();
        bundle.putInt(Constants.Extra.IMAGE_POSITION, 0);
        bundle.putStringArrayList(Constants.Extra.IMAGE_TO_SHOW, (ArrayList)imageUrls);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void startAddPetActivity(Fragment fr) {
        Intent addPetIntent = new Intent(fr.getActivity(), AddPetActivity.class);
        fr.startActivityForResult(addPetIntent, Constants.Pet.ADD_PET);
    }

    public static void startAddPetActivity(Activity activity) {
        Intent addPetIntent = new Intent(activity, AddPetActivity.class);
        activity.startActivityForResult(addPetIntent, Constants.Pet.ADD_PET);
    }

    public static void startAddPetActivity(Activity activity, int petId) {
        Intent addPetIntent = new Intent(activity, AddPetActivity.class);
        addPetIntent.putExtra(Constants.Pet.PET_ID, petId);
        activity.startActivityForResult(addPetIntent, Constants.Pet.ADD_PET);
    }


    public static void pickImageFromAlbum(Fragment fr) {
        Intent intent = new Intent(fr.getActivity(), CustomAlbum.class);
        fr.startActivityForResult(intent, Constants.Image.PICK_IMAGE);
    }

    public static void pickImageFromAlbum(Activity activity) {
        Intent intent = new Intent(activity, CustomAlbum.class);
        activity.startActivityForResult(intent, Constants.Image.PICK_IMAGE);
    }

    public static void takePhoto(Fragment fr, File photoFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(fr.getActivity().getPackageManager()) != null) {
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                fr.startActivityForResult(takePictureIntent, Constants.Image.TAKE_PHOTO);
            }
        }
    }

    public static void startPetActivity(Fragment fr, int petId) {
        Intent petIntent = new Intent(fr.getActivity(), PetActivity.class);
        petIntent.putExtra(Constants.Pet.PET_ID, petId);
        fr.startActivity(petIntent);
    }

}
