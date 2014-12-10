package me.qiang.android.chongai.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import me.qiang.android.chongai.GlobalApplication;

/**
 * Created by qiang on 12/10/2014.
 */
public class HttpClient {
    private static final String BASE_URL="http://192.168.1.234/";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static PersistentCookieStore myCookieStore = new PersistentCookieStore(GlobalApplication.getAppContext());

    static {
        client.setCookieStore(myCookieStore);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
