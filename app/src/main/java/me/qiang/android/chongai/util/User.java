package me.qiang.android.chongai.util;

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
}
