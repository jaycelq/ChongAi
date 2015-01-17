package me.qiang.android.chongai.util;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 1/8/2015.
 */
public class User {
    public enum Gender {MALE, FEMALE}

    public String nickName;
    public String location;
    public String photo;
    public Gender gender;
    public String signature;
    public boolean isFollowed;

    //TODO: remove the defaults constructor
    public User() {
        nickName = "NANA";
        location = "上海市 长宁区";
        photo = "drawable://" + R.drawable.profile_photo_nana;
        gender = Gender.FEMALE;
        signature = "Love me~ Love my pet!~";
        isFollowed = false;
    }
}
