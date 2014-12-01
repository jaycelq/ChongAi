package me.qiang.android.chongai.Activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.*;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import me.qiang.android.chongai.R;

public class BaiduLBS extends ActionBarActivity {


    MapView myMap=null;
    BaiduMap mBaiduMap = null;
    double myLongitude=31.205209;
    double myLatitude=121.439316;
    LatLng myPoint= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化地图
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_baidu_map);
        myMap=(MapView)findViewById(R.id.bmapView);
        mBaiduMap=myMap.getMap();




        Button btnNextPoint=(Button)findViewById(R.id.nextPoint);
        btnNextPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLongitude=myLongitude+0.0001;
                myPoint=new LatLng(myLongitude,myLatitude);

                mBaiduMap.clear();
                mBaiduMap.addOverlay(new MarkerOptions().position(myPoint)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_marka)));
                mBaiduMap.setMaxAndMinZoomLevel(19,18);
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(myPoint));
                Toast.makeText(getApplicationContext(), "经度"+myLongitude, Toast.LENGTH_LONG)
                        .show();

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_baidu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
