package com.example.alvin.lbsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;

import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LocationClient m_locationClient;
    private MapView m_mapView;
    private BaiduMap m_baiduMap;
    private boolean b_isFirstLocate = true;

    private Vector<MonitorDef> listMonitor = new Vector<>();

    private final int LIST_SIZE = 10;
    private final int STATE_SiZE = 10;
    private boolean m_bLoadFinsh;

    private View m_markView;
    private ImageView m_icon;
    private TextView  m_text;
    private Random rand = new Random();

    private HashMap<Integer,Marker> MapDevIDToMarker = new HashMap<>();

    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_locationClient = new LocationClient(getApplicationContext());
        m_locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        m_mapView = (MapView)findViewById(R.id.bmapView);
        m_baiduMap = m_mapView.getMap();
        m_baiduMap.setMyLocationEnabled(true);

        m_markView = View.inflate(this,R.layout.marker_layout,null);

        m_icon = (ImageView) m_markView.findViewById(R.id.iv_markerIcon);
        m_text = (TextView) m_markView.findViewById(R.id.tv_markerTable);

        initPermission();

        initMarkerClickEvent();
        initListMonitor();


        //地图点击事件
        m_baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                m_baiduMap.hideInfoWindow();
            }
        });

        m_baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback(){
            @Override
            public void onMapLoaded(){
                m_bLoadFinsh = true;
            }
        });

    }

    private void initListMonitor(){
        LogUtil.i("Marker","begin show marker on map");
        for(int i=0;i<LIST_SIZE;i++){
            listMonitor.add(new MonitorDef(i));
        }


        for(MonitorDef monitor:listMonitor){
            showMarkerOnMap(monitor);
        }

        LogUtil.i("Marker","end show marker on map");
    }
    private void initPermission(){
        List<String> lstPermission = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            lstPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
        {
            lstPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            lstPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(! lstPermission.isEmpty())
        {
            String[] permissions = lstPermission.toArray(new String[lstPermission.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else{
            requestLocation();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        m_mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        m_mapView.onPause();
    }

    private void requestLocation(){
        initLocation();
        m_locationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(10000);
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll");
        m_locationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        m_locationClient.stop();
        m_mapView.onDestroy();
        m_baiduMap.setMyLocationEnabled(false);
    }

    private void navigeteTo(BDLocation location){
        if(b_isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            m_baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            m_baiduMap.animateMapStatus(update);

            if(m_baiduMap.getLocationData() != null){
                if(m_baiduMap.getLocationData().latitude == location.getLatitude()
                    && m_baiduMap.getLocationData().longitude == location.getLongitude()){
                    b_isFirstLocate = false;
                }
            }

        }

        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        m_baiduMap.setMyLocationData(locationData);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if(grantResults.length > 0)
                {
                    for(int result : grantResults)
                    {
                        if(result != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this,"必须同意所有的权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }
                break;
            default:
        }
    }


    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLocType() == BDLocation.TypeGpsLocation ||
                    location.getLocType() == BDLocation.TypeNetWorkLocation)
            {
                navigeteTo(location);
            }
        }


        @Override
        public  void onConnectHotSpotMessage(String var1, int var2){

        }
    }

    public void showMarkerOnMap(final MonitorDef monitor){

        final LatLng latLng = new LatLng(monitor.getLatitude(), monitor.getLongitude());

        int iState = monitor.getState();
        m_icon.setImageResource(escStatusToBitmap(iState));
        m_text.setText(monitor.getMonitorName());

        final BitmapDescriptor iBitmapID = BitmapDescriptorFactory.fromView(m_markView);

        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng)
                        .icon(iBitmapID)
                        .title(monitor.getMonitorName());

                Marker marker = (Marker) (m_baiduMap.addOverlay(markerOptions));

                MapDevIDToMarker.put(monitor.getMonitorID(),marker);
                //设置marker附带的信息
                Bundle bundle  = new Bundle();
                bundle.putSerializable("Monitor",monitor);
                marker.setExtraInfo(bundle);
                marker.setVisible(true);
            }
        });

    }
    private void initMarkerClickEvent()
    {
        // 对Marker的点击
        m_baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(final Marker marker)
            {
                // 获得marker中的数据
                final MonitorDef monitor = (MonitorDef) marker.getExtraInfo().get("Monitor");

                InfoWindow mInfoWindow;
                // 生成一个TextView用户在地图中显示InfoWindow
                TextView text = new TextView(getApplicationContext());
                text.setSingleLine(false);
                text.setHeight(80);
                text.setWidth(400);
                text.setMaxLines(5);
                text.setVerticalScrollBarEnabled(true);
                text.setMovementMethod(new ScrollingMovementMethod());
                text.setBackgroundResource(R.drawable.mapinfor);
                text.setPadding(20, 10, 10, 10);

                StringBuilder context = new StringBuilder();
                context.append(monitor.getMonitorName()).append("\n");
                context.append(monitor.getMonitorID()).append("\n");
                context.append(monitor.getAddress()).append("\n");
                text.setText(context.toString());
                text.setTextSize(12);
                // 将marker所在的经纬度的信息转化成屏幕上的坐标
                final LatLng ll = marker.getPosition();
                Point p = m_baiduMap.getProjection().toScreenLocation(ll);
                LatLng llInfo = m_baiduMap.getProjection().fromScreenLocation(p);
                //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(text), llInfo, -47, new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {

                    }
                });

                // 显示InfoWindow
                m_baiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }
    private int escStatusToBitmap(int iStatus){
        int iBitmap;
        switch (iStatus){
            case 1:
                iBitmap = R.mipmap.fault;
                break;
            case 2:
                iBitmap = R.mipmap.up;
                break;
            case 3:
                iBitmap = R.mipmap.down;
                break;
            case 4:
                iBitmap = R.mipmap.slowup;
                break;
            case 5:
                iBitmap = R.mipmap.slowdown;
                break;
            case 6:
                iBitmap = R.mipmap.intermittentup;
                break;
            case 7:
                iBitmap = R.mipmap.intermittentdown;
                break;
            case 8:
                iBitmap = R.mipmap.maintain;
                break;
            case 9:
                iBitmap = R.mipmap.comerr;
                break;
            default:
                iBitmap = R.mipmap.comerr;
                break;
        }

        return iBitmap;
    }
    public void updateDevBitmap(){
        if(m_bLoadFinsh == false)
            return;

        int index = rand.nextInt(LIST_SIZE);
        final MonitorDef monitor = listMonitor.get(index);

        int iState = rand.nextInt(STATE_SiZE);
        m_icon.setImageResource(escStatusToBitmap(iState));
        m_text.setText(monitor.getMonitorName());

        final BitmapDescriptor iBitmapID = BitmapDescriptorFactory.fromView(m_markView);

        Marker marker = MapDevIDToMarker.get(monitor.getMonitorID());
        marker.setIcon(iBitmapID);
    /*
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                if (monitor != null) {

                    LatLng latlng = new LatLng(monitor.getLatitude(), monitor.getLongitude());
                    OverlayOptions overlayOptions = new MarkerOptions().position(latlng).icon(iBitmapID);
                    Marker marker = (Marker) m_baiduMap.addOverlay(overlayOptions);

                    //设置marker附带的信息
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Monitor", monitor);
                    marker.setExtraInfo(bundle);
                    marker.setVisible(true);
                }
            }
        });
        */

    }

    @Override
    public void onBackPressed(){
        updateDevBitmap();
    }
}
