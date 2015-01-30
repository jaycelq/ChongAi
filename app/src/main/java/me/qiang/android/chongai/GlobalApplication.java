package me.qiang.android.chongai;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import me.qiang.android.chongai.Model.UserSessionManager;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class GlobalApplication extends Application {
    private static Context context;
    private static UserSessionManager userSessionManager;

    static final String TAG = "BDApplication";
    public static final int NEW_LOC_MSG = 0;
    private static GlobalApplication mBDApplicationInstance;
    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;
    public Handler mLocationHandler;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        if (Constants.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }

        super.onCreate();
        GlobalApplication.context = getApplicationContext();
        GlobalApplication.userSessionManager = new UserSessionManager(getAppContext());

        //注意：实现GetInstance()，此行代码易漏写
        mBDApplicationInstance = this;
        //百度地图
        //在使用百度地图SDK各组件之前初始化context信息，传入ApplicationContext。注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(this);
        //百度定位
        //LocationClient类必须在主线程中声明。需要Context类型的参数，且需全进程有效的context，推荐用getApplicationConext获取全进程有效的context
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        //注册监听函数，当没有注册监听函数时，无法发起网络请求
        mLocationClient.registerLocationListener(mMyLocationListener);

        initImageLoader(getApplicationContext());
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static Context getAppContext() {
        return GlobalApplication.context;
    }

    public static UserSessionManager getUserSessionManager() {return GlobalApplication.userSessionManager;}

    /**获取BDApplication实例
     * 注意：一定要在onCreate()获得自身的引用 mBDApplicationInstance = this;
     * @return
     */
    public static GlobalApplication GetInstance() {
        return mBDApplicationInstance;
    }
    /**设置mLoactionHandler全局参数
     * @param locationHandler
     */
    public void setLoactionHandler(Handler locationHandler){
        this.mLocationHandler = locationHandler;
    }
    /**实现位置回调监听
     * BDLocationListener接口需要实现方法：接收异步返回的定位结果，参数是BDLocation类型参数。
     * @author Eugene
     * @data 2014-12-24
     */
    public class MyLocationListener implements BDLocationListener {
        //定位请求回调函数
        //BDLocation类，封装了定位SDK的定位结果，在BDLocationListener的onReceive方法中获取。通过BDLocation用户可以获取error code，位置的坐标，精度半径等信息。
        @Override
        public void onReceiveLocation(BDLocation location) {
            //记录获取的位置信息
//			logLocationInfo(location);
            if (location == null) return;
            //发送新位置信息
            Message msg = new Message();
            msg.obj = location;
            msg.what = NEW_LOC_MSG;
            if(null != mLocationHandler) mLocationHandler.sendMessage(msg);
        }
    }
}