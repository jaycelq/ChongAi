package me.qiang.android.chongai.baidumap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.R;

public class BDMapFragment extends Fragment implements View.OnClickListener{
	static final String TAG = "BDMapFragment";
	
	public static final String IMEI = "862950025623748";

    GlobalApplication mApplication;//全局应用
	
	MapView mMapView;//百度地图视图控件
	BaiduMap mMapController;//百度地图控制器
	
	LocationClient mLocationClient;//定位服务的客户端
	LocationClientOption locationOption;//定位参数
	BDLocation mCurLocation;//当前位置
	
	View view = null;
	EditText etRadius;
	TextView tvPetName, tvDistance, tvBattery;
	ImageButton minusBtn, plusBtn;
	Button locateBtn;
	MyToggleButton toggleBtnPetGPS, toggleBtnZhalan;
	ImageView ivPetName;
	Vibrator mVibrator;//震动组件
	
	LatLng petLocation = null;//宠物坐标
	int radius = 0;//检测半径
	boolean isOutBound = false;//是否离开限定区域
	int mSingleChoiceID = -1;//当前选定宠物ID
	String[] petNames = {"菲比", "莉莉", "金毛"};//宠物名列表（测试）
	
	Timer timer = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_bdmap, container, false);

        /**全局Application初始化，尽量在setContentView之前初始化 */
        mApplication = GlobalApplication.GetInstance();
        //设置LoactionHandler
        mApplication.setLoactionHandler(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == GlobalApplication.NEW_LOC_MSG){
                    mCurLocation = (BDLocation) msg.obj;
                    mMapController.clear();
                    locatingOnReceiveLocation();
                    drawCircleOnReceiveLocation(radius);
                    drawPetIcon(petLocation);
                }
            }
        });

        /**UI初始化*/
        tvPetName = (TextView) view.findViewById(R.id.tv_petname);
        tvDistance = (TextView) view.findViewById(R.id.tv_distance);
        tvBattery = (TextView) view.findViewById(R.id.tv_battery);
        etRadius = (EditText) view.findViewById(R.id.et_radius);
        radius = getInt(etRadius.getText().toString());//获取默认检测半径值
        etRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                radius = getInt(s.toString());
                if (radius != -1) {
                    mMapController.clear();
                    drawCircleOnReceiveLocation(radius);
                    drawPetIcon(petLocation);
                }
            }
        });
        minusBtn = (ImageButton) view.findViewById(R.id.ibtn_minus);
        plusBtn = (ImageButton) view.findViewById(R.id.ibtn_plus);
        locateBtn = (Button) view.findViewById(R.id.btn_locate);
        ivPetName = (ImageView) view.findViewById(R.id.iv_petname);
        minusBtn.setOnClickListener(this);
        plusBtn.setOnClickListener(this);
        locateBtn.setOnClickListener(this);
        ivPetName.setOnClickListener(this);
        toggleBtnPetGPS = (MyToggleButton) view.findViewById(R.id.togglebtn_gps);
        toggleBtnZhalan = (MyToggleButton) view.findViewById(R.id.togglebtn_zhalan);
        toggleBtnPetGPS.setHandler(new Handler(){//宠物GPS开关
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MyToggleButton.TOGGLE_CHANGE) {
                    if (locationOption != null) {
                        if (toggleBtnPetGPS.getToggleState()) {//若开启宠物，则立即发送一次定位请求
                            JsonProcess.RequestForString(getActivity(), IMEI);
                        }
                        Toast.makeText(getActivity(),
                                "宠物GPS: " + (toggleBtnPetGPS.getToggleState()?"On":"Off"), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        toggleBtnZhalan.setHandler(new Handler(){//栅栏开关
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MyToggleButton.TOGGLE_CHANGE) {
                    mMapController.clear();
                    drawCircleOnReceiveLocation(radius);//内含getToggleState()判断
                    drawPetIcon(petLocation);//内含getToggleState()判断
                    Toast.makeText(getActivity(),
                            "栅栏: " + (toggleBtnZhalan.getToggleState()?"On":"Off"), Toast.LENGTH_SHORT).show();
                }
            }
        });
        //获取震动服务
        mVibrator =(Vibrator)getActivity().getSystemService(Service.VIBRATOR_SERVICE);

        mMapView = (MapView) view.findViewById(R.id.mapView);//百度地图视图控件

		return view;
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		/**地图视图、控制器初始化 */
		mMapController = mMapView.getMap();//百度地图控制器
		initMapStatus();
		//开启定位图层
		mMapController.setMyLocationEnabled(true);//setMyLocationEnabled设置是否允许定位图层

		/**定位初始化 */
		//定位服务的客户端，从BDApplication获取全局LocationClient
		mLocationClient = ((GlobalApplication)getActivity().getApplication()).mLocationClient;
		//定义定位参数
		locationOption = new LocationClientOption();
		initLocationOptions(locationOption);//设置定位参数
		mLocationClient.setLocOption(locationOption);
		//启动定位sdk
		mLocationClient.start();

		/**设置json处理的handler */
		JsonProcess.SetHandler(new Handler(){
			@Override
			public void handleMessage(Message msg) {//当获取到服务器端的坐标时，更新跟踪位置
				try {
					if (msg.what == JsonProcess.RECEIVE_JSON_STRING) {
						String result = (String)msg.obj;
						JSONObject jsonObject = new JSONObject(result);
						Double distance = -1d;
						//从json获取位置坐标
						petLocation = JsonProcess.ParseJsonForLocation(jsonObject);
						//宠物图标更新
						if (toggleBtnPetGPS.getToggleState()) {
							if (petLocation == null) {
								Toast.makeText(getActivity(), "服务器忙，稍后将重新获取宠物位置", Toast.LENGTH_LONG).show();
							} else {
								mMapController.clear();
								drawPetIcon(petLocation);//绘制宠物头像
								drawCircleOnReceiveLocation(radius);
								//更新人宠距离
								distance = updateDistance();
								//视图移动到宠物位置
//								mMapController.setMapStatus(MapStatusUpdateFactory.newLatLng(petLocation));
								Toast.makeText(getActivity(), "宠物坐标纬度：" + petLocation.latitude + 
										"；经度：" + petLocation.longitude, Toast.LENGTH_LONG).show();
							}
						}
						//栅栏越界判断
						if (toggleBtnZhalan.getToggleState() && distance != -1d) {
							isOutBound = distance > (double)radius;
							if (isOutBound) {//超出限定区域，弹出对话框并震动
								showPetOutBoundAlertDialog();
								mVibrator.vibrate(1000);//1s
							}
						}
						//从json获取电池信息
						int battery = JsonProcess.ParseJsonForBattery(jsonObject);
						if (battery != -1) {
							tvBattery.setText(battery + "%");
						}
						//从json获取宠物名
						String petName = JsonProcess.ParseJsonForPetName(jsonObject);
						if (petName != null) {
							tvPetName.setText(petName);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		/**开启POST请求获取json数据（转移到OnStart） */
//		timer = new Timer();
//		timer.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//				JsonProcess.RequestForString(getActivity());
//			}
//		}, 3 * 1000, 20 * 1000);//每20s发送一次请求
		
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_locate) {
			//请求定位，异步返回，结果在locationListener中获取
			mLocationClient.requestLocation();
			//若开启宠物GPS，则立即发送一次宠物定位请求
			if (toggleBtnPetGPS.getToggleState()) {
				JsonProcess.RequestForString(getActivity(), IMEI);
			}
		} else if (v.getId() == R.id.ibtn_minus) {
			etRadius.setText((radius - 1) + "");
		} else if (v.getId() == R.id.ibtn_plus) {
			etRadius.setText((radius + 1) + "");
		} else if (v.getId() == R.id.iv_petname) {//弹出宠物列表选择框
			showPetListDialog();
		}
	}
	
	/**从字符串提取整（格式判断与异常处理）
	 * @param s
	 * @return
	 */
	private int getInt(String s){
		try {
			if (s.length() > 0) {
				return Integer.valueOf(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "请输入距离", Toast.LENGTH_SHORT).show();
			return -1;
		}
		Toast.makeText(getActivity(), "请输入距离", Toast.LENGTH_SHORT).show();
		return -1;
	}
	
	/**更新人宠距离
	 * @return 失败将返回-1
	 */
	private double updateDistance(){
		double distance = 0d;
        if(mCurLocation == null){
            Toast.makeText(getActivity(), "无法获取当前位置，稍后重试", Toast.LENGTH_SHORT).show();
            return -1d;
        }
		if (petLocation != null) {
			LatLng curPoint = new LatLng(mCurLocation.getLatitude(), mCurLocation.getLongitude());
			//距离计算（单位：米），错误时返回-1；DistanceUtil为测距工具
			distance = DistanceUtil.getDistance(petLocation, curPoint);//传入参数为百度经纬度坐标
			if (distance != -1d) {
				tvDistance.setText(Math.round(distance) + "m");
			}else {
				Toast.makeText(getActivity(), "距离计算失败", Toast.LENGTH_SHORT).show();
				return -1d;
			}
			
		}
		return distance;
	}
	
	/**创建并显示宠物越界提醒对话框
	 */
	private void showPetOutBoundAlertDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setIcon(R.drawable.icon);  
        builder.setTitle("您的宠物已跑出栅栏！");  
        builder.setPositiveButton("开启精准定位", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int whichButton) {  
//                new AlertDialog.Builder(getActivity()).setMessage("开启精准定位").show();
            	Toast.makeText(getActivity(), "开启精准定位", Toast.LENGTH_SHORT).show();
            }  
        });  
        builder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int whichButton) {  
//            	new AlertDialog.Builder(getActivity()).setMessage("知道了").show();
                toggleBtnZhalan.turnoff();
                mMapController.clear();
                drawCircleOnReceiveLocation(radius);
                drawPetIcon(petLocation);
            	Toast.makeText(getActivity(), "知道了", Toast.LENGTH_SHORT).show();
            }  
        });  
        builder.create().show();  
	}
	
	/**创建并显示宠物列表选择对话框
	 */
	private void showPetListDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());   
		 
//		builder.setIcon(R.drawable.pet_median);  
	    builder.setTitle("请选择您的宠物");  
	    builder.setSingleChoiceItems(petNames, 0, new DialogInterface.OnClickListener() {  
	        public void onClick(DialogInterface dialog, int whichButton) {  
                mSingleChoiceID = whichButton;
	        }  
	    });  
	    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	        public void onClick(DialogInterface dialog, int whichButton) {  
	            if(mSingleChoiceID != -1) {
	            	tvPetName.setText(petNames[mSingleChoiceID]);
	            	Toast.makeText(getActivity(), "切换到宠物：" + petNames[mSingleChoiceID], Toast.LENGTH_SHORT).show();
	            }  
	        }  
	    });  
	    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
	        public void onClick(DialogInterface dialog, int whichButton) {  
	        }  
	    });  
	    builder.create().show();
	}
	
	/**设置地图状态
	 */
	private void initMapStatus() {
		mMapView.showZoomControls(false);//取消缩放控件
		
		//MapStatusUpdateFactory生成地图状态将要发生的变化
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);//设置缩放级别
		//setMapStatus更新地图状态
		mMapController.setMapStatus(msu);
		
		//MapStatus定义地图状态，MapStatus.Builder为地图状态构造器
		MapStatus mapStatus = new MapStatus.Builder(mMapController.getMapStatus()).rotate(0).build();
		MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mapStatus);//newMapStatus设置地图新状态
		//animateMapStatus以动画方式更新地图状态，动画耗时 300 ms
		mMapController.animateMapStatus(u);
	}
	
	/**当获取新的位置信息时，进行相关处理
	 */
	private void locatingOnReceiveLocation() {
        if(mCurLocation == null){
            Toast.makeText(getActivity(), "无法获取当前位置，稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }
		//MyLocationData定位数据，MyLocationData.Builder定位数据建造器
		MyLocationData locData = new MyLocationData.Builder()
			.accuracy(0)//设置定位数据的精度信息，单位：米（设置为0则不显示精度覆盖图层）
//			.direction(100)//设置定位数据的方向信息，顺时针0-360
			.latitude(mCurLocation.getLatitude())
			.longitude(mCurLocation.getLongitude()).build();
		mMapController.setMyLocationData(locData);
		//MyLocationConfiguration配置定位图层显示方式
		mMapController.setMyLocationConfigeration(
				new MyLocationConfiguration(
					MyLocationConfiguration.LocationMode.NORMAL, //定位图层显示方式
					false, //是否允许显示方向信息
					BitmapDescriptorFactory.fromResource(R.drawable.me_small)));//用户自定义定位图标
		//以动画方式更新地图状态到当前位置
		LatLng ll = new LatLng(mCurLocation.getLatitude(), mCurLocation.getLongitude());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);//newLatLng设置地图新中心点
		mMapController.animateMapStatus(u);//animateMapStatus以动画方式更新地图状态，动画耗时 300 ms
	}
	
	/**绘制以当前位置为圆心的圆
	 */
	private void drawCircleOnReceiveLocation(int radius) {
        if(mCurLocation == null){
            Toast.makeText(getActivity(), "无法获取当前位置，稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }
		if (toggleBtnZhalan.getToggleState()) {//只有开启栅栏才绘制
			LatLng llCircle = new LatLng(mCurLocation.getLatitude(), mCurLocation.getLongitude());
			//CircleOptions创建圆的选项
			OverlayOptions circleOverlayOptions = new CircleOptions()
				.fillColor(0x7F89aee5)//设置圆填充颜色
				.center(llCircle)//设置圆心坐标
				.stroke(new Stroke(2, 0xDD89aee5))//设置圆边框信息，边框的宽度默认为 5（像素）
				.radius(radius);//设置圆半径（米）
			mMapController.addOverlay(circleOverlayOptions);//向地图添加一个 Overlay
		}
	}
	
	/**在给定坐标绘制宠物头像
	 * @param petLocation
	 */
	private void drawPetIcon(LatLng petLocation) {
		if (toggleBtnPetGPS.getToggleState()) {//只有开启宠物GPS才绘制
			if (petLocation != null) {
				mMapController.addOverlay(
						new MarkerOptions()//MarkerOptions标记覆盖物
						.position(petLocation)//设置 marker 覆盖物的位置坐标
						//设置 Marker覆盖物的图标，相同图案的 icon的 marker最好使用同一个 BitmapDescriptor对象以节省内存空间。
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_median)));
			}
		}
	}
	
	/**绘制模拟的点
	 */
//	private void drawDotOnReceiveLocation(double lat, double lng) {
//		LatLng llDot = new LatLng(lat, lng);
//		OverlayOptions ooDot = new DotOptions()
//			.center(llDot)//设置圆点的圆心坐标
//			.radius(15)//设置圆点的半径（像素）, 默认为 5px
//			.color(0xFF0000FF);//设置圆点的颜色
//		mMapController.addOverlay(ooDot);
//	}
	
	/**设置定位参数
	 * @param option
	 */
	private void initLocationOptions(LocationClientOption option) {
		option.setOpenGps(true);//打开gps
		option.setCoorType("bd09ll");//设置坐标类型Baidu encoded latitude & longtitude
//		option.setScanSpan(1000 * 60 * 5);//扫描间隔5min
		option.setScanSpan(30 * 1000);//扫描间隔30s
		option.setLocationMode(LocationMode.Hight_Accuracy);//GPS + Network locating
		option.setAddrType("all");//locating results include all address infos
		option.setIsNeedAddress(true);//include address infos
	}
	
	@Override
    public void onStart() {
    	super.onStart();
    	/**开启POST请求获取json数据 */
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				JsonProcess.RequestForString(getActivity(), IMEI);
			}
		}, 3 * 1000, 20 * 1000);//每20s发送一次请求
    }
	
    @Override
	public void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView.onResume()，实现地图生命周期管理  
        mMapView.onResume();
    }
    
    @Override
	public void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView.onPause()，实现地图生命周期管理  
        mMapView.onPause();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	timer.cancel();
    	timer.purge();
    }

    @Override
    public void onDestroy() {  
    	// 退出时停止定位sdk
    	mLocationClient.stop();
    	// 关闭定位图层
    	mMapController.setMyLocationEnabled(false);
    	
    	mMapView.onDestroy();
    	mMapView = null;
    	super.onDestroy();
    }
}
