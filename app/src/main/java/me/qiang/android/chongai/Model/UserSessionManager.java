package me.qiang.android.chongai.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import me.qiang.android.chongai.Activity.LoginActivity;
import me.qiang.android.chongai.util.IMEI;

/**
 * Created by qiang on 1/7/2015.
 */
public class UserSessionManager {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    Gson gson = new Gson();

    private User currentUser = null;

    // Pet List Type
    Type PetListType = new TypeToken<ArrayList<Pet>>(){}.getType();

    // Sharedpref file name
    private static final String PREFER_NAME = "app_user_session";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "is_user_login";

    // Has User Entered Profile
    private static final String HAS_USER_PROFILE = "has_user_profile";

    // Phone number (make variable public to access from outside)
    public static final String KEY_PHONE_NUMBER = "phone_number";

    // Email address (make variable public to access from outside)
    public static final String KEY_MD5_PASSWORD = "md5_password";

    // User id (make variable public to access from outside)
    public static final String KEY_USER_PROFILE = "user_details";

    // Pet Info (make variable public to access from outside)
    public static final String KEY_PET_INFO = "pet_details";

    // Constructor
    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(String phoneNumber, String md5Password,
                                       boolean hasProfile, User user, ArrayList<Pet> petList){
        String currentUserDetails = gson.toJson(user, User.class);
        String petListDetails = gson.toJson(petList, PetListType);

        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing profile value as it is
        editor.putBoolean(HAS_USER_PROFILE, hasProfile);

        // Storing name in pref
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);

        // Storing email in pref
        editor.putString(KEY_MD5_PASSWORD, md5Password);

        // Storing profile in pref
        editor.putString(KEY_USER_PROFILE, currentUserDetails);

        editor.putString(KEY_PET_INFO, petListDetails);

        currentUser = user;

        // commit changes
        editor.commit();
    }

    public void updatePetInfo(ArrayList<Pet> petList) {
        String petListDetails = gson.toJson(petList, PetListType);

        editor.putString(KEY_PET_INFO, petListDetails);
        editor.commit();
    }

    public void addPet(Pet pet) {
        String petListDetails = pref.getString(KEY_PET_INFO, "");
        ArrayList<Pet> petList = gson.fromJson(petListDetails, PetListType);
        petList.add(pet);
        editor.putString(KEY_PET_INFO, gson.toJson(petList, PetListType));
        editor.commit();
    }

    public void updatePet(int petId, Pet newPet) {
        String petListDetails = pref.getString(KEY_PET_INFO, "");
        ArrayList<Pet> petList = gson.fromJson(petListDetails, PetListType);
        for(int i = 0; i < petList.size(); i++) {
            Pet pet = petList.get(i);
            if(pet.getPetId() == petId) {
                petList.set(i, newPet);
                break;
            }
        }
        editor.putString(KEY_PET_INFO, gson.toJson(petList, PetListType));
        editor.commit();
    }

    public Pet getPet(int petId) {
        String petListDetails = pref.getString(KEY_PET_INFO, "");
        ArrayList<Pet> petList = gson.fromJson(petListDetails, PetListType);
        for(int i = 0; i < petList.size(); i++) {
            Pet pet = petList.get(i);
            if(pet.getPetId() == petId)
                return pet;
        }
        return null;
    }

    public void updateUserProfile(boolean hasProfile, User user) {
        Gson gson = new Gson();
        String currentUserDetails = gson.toJson(user, User.class);

        // Storing profile value as it is
        editor.putBoolean(HAS_USER_PROFILE, hasProfile);

        // Storing profile in pref
        editor.putString(KEY_USER_PROFILE, currentUserDetails);

        currentUser = user;

        // commit changes
        editor.commit();
    }

    public User getCurrentUser() {
        Gson gson = new Gson();

        if(currentUser == null) {
            String userDetails = pref.getString(KEY_USER_PROFILE, "");
            currentUser = gson.fromJson(userDetails, User.class);
        }

        return currentUser;
    }

    public ArrayList<Pet> getPetList() {
        String petListDetails = pref.getString(KEY_PET_INFO, "");
        ArrayList<Pet> petList = gson.fromJson(petListDetails, PetListType);
        return petList;
    }

    public ArrayList<Pet> getBindedPetList() {
        String petListDetails = pref.getString(KEY_PET_INFO, "");
        ArrayList<Pet> petList = gson.fromJson(petListDetails, PetListType);
        ArrayList<Pet> bindedPetList = new ArrayList<>();
        for(int i = 0; i < petList.size(); i++) {
            Pet pet = petList.get(i);
            if(IMEI.isIMEIValid(pet.getImei())) {
                bindedPetList.add(pet);
            }
        }
        return bindedPetList;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

    // Check for profile
    public boolean hasProfile() {
        return pref.getBoolean(HAS_USER_PROFILE, false);
    }
}
