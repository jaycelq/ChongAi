package me.qiang.android.chongai.util;

/**
 * Created by LiQiang on 17/1/15.
 */
public class Pet {
    public enum Gender {MALE, FEMALE}
    public String petName;
    public String petType;
    public Gender petGender;

    public Pet() {
        petName = "Lucky";
        petType = "金色寻回犬";
        petGender = Gender.FEMALE;
    }
}
