package me.qiang.android.chongai.util;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LiQiang on 17/1/15.
 */
public class Pet {
    public enum Gender {MALE, FEMALE}

    @SerializedName("pet_id")
    public int petId;

    @SerializedName("user_id")
    public int petUserId;

    @SerializedName("pet_name")
    public String petName;

    @SerializedName("sex")
    public Gender petGender;

    @SerializedName("info")
    public String petType;

    public Pet() {
        petName = "Lucky";
        petType = "金色寻回犬";
        petGender = Gender.FEMALE;
    }
}
