package me.qiang.android.chongai.Model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LiQiang on 17/1/15.
 */
public class Pet {
    public enum Gender {
        @SerializedName("0")
        MALE,
        @SerializedName("1")
        FEMALE
    }

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

    public String getPetName() {
        return petName;
    }

    public Gender getPetGender() {
        return petGender;
    }

    public String getPetType() {
        return petType;
    }
}
