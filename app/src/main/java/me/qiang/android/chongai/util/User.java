package me.qiang.android.chongai.util;

import com.google.gson.annotations.SerializedName;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 1/8/2015.
 */
public class User {
    public enum Gender {
        @SerializedName("0")
        MALE,
        @SerializedName("1")
        FEMALE
    }

    @SerializedName("user_id")
    public int userId;

    @SerializedName("user_name")
    public String nickName;

    @SerializedName("user_location")
    public String location;

    @SerializedName("sex")
    public Gender gender;

    @SerializedName("avatar")
    public String photo;

    @SerializedName("intro")
    public String signature;

    @SerializedName("if_follow")
    public boolean isFollowed;

    //TODO: remove the defaults constructor
    public User() {
        userId = 4;
        nickName = "李强";
        location = "上海市 闵行区";
        photo = "drawable://" + R.drawable.profile_photo_nana;
        gender = Gender.MALE;
        signature = "Love me~ Love my pet!~";
        isFollowed = false;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {return nickName;}

    public String getUserLocation() {return location;}

    public String getUserPhoto() {return photo;}

    public Gender getUserGender() {
        return gender;
    }
}
