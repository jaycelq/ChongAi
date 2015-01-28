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

    @SerializedName("pet_user")
    public User petUser;

    @SerializedName("pet_name")
    public String petName;

    @SerializedName("pet_photo")
    public String petPhoto;

    @SerializedName("sex")
    public Gender petGender;

    @SerializedName("pet_hobby")
    public String petHobby;

    @SerializedName("pet_age")
    public int petAge;

    @SerializedName("pet_skill")
    public String petSkill;

    @SerializedName("info")
    public String petType;

    @SerializedName("imei")
    public String petImei;

    public Pet() {
        petName = "Lucky";
        petType = "金色寻回犬";
        petGender = Gender.FEMALE;
    }

    public Pet(int petId, int petUserId, User petUser, String petName, String petPhoto,
               Gender petGender, String petType, String petHobby,
               int petAge, String petSkill) {
        this.petId = petId;
        this.petUserId = petUserId;
        this.petUser = petUser;
        this.petName = petName;
        this.petPhoto = petPhoto;
        this.petGender = petGender;
        this.petType = petType;
        this.petHobby = petHobby;
        this.petAge = petAge;
        this.petSkill = petSkill;
    }

    public int getPetId() {
        return petId;
    }

    public int getPetUserId() {
        return petUserId;
    }

    public User getPetOwner() { return petUser;}

    public String getPetName() {
        return petName;
    }

    public String getPetPhoto() {
        return petPhoto;
    }

    public Gender getPetGender() {
        return petGender;
    }

    public String getPetHobby() {
        return petHobby;
    }

    public int getPetAge() {
        return petAge;
    }

    public String getPetSkill() {
        return petSkill;
    }

    public String getPetType() {
        return petType;
    }

    public String getImei() {
        return petImei;
    }

}
