package me.qiang.android.chongai.baidumap;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

/**JsonAty extends Activity
 * json操作（创建、解析）测试；
 * @author Eugene
 * @data 2015-1-25
 */
public class JsonProcess{
	static final String TAG = "JsonProcess";
	
	static final String URL = "http://128.199.226.246/location/getLatestLocation";
	
	public static final int RECEIVE_JSON_STRING = 101;
	
	private static Handler handler;
	
	public static void SetHandler(Handler handler){
		JsonProcess.handler = handler;
	}
	
	/**访问服务器端获取json数据
	 */
	public static void RequestForJson(Context context) {   
        RequestParams params = new RequestParams();
        params.put("imei", "862950025623748");
        Toast.makeText(context, "Sending: " + createJson().toString(), Toast.LENGTH_SHORT).show();
        AsyncHttpClient client = new AsyncHttpClient();//创建客户端对象
        client.post(URL, new JsonHttpResponseHandler() {
            @Override//返回JSONArray对象 | JSONObject对象  
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {  
                super.onSuccess(statusCode, headers, response);  
                if (statusCode == 200) {
                    parseJson(response); 
                }  
            }  
  
        });  
    } 
	
	/**访问服务器端获取String
	 */
	public static void RequestForString(Context context, String imei) {   
        RequestParams params = new RequestParams();
        params.put("imei", imei);
        SyncHttpClient client = new SyncHttpClient();//创建客户端对象
        client.post(URL, params, new TextHttpResponseHandler() {  
        	@Override
        	public void onSuccess(int statusCode, Header[] arg1, String content) {
                if (statusCode == 200) {
                    try {
                    	if (handler != null) {
                    		Message msg = new Message();
                    		msg.what = RECEIVE_JSON_STRING;
                    		msg.obj = content;
            				handler.sendMessage(msg);
            			}
						parseJson(new JSONObject(content));//Test
					} catch (JSONException e) {
						e.printStackTrace();
					} 
                }  
        	}
			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
				Log.i(TAG, "Exception");
			}
        });  
    }
	
	/**创建待发送json；
	 * json示例：{"imei":"862950025623748"}；
	 * @return
	 */
	private static JSONObject createJson(){
		JSONObject root = new JSONObject();
		try {
			root.put("imei", "862950025623748");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return root;
	}
	
	/**解析接收到的json
	 * 格式示例：
	 * {
	    "status": 0,
	    "body": {
	        "id": "326",
	        "imei": "862950025623748",
	        "time": "2015-01-25 22:20:20.0",
	        "battery": "37",
	        "location_type": "1",
	        "latitude": "31.027273",
	        "longitude": "121.437614"
	    }
	   }
	 * @param root
	 */
	private static void parseJson(JSONObject root){
		try {
			Log.i(TAG, "status = " + root.getInt("status"));
			JSONObject bodyRoot = root.getJSONObject("body");
			Log.i(TAG, "body = " + bodyRoot);
			Log.i(TAG, "id = " + bodyRoot.getString("id"));
			Log.i(TAG, "imei = " + bodyRoot.getString("imei"));
			Log.i(TAG, "time = " + bodyRoot.getString("time"));
			Log.i(TAG, "battery = " + bodyRoot.getString("battery"));
			Log.i(TAG, "location_type = " + bodyRoot.getString("location_type"));
			Log.i(TAG, "latitude = " + bodyRoot.getString("latitude"));
			Log.i(TAG, "longitude = " + bodyRoot.getString("longitude"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static LatLng ParseJsonForLocation(JSONObject root){
		LatLng location = null;
		try {
			JSONObject bodyRoot = root.getJSONObject("body");
			String lat, lng;
			lat = bodyRoot.getString("latitude");
			lng = bodyRoot.getString("longitude");
			if (lat == "" || lng == "") {
				return null;
			}
			location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return location;
	}
	
	public static int ParseJsonForBattery(JSONObject root){
		int battery = 0;
		try {
			JSONObject bodyRoot = root.getJSONObject("body");
			battery = Integer.valueOf(bodyRoot.getString("battery"));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return battery;
	}
	
	public static String ParseJsonForPetName(JSONObject root){
		String name = null;
		try {
			JSONObject bodyRoot = root.getJSONObject("body");
			name = bodyRoot.getString("id");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return name;
	}
	
}
