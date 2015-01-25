package me.qiang.android.chongai.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import me.qiang.android.chongai.Activity.LoginActivity;

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

    private User currentUser = null;

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

    // Constructor
    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(String phoneNumber, String md5Password,
                                       boolean hasProfile, User user){
        Gson gson = new Gson();
        String currentUserDetails = gson.toJson(user, User.class);

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

        currentUser = user;

        // commit changes
        editor.commit();
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
