package me.qiang.android.chongai.util;

import com.google.gson.annotations.SerializedName;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 1/8/2015.
 */
public class User {
    public enum Gender {MALE, FEMALE}

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
        userId = 0;
        nickName = "NANA";
        location = "上海市 长宁区";
        photo = "drawable://" + R.drawable.profile_photo_nana;
        gender = Gender.FEMALE;
        signature = "Love me~ Love my pet!~";
        isFollowed = false;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public int getUserId() {
        return userId;
    }
}
