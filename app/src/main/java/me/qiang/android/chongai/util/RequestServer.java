package me.qiang.android.chongai.util;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static void login(final String phoneNumber, final String md5Password, JsonHttpResponseHandler jsonHttpResponseHandler){
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

    public static void verifyPhoneNumber(String phoneNumber, JsonHttpResponseHandler jsonHttpResponseHandler) {
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("phone_number", phoneNumber);
        params.put("verify_sms", 0);
        HttpClient.post("login/verify", params, jsonHttpResponseHandler);
    }

}
