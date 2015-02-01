package me.qiang.android.chongai.Model;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("post_num")
    public int post_num;

    @SerializedName("fans_num")
    public int fans_num;

    @SerializedName("follow_num")
    public int follow_num;


    public User(String userName, Gender userGender, String userLocation,
                String userSignature, String userLocalPhoto) {
        nickName = userName;
        location = userLocation;
        photo = userLocalPhoto;
        gender = userGender;
        signature = userSignature;
        isFollowed = false;
    }

    public User(int userId, String userName, Gender userGender, String userLocation,
                String userSignature, String userLocalPhoto) {
        this.userId = userId;
        nickName = userName;
        location = userLocation;
        photo = userLocalPhoto;
        gender = userGender;
        signature = userSignature;
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
