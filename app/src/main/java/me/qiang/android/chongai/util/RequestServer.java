package me.qiang.android.chongai.util;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.User;

/**
 * Created by LiQiang on 24/1/15.
 */
public class RequestServer {

    public static void follow(int userId, JsonHttpResponseHandler jsonHttpResponseHandler){
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("act", "follow");
        params.put("userId", userId);
        HttpClient.post("login", params, jsonHttpResponseHandler);
    }

    public static void login(final String phoneNumber, final String md5Password,
                             JsonHttpResponseHandler jsonHttpResponseHandler){
        JSONObject userInfo = new JSONObject();
        try {
            userInfo.put("phone_number", phoneNumber);
            userInfo.put("password", md5Password);
        } catch (JSONException ex) {
            // 键为null或使用json不支持的数字格式(NaN, infinities)
            throw new RuntimeException(ex);
        }
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("act", "login");
        params.put("userinfo", userInfo);
        HttpClient.post("login", params, jsonHttpResponseHandler);
    }

    public static void register(String phoneNumber, String password, String verifyCode,
                         JsonHttpResponseHandler jsonHttpResponseHandler){
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("phone_number", phoneNumber);
        params.put("password", MD5.md5(password));
        params.put("verify_sms", verifyCode);
        HttpClient.post("login/verify", params, jsonHttpResponseHandler);
    }

    public static void verifyPhoneNumber(String phoneNumber,
                                         JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("phone_number", phoneNumber);
        params.put("verify_sms", 0);
        HttpClient.post("login/verify", params, jsonHttpResponseHandler);
    }

    public static void like(int stateId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("post_id", stateId);
        HttpClient.post("like", params, jsonHttpResponseHandler);
    }

    public static void unlike(int stateId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("post_id", stateId);
        HttpClient.post("like/unlike", params, jsonHttpResponseHandler);
    }

    public static void getStates(int stateId, JsonHttpResponseHandler jsonHttpResponseHandler){
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("post_id", stateId);
        HttpClient.post("post/getPosts", params, jsonHttpResponseHandler);
    }

    public static void saveProfile(User user, InputStream avatarPic, String avatarUrl,
                             JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();

        final Gson gson = new Gson();
        String userInfo = gson.toJson(user, User.class);

        params.put("avatar_pic", avatarPic, avatarUrl);
        params.put("user", userInfo);
        HttpClient.post("user/newProfile", params, jsonHttpResponseHandler);
    }

    public static void addPet(Pet pet,InputStream avatarPic, String avatarUrl,
                              JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();

        final Gson gson = new Gson();
        String petInfo = gson.toJson(pet, Pet.class);
        params.put("pet_avatar", avatarPic, avatarUrl);
        params.put("pet_info", petInfo);
        HttpClient.post("pet/add", params, jsonHttpResponseHandler);
    }


    public static void sendComment(int stateId, final String content, int userId,
                             JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("post_id", stateId);
        params.put("content", content);
        params.put("user_id", userId);

        HttpClient.post("comment", params, jsonHttpResponseHandler);
    }

    public static void getComments(int commentId, int stateId,
                            JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("comment_id", commentId);
        params.put("post_id", stateId);

        HttpClient.post("comment/getComment", params, jsonHttpResponseHandler);
    }

    public static void sendState(String content, String photoUrl, int petId, double locationX,
                                 double locationY, InputStream photoFileStream,
                                 JsonHttpResponseHandler jsonHttpResponseHandler){
        RequestParams params = new RequestParams();
        params.put("photo", photoFileStream, photoUrl);
        JSONObject stateInfo = new JSONObject();
        try {
            stateInfo.put("content", content);
            stateInfo.put("pet_id", petId);
            JSONObject location = new JSONObject();
            location.put("location_x", locationX);
            location.put("location_y", locationY);
            stateInfo.put("location", location);

        } catch (JSONException ex) {
            // 键为null或使用json不支持的数字格式(NaN, infinities)
            throw new RuntimeException(ex);
        }
        params.put("content", stateInfo);
        HttpClient.post("post/index", params, jsonHttpResponseHandler);
    }

    public static void getUserInfo(int userId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("user_id", userId);

        HttpClient.post("/user", params, jsonHttpResponseHandler);
    }

}
