package com.example.mycalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.List;

public class Activity_Map extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    //地图控件
    public MapView View_TheMAP;

    //地图的MapView
    //public MapView View_TheMAPView;

    //包含地图的线性布局
    public LinearLayout LinearLayout_TheMap;

    //返回按钮
    public ImageButton btn_BackToEditUserBackUpMsg;

    //确认新地址按钮
    public ImageButton btn_ConfirmNewLocationFromMap;

    public TextView curSelectedLocation;

    public ListView ListView_POI;

    public LocationClient mLocationClient = null;

    private MyLocationListener myListener = new MyLocationListener();

    String myLocationDescribe;
    double myLatitude;    //获取纬度信息
    double myLongitude;    //获取经度信息
    float myRadius;    //获取定位精度，默认值为0.0f

    //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
    String myCoorType;

    //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
    int myErrorCode;

    //中心标点的设置
    OverlayOptions option;

    //当前地图上的标记
    Marker curMarker;

    //从经纬度反编码之后的新地址
    String newLocationDescribe;

    //反编码对象
    GeoCoder mCoder;

    //逆地址编码返回的数据版本 0为旧版 1为新版
    private static final int GEOCODER_DATA_VERSION = 1;

    //返回POI的范围
    private static final int POI_RADIUS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //百度定位----------------------------------------------------------------
        LocationClient.setAgreePrivacy(true);
        //声明LocationClient类
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //注册监听函数
        if(mLocationClient != null) {
            mLocationClient.registerLocationListener(myListener);
        }

        InitBaiduLocate();
        InitGeoCoder();
        //----------------------------------------------------------------------


        InitMapComponents();
        InitOtherComponents();

        View_TheMAP.bringToFront();
    }


    //逆地址编码相关
    private void InitGeoCoder() {
        mCoder = GeoCoder.newInstance();

        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            //逆地址编码回调函数
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                    return;
                } else {
                    //详细地址
                    String address = reverseGeoCodeResult.getAddressDetail().province +
                            reverseGeoCodeResult.getAddressDetail().city +
                            reverseGeoCodeResult.getAddressDetail().district +
                            reverseGeoCodeResult.getAddressDetail().town +
                            reverseGeoCodeResult.getAddressDetail().streetNumber +
                            reverseGeoCodeResult.getAddressDetail().direction;
                    //行政区号
                    //int adCode = reverseGeoCodeResult. getCityCode();

                    System.out.println(address);

                    newLocationDescribe = address;

                    //curSelectedLocation.setText(address);

                    ArrayAdapter<PoiInfo> PoiInfo_MapPoiAdapter = new ArrayAdapter<>(Activity_Map.this, R.layout.list_poilist, reverseGeoCodeResult.getPoiList());
                    ArrayAdapter<String> String_MapPoiAdapter = new ArrayAdapter<>(Activity_Map.this, R.layout.list_poilist);

                    //百度地图有时候会获取不到信息
                    try {
                        if(PoiInfo_MapPoiAdapter != null){
                            for (int i = 0;i < PoiInfo_MapPoiAdapter.getCount();++i){
                                String_MapPoiAdapter.add(PoiInfo_MapPoiAdapter.getItem(i).address + " " + PoiInfo_MapPoiAdapter.getItem(i).name);
                            }
                        }
                    }
                    catch (Exception e){
                        System.out.println(e);
                        String_MapPoiAdapter.add("宇宙 银河系 太阳系 地球");
                    }


                    //System.out.println();reverseGeoCodeResult.getPoiList().get(0).name

                    ListView_POI.setAdapter(String_MapPoiAdapter);
                }
            }
        };

        mCoder.setOnGetGeoCodeResultListener(listener);
    }


    //初始化地图相关组件
    public void InitMapComponents(){
        //地图设置对象
        //BaiduMapOptions mapOptions = new BaiduMapOptions();
        //设置地图模式为卫星地图
        //mapOptions.mapType(BaiduMap.MAP_TYPE_SATELLITE);
        //不允许旋转手势
        //mapOptions.rotateGesturesEnabled(false);

        //LinearLayout_TheMap = (LinearLayout) findViewById(R.id.LinearLayout_TheMap);
        //View_TheMAP = new MapView(LinearLayout_TheMap.getContext(), mapOptions);
//        View_TheMAP = new MapView(this, mapOptions);
//        setContentView(View_TheMAP);

        View_TheMAP = (MapView) findViewById(R.id.View_TheMAP);

        mLocationClient.start();


        View_TheMAP.getMap().setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            //地图状态开始改变。
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            //地图状态改变结束
            public void onMapStatusChangeFinish(MapStatus status) {
                //View_TheMAP.getMap().clear();
                if(curMarker != null) curMarker.remove();  //方法即可移除。

                //改变结束之后，获取地图可视范围的中心点坐标
                LatLng latLng = status.target;
                //拿到经纬度之后，就可以反地理编码获取地址信息了
                //initGeoCoder(latLng)
                System.out.println("new position: " + latLng.latitude + "/" + latLng.longitude);

                myLatitude = latLng.latitude;
                myLongitude = latLng.longitude;

                //定义Maker坐标点
                LatLng point = new LatLng(latLng.latitude, latLng.longitude);
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAsset("Icon_mark1.png");
                //构建Marker图标
                //构建MarkerOption，用于在地图上添加Marker
                option = new MarkerOptions()
                        .scaleX(3)
                        .scaleY(3)
                        .position(point) //必传参数
                        .icon(bitmap) //必传参数 BitmapFactory.decodeResource(getResources(),R.drawable.mycalendar_white))
                        .draggable(true)
                        //设置平贴地图，在地图中双指下拉查看效果
                        .flat(true)
                        .alpha(1f);
                //在地图上添加Marker，并显示
                curMarker = (Marker) View_TheMAP.getMap().addOverlay(option);

                //mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).pageNum(0).pageSize(100));
                mCoder.reverseGeoCode(new ReverseGeoCodeOption()
                        .newVersion(GEOCODER_DATA_VERSION)
                        .location(point)
                        // 设置是否返回新数据 默认值0不返回，1返回
                        .newVersion(1)
                        // POI召回半径，允许设置区间为0-1000米，超过1000米按1000米召回。默认值为1000
                        .radius(POI_RADIUS));

                btn_ConfirmNewLocationFromMap.setVisibility(View.INVISIBLE);
            }

            //地图状态变化中
            public void onMapStatusChange(MapStatus status) {
                if(curMarker != null) curMarker.remove();  //方法即可移除。

                //改变结束之后，获取地图可视范围的中心点坐标
                LatLng latLng = status.target;
                //拿到经纬度之后，就可以反地理编码获取地址信息了
                //initGeoCoder(latLng)
                System.out.println("new position: " + latLng.latitude + "/" + latLng.longitude);

                myLatitude = latLng.latitude;
                myLongitude = latLng.longitude;

                //定义Maker坐标点
                LatLng point = new LatLng(latLng.latitude, latLng.longitude);
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAsset("Icon_mark1.png");
                //构建Marker图标
                //构建MarkerOption，用于在地图上添加Marker
                option = new MarkerOptions()
                        .scaleX(3)
                        .scaleY(3)
                        .position(point) //必传参数
                        .icon(bitmap) //必传参数 BitmapFactory.decodeResource(getResources(),R.drawable.mycalendar_white))
                        .draggable(true)
                        //设置平贴地图，在地图中双指下拉查看效果
                        .flat(true)
                        .alpha(1f);
                //在地图上添加Marker，并显示
                curMarker = (Marker) View_TheMAP.getMap().addOverlay(option);

                btn_ConfirmNewLocationFromMap.setVisibility(View.INVISIBLE);
            }
        });


        //点击Marker时会回调BaiduMap.OnMarkerClickListener，监听器的实现方式示例如下：
        View_TheMAP.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法
            //若响应点击事件，返回true，否则返回false
            //默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println("marker click!!!!!!!!!!!!!!!!");

                return true;
            }
        });

        //在拖拽Marker时会回调BaiduMap.OnMarkerDragListener，监听器的实现方式如下(要在构造MarkerOptions时开启draggable):
        View_TheMAP.getMap().setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            //在Marker拖拽过程中回调此方法，这个Marker的位置可以通过getPosition()方法获取
            //marker 被拖动的Marker对象
            @Override
            public void onMarkerDrag(Marker marker) {
                //对marker处理拖拽逻辑
            }

            //在Marker拖动完成后回调此方法， 这个Marker的位可以通过getPosition()方法获取
            //marker 被拖拽的Marker对象
            @Override
            public void onMarkerDragEnd(Marker marker) {
                System.out.println("new position: " + marker.getPosition());
            }

            //在Marker开始被拖拽时回调此方法， 这个Marker的位可以通过getPosition()方法获取
            //marker 被拖拽的Marker对象
            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });


        //View_TheMAPView = new MapView(this, mapOptions);

        //LinearLayout_TheMap.addView(View_TheMAP);
    }

    //在地图上加点
    public void AddOverLayToMap(double latitude, double longitude){
        //定义Maker坐标点
        LatLng point = new LatLng(latitude, longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAsset("Icon_mark1.png");
        //构建Marker图标
        //构建MarkerOption，用于在地图上添加Marker
        option = new MarkerOptions()
                .scaleX(3)
                .scaleY(3)
                .position(point) //必传参数
                .icon(bitmap) //必传参数 BitmapFactory.decodeResource(getResources(),R.drawable.mycalendar_white))
                .draggable(true)
                //设置平贴地图，在地图中双指下拉查看效果
                .flat(true)
                .alpha(1f);
        //在地图上添加Marker，并显示
        curMarker = (Marker) View_TheMAP.getMap().addOverlay(option);

        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point) //标注居中显示!!!!!!!!
                .zoom(18)//图层大小
                .build();

        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        View_TheMAP.getMap().setMapStatus(mMapStatusUpdate);

    }

    //用于在回调函数中调用finish
    public void ThisFinish(){
        this.finish();
    }

    //初始化其他控件
    public void InitOtherComponents(){
        btn_BackToEditUserBackUpMsg = (ImageButton) findViewById(R.id.btn_BackToEditUserBackUpMsg);
        btn_ConfirmNewLocationFromMap = (ImageButton) findViewById(R.id.btn_ConfirmNewLocationFromMap);
        //curSelectedLocation = (TextView) findViewById(R.id.curSelectedLocation);
        ListView_POI = (ListView) findViewById(R.id.ListView_POI);


        btn_BackToEditUserBackUpMsg.setOnClickListener(this);
        btn_ConfirmNewLocationFromMap.setOnClickListener(this);
        ListView_POI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newLocationDescribe = ((TextView)view).getText().toString();
                //返回并保存新地址
                Intent backToEditUserBackUpMsgWithNewLocation = new Intent(Activity_Map.this, Activity_EditUserBackUpMsg.class);
                backToEditUserBackUpMsgWithNewLocation.putExtra("mapReturnCode", "1");
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlatitude", String.valueOf(myLatitude));
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlongitude", String.valueOf(myLongitude));
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlocationdescribe", newLocationDescribe);
                setResult(RESULT_OK, backToEditUserBackUpMsgWithNewLocation);

                ThisFinish();
            }
        });
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            myLatitude = latitude;
            myLongitude = longitude;
            myRadius = radius;

            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            myCoorType = coorType;

            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();
            myErrorCode = errorCode;
            if(errorCode != 161){
                Toast.makeText(Activity_Map.this, "定位失败：" + errorCode, Toast.LENGTH_SHORT).show();
            }
            System.out.println("LocateErr: " + String.valueOf(myErrorCode));

            //获取位置描述信息
            String locationDescribe = location.getLocationDescribe();
            myLocationDescribe = locationDescribe;

            System.out.println("Location: " + myLatitude + "/" + myLongitude + "(" + myRadius + ")");
            System.out.println("LocationDescribe: " + locationDescribe);

            AddOverLayToMap(myLatitude, myLongitude);

            //获取完毕，关闭监听
            mLocationClient.stop();

            mCoder.reverseGeoCode(new ReverseGeoCodeOption()
                    .newVersion(GEOCODER_DATA_VERSION)
                    .location(new LatLng(myLatitude, myLongitude))
                    // 设置是否返回新数据 默认值0不返回，1返回
                    .newVersion(1)
                    // POI召回半径，允许设置区间为0-1000米，超过1000米按1000米召回。默认值为1000
                    .radius(POI_RADIUS));
        }
    }

    //初始化百度定位对象
    public void InitBaiduLocate(){
        LocationClientOption option = new LocationClientOption();

        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        //LocationMode.Fuzzy_Locating, 模糊定位模式；v9.2.8版本开始支持，可以降低API的调用频率，但同时也会降低定位精度；
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setCoorType("bd09ll");

        //可选，首次定位时可以选择定位的返回是准确性优先还是速度优先，默认为速度优先
        //可以搭配setOnceLocation(Boolean isOnceLocation)单次定位接口使用，当设置为单次定位时，setFirstLocType接口中设置的类型即为单次定位使用的类型
        //FirstLocType.SPEED_IN_FIRST_LOC:速度优先，首次定位时会降低定位准确性，提升定位速度；
        //FirstLocType.ACCUARACY_IN_FIRST_LOC:准确性优先，首次定位时会降低速度，提升定位准确性；
        option.setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC);

        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setScanSpan(1000);

        //可选，设置是否使用卫星定位，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGnss(true);

        //可选，设置是否当卫星定位有效时按照1S/1次频率输出卫星定位结果，默认false
        option.setLocationNotify(true);

        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);

        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);

        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setWifiCacheTimeOut(5*60*1000);

        //可选，设置是否需要过滤卫星定位仿真结果，默认需要，即参数为false
        option.setEnableSimulateGnss(false);

        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        option.setNeedNewVersionRgc(true);

        //可选，是否需要位置描述信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的位置信息，此处必须为true
        option.setIsNeedLocationDescribe(true);

        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        if(mLocationClient != null) {
            mLocationClient.setLocOption(option);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_BackToEditUserBackUpMsg:{
                //直接返回，不保存新地址
                Intent backToEditUserBackUpMsgWithoutNewLocation = new Intent(Activity_Map.this, Activity_EditUserBackUpMsg.class);
                backToEditUserBackUpMsgWithoutNewLocation.putExtra("mapReturnCode", "0");
                backToEditUserBackUpMsgWithoutNewLocation.putExtra("newlatitude", String.valueOf(myLatitude));
                backToEditUserBackUpMsgWithoutNewLocation.putExtra("newlongitude", String.valueOf(myLongitude));
                setResult(RESULT_OK, backToEditUserBackUpMsgWithoutNewLocation);

                this.finish();

                break;
            }
            case R.id.btn_ConfirmNewLocationFromMap:{
                //返回并保存新地址
                Intent backToEditUserBackUpMsgWithNewLocation = new Intent(Activity_Map.this, Activity_EditUserBackUpMsg.class);
                backToEditUserBackUpMsgWithNewLocation.putExtra("mapReturnCode", "1");
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlatitude", String.valueOf(myLatitude));
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlongitude", String.valueOf(myLongitude));
                backToEditUserBackUpMsgWithNewLocation.putExtra("newlocationdescribe", newLocationDescribe);
                setResult(RESULT_OK, backToEditUserBackUpMsgWithNewLocation);

                this.finish();

                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        View_TheMAP.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        View_TheMAP.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        View_TheMAP.onDestroy();
    }
}