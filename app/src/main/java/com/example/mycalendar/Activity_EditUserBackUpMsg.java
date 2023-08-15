package com.example.mycalendar;

import static com.example.mycalendar.MainActivity.myCalendarOptions;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.contrarywind.view.WheelView;
import com.google.gson.Gson;
import com.receiver.AlarmReceiver;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;


public class Activity_EditUserBackUpMsg extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private static final String APP_ID = "102052176"; //获取的APPID

    private static final int REQUEST_CODE_MAP = 3;

    //编辑模式，-1表示为新添加，> -1则表示为修改，且该值为改天的备忘录条目的下标
    int editMode = -1;

    int curSelectedYear = 0;
    int curSelectedMonth = 0;
    int curSelectedDay = 0;

    String SourcePage;
    JSONObject JSON_UserBackUpMsg;

    public TextView txtShowCurDate;

    public ImageButton btn_ShareToQQ;

    public EditText EditText_NewUserBackUpMsgLocation;

    public ImageButton btn_ChooseLocation;

    public ImageButton btn_ShowMap;

    public TextView txtStartTime;
    public TextView txtEndTime;

    public Date startTime_Date;
    public String startTime_String;
    public Date endTime_Date;
    public String endTime_String;


    public ImageView btn_BackToMain;
    public ImageView btn_DelThisUserBackUpMsg;
    public ImageView btn_ConfirmChange;


    //输入的新备忘条目的标题
    String NewUserBackUpMsgTitle;
    public EditText EditText_NewUserBackUpMsgTitle;
    //输入的新备忘条目的内容
    String NewUserBackUpMsgContent;
    public EditText EditText_NewUserBackUpMsgContent;

    public CheckBox cb_ActivateRingForThisBackUpMsg;
    public CheckBox cb_ActivateVibrateForThisBackUpMsg;

    String NewUserBackUpMsgLocation;

    private Tencent mTencent;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_back_up_msg);


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
        //----------------------------------------------------------------------


        //获取从主页面传过来的数据
        Intent gotoAddNewUserBackUpMsg = getIntent();
        editMode = Integer.parseInt(gotoAddNewUserBackUpMsg.getStringExtra("editMode"));
        curSelectedYear = Integer.parseInt(gotoAddNewUserBackUpMsg.getStringExtra("curSelectedYear"));
        curSelectedMonth = Integer.parseInt(gotoAddNewUserBackUpMsg.getStringExtra("curSelectedMonth"));
        curSelectedDay = Integer.parseInt(gotoAddNewUserBackUpMsg.getStringExtra("curSelectedDay"));
        String String_UserBackUpMsg = gotoAddNewUserBackUpMsg.getStringExtra("TheUserBackUpMsg");
        SourcePage = gotoAddNewUserBackUpMsg.getStringExtra("SourcePage");
        try {
            JSON_UserBackUpMsg = new JSONObject(String_UserBackUpMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        try {
            InitView();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        txtShowCurDate.setText("" + curSelectedYear + " 年 " + curSelectedMonth + " 月 " + curSelectedDay + " 日");
        mTencent = Tencent.createInstance(APP_ID, Activity_EditUserBackUpMsg.this.getApplicationContext());
    }

    public Context GetThis(){
        return this;
    }


    //实现EditText监听接口，用于时间的格式纠正
    public class EditText_Time_Format_Correcter implements TextWatcher{

        private EditText curEditText;
        private String type;  //HOUR 表示这个文本框是小时输入框，MIN 表示这个文本框是分钟输入框

        public EditText_Time_Format_Correcter(EditText v, String type){
            curEditText = v;
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            curEditText.setSelection(curEditText.length());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            curEditText.setSelection(curEditText.length());
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(curEditText.getText().length() > 2){
                if(curEditText.getText().charAt(0) == '0'){
                    curEditText.setText(String.valueOf(curEditText.getText()).substring(1, 3));
                }
                else {
                    curEditText.setText(String.valueOf(curEditText.getText()).substring(0, 2));
                }
            }
            if(curEditText.getText().length() < 1){
                curEditText.setText("00");
            }
            if(curEditText.getText().length() == 1){
                curEditText.setText("0" + curEditText.getText());
            }
            if(type.equals("HOUR")){
                if(Integer.parseInt(String.valueOf(curEditText.getText())) > 23){
                    curEditText.setText("23");
                }
                if(Integer.parseInt(String.valueOf(curEditText.getText())) < 0){
                    curEditText.setText("00");
                }
            }
            else if(type.equals("MIN")){
                if(Integer.parseInt(String.valueOf(curEditText.getText())) > 59){
                    curEditText.setText("59");
                }
                if(Integer.parseInt(String.valueOf(curEditText.getText())) < 0){
                    curEditText.setText("00");
                }
            }
            curEditText.setSelection(curEditText.length());
        }
    }


    //实现BDAbstractLocationListener接口
    //Android定位SDK自V7.2版本起，对外提供了Abstract类型的监听接口BDAbstractLocationListener，
    // 用于实现定位监听。原有BDLocationListener暂时保留，推荐开发者升级到Abstract类型的新监听接口使用，
    // 该接口会异步获取定位结果，核心代码如下：
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
                Toast.makeText(Activity_EditUserBackUpMsg.this, "定位失败：" + errorCode, Toast.LENGTH_SHORT).show();
            }
            System.out.println("LocateErr: " + String.valueOf(myErrorCode));

            //获取位置描述信息
            String locationDescribe = location.getLocationDescribe();
            myLocationDescribe = locationDescribe;

            System.out.println("Location: " + myLatitude + "/" + myLongitude + "(" + myRadius + ")");
            System.out.println("LocationDescribe: " + locationDescribe);

            EditText_NewUserBackUpMsgLocation.setText(myLocationDescribe);
            EditText_NewUserBackUpMsgLocation.setSelection(EditText_NewUserBackUpMsgLocation.length());

            //获取完毕，关闭监听
            mLocationClient.stop();
        }
    }

//    //设定闹钟提醒日程
    public void SetArrangementAlarm(int year, int month, int day, int hour, int min, int sec, boolean ring, boolean vibrate ,boolean delpi) throws JSONException {
        System.out.println("alarmTime: " + year + "/" + month + "/" + day + "  " + hour + ":" + min + ":" + sec);
        //用calendar设定时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);

        AlarmManager alarmMgr;
        PendingIntent alarmPendingIntent;

        if(!delpi){
            //根据参数设定 响铃 和 震动 情况
            if(!ring && !vibrate){
                System.out.println("无震动或铃声");
            }
            else {
                System.out.println("通知来了吗");
                alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(this, AlarmReceiver.class);
                alarmIntent.putExtra("isring", ring);
                alarmIntent.putExtra("isvibrate", vibrate);
                alarmIntent.putExtra("title", String.valueOf(EditText_NewUserBackUpMsgTitle.getText()));
                alarmIntent.putExtra("ringname", myCalendarOptions.getRingName());
                int rc = 0;
                if(editMode == -1){
                    rc = myCalendarOptions.getPendingIntentRequestCodeCount();
                }
                else {
                    rc = (int) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("pendingIntentRequestCodeCount");
                }

                alarmPendingIntent = PendingIntent.getBroadcast(this, rc, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Log.i("newAlarm", "newAlarm(" + rc + "): " + alarmPendingIntent);

                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
            }
        }
        else {
            System.out.println("求求你了，干死已有的pi吧");
            alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.putExtra("isring", false);
            alarmIntent.putExtra("isvibrate", false);
            alarmIntent.putExtra("title", "");
            alarmIntent.putExtra("ring", "ciao");
            int rc = 0;
            if(editMode == -1){
                rc = myCalendarOptions.getPendingIntentRequestCodeCount();
            }
            else {
                rc = (int) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("pendingIntentRequestCodeCount");
            }

            alarmPendingIntent = PendingIntent.getBroadcast(this, rc, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Log.i("cancelAlarm", "cancelAlarm(" + rc + "): " + alarmPendingIntent);

            //alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
            alarmMgr.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
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

    //初始化控件
    public void InitView() throws JSONException {
        txtShowCurDate = (TextView) findViewById(R.id.txtShowCurDate);
        txtStartTime = (TextView) findViewById(R.id.txtStartTime);
        txtEndTime = (TextView) findViewById(R.id.txtEndTime);
//        EditText_UserBackUpMsg_StartTime_Hour = (EditText) findViewById(R.id.EditText_UserBackUpMsg_StartTime_Hour);
//        EditText_UserBackUpMsg_StartTime_Min = (EditText) findViewById(R.id.EditText_UserBackUpMsg_StartTime_Min);
//        EditText_UserBackUpMsg_EndTime_Hour = (EditText) findViewById(R.id.EditText_UserBackUpMsg_EndTime_Hour);
//        EditText_UserBackUpMsg_EndTime_Min = (EditText) findViewById(R.id.EditText_UserBackUpMsg_EndTime_Min);
        btn_BackToMain = (ImageView) findViewById(R.id.btn_BackToMain);
        btn_DelThisUserBackUpMsg = (ImageView) findViewById(R.id.btn_DelThisUserBackUpMsg);
        btn_ConfirmChange = (ImageView) findViewById(R.id.btn_ConfirmChange);
        btn_ShareToQQ = (ImageButton) findViewById(R.id.btn_ShareToQQ);
        btn_ChooseLocation = (ImageButton) findViewById(R.id.btn_ChooseLocation);
        btn_ShowMap = (ImageButton) findViewById(R.id.btn_ShowMap);
        EditText_NewUserBackUpMsgLocation = (EditText) findViewById(R.id.EditText_NewUserBackUpMsgLocation);
        EditText_NewUserBackUpMsgTitle = (EditText) findViewById(R.id.EditText_NewUserBackUpMsgTitle);
        EditText_NewUserBackUpMsgContent = (EditText) findViewById(R.id.EditText_NewUserBackUpMsgContent);
        cb_ActivateRingForThisBackUpMsg = (CheckBox) findViewById(R.id.cb_ActivateRingForThisBackUpMsg);
        cb_ActivateVibrateForThisBackUpMsg = (CheckBox) findViewById(R.id.cb_ActivateVibrateForThisBackUpMsg);

        btn_BackToMain.setOnClickListener(this);
        btn_DelThisUserBackUpMsg.setOnClickListener(this);
        btn_ConfirmChange.setOnClickListener(this);
        btn_ShareToQQ.setOnClickListener(this);
        btn_ChooseLocation.setOnClickListener(this);
        btn_ShowMap.setOnClickListener(this);
        txtStartTime.setOnClickListener(this);
        txtEndTime.setOnClickListener(this);
        cb_ActivateRingForThisBackUpMsg.setOnClickListener(this);
        cb_ActivateVibrateForThisBackUpMsg.setOnClickListener(this);


        if(editMode == -1){  //在添加新的备忘条目
            System.out.println("new userbackupmsg");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date(System.currentTimeMillis());
            String curTime = simpleDateFormat.format(date);
            btn_DelThisUserBackUpMsg.setVisibility(View.GONE); //隐藏删除按钮
            startTime_String = curTime;
            endTime_String = curTime;
            txtStartTime.setText("  开始时间:  " + curTime + "  ");
            txtEndTime.setText("  结束时间:  " + curTime + "  ");
        }
        else {  //在更新已有的备忘录条目
            EditText_NewUserBackUpMsgTitle.setText((CharSequence) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("title"));
            EditText_NewUserBackUpMsgContent.setText((CharSequence) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("content"));
            EditText_NewUserBackUpMsgLocation.setText((CharSequence) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("location"));

            startTime_String = String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("starttime"));
            txtStartTime.setText("  开始时间:  " + startTime_String + "  ");
            endTime_String = String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("endtime"));
            txtEndTime.setText("  结束时间:  " + endTime_String + "  ");

            cb_ActivateRingForThisBackUpMsg.setChecked((Boolean) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("activatering"));
            cb_ActivateVibrateForThisBackUpMsg.setChecked((Boolean) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("activatevibrate"));
        }

        cb_ActivateRingForThisBackUpMsg.setText("启用铃声(" + myCalendarOptions.getRingName() + ")");
    }

    //将用户的备忘录内容保存到本地
    public void SaveUserBackUpMsgToLocalFile() throws IOException {
        FileOutputStream fileOutputStream = openFileOutput("MyCalendarUserBackUpMsg.json",MODE_PRIVATE);
        String UserBackUpMsg = String.valueOf(JSON_UserBackUpMsg);
        fileOutputStream.write(UserBackUpMsg.getBytes());
        fileOutputStream.close();
    }

    //从别的窗体返回
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_MAP:{  //从地图页面返回
                //mapReturnCode  0说明地址不变，1说明用户选了新地址
                int mapReturnCode = Integer.parseInt(data.getStringExtra("mapReturnCode"));
                if(mapReturnCode == 1){
                    myLatitude = Double.parseDouble(data.getStringExtra("newlatitude"));
                    myLongitude = Double.parseDouble(data.getStringExtra("newlongitude"));
                    String newLocationDescribe = data.getStringExtra("newlocationdescribe");
                    EditText_NewUserBackUpMsgLocation.setText(newLocationDescribe);
                    EditText_NewUserBackUpMsgLocation.setSelection(EditText_NewUserBackUpMsgLocation.length());
                }

                System.out.println("New_Location: " + myLatitude + "/" + myLongitude + "(" + myRadius + ")");

                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        //获取当前的标题和内容和地址
        NewUserBackUpMsgTitle = String.valueOf(EditText_NewUserBackUpMsgTitle.getText());
        NewUserBackUpMsgContent = String.valueOf(EditText_NewUserBackUpMsgContent.getText());
        NewUserBackUpMsgLocation = String.valueOf(EditText_NewUserBackUpMsgLocation.getText());

        //获取时间
        String startH = startTime_String.substring(0, 2);
        String startM = startTime_String.substring(3, 5);
        String endH = endTime_String.substring(0, 2);
        String endM = endTime_String.substring(3, 5);


        switch (v.getId()){
            case R.id.btn_BackToMain:{  //返回主界面（不保存新的）
                if(SourcePage.equals("MAIN")){
                    Intent backToMainWithoutSaveNewUserBackUpMsg = new Intent(Activity_EditUserBackUpMsg.this, MainActivity.class);
                    startActivity(backToMainWithoutSaveNewUserBackUpMsg);
                }
                else if(SourcePage.equals("SHOW_ALL_USERBACKUPMSGS")){
                    Intent backToMainWithoutSaveNewUserBackUpMsg = new Intent(Activity_EditUserBackUpMsg.this, Activity_ShowAllUserBackUpMsgs.class);
                    startActivity(backToMainWithoutSaveNewUserBackUpMsg);
                }

                break;
            }
            case R.id.btn_ConfirmChange:{  //保存更改并返回上一个界面
                //先判断时间格式是否正确
                int startH_int = Integer.parseInt(startTime_String.substring(0, 2));
                int startM_int = Integer.parseInt(startTime_String.substring(3, 5));
                int endH_int = Integer.parseInt(endTime_String.substring(0, 2));
                int endM_int = Integer.parseInt(endTime_String.substring(3, 5));

                if((endH_int < startH_int) || (endH_int == startH_int && endM_int < startM_int)){
                    Toast.makeText(Activity_EditUserBackUpMsg.this, "结束时间应晚于开始时间", Toast.LENGTH_SHORT).show();
                    break;
                }


                //System.out.println("原页面：" + SourcePage);

                if(SourcePage.equals("MAIN")){
                    //返回到主界面
                    if(NewUserBackUpMsgTitle.equals("") || NewUserBackUpMsgContent.equals("")){
                        //如果标题和内容有一个为空
                        Toast.makeText(Activity_EditUserBackUpMsg.this, "内容和标题均不能为空", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //这里要根据用户修改的信息是否启用系统铃声和震动提醒来设置系统服务
                        //启用 铃声 值为cb_ActivateRingForThisBackUpMsg.isChecked()
                        //启用 震动 值为cb_ActivateVibrateForThisBackUpMsg.isChecked()
                        try {
                            SetArrangementAlarm(curSelectedYear, curSelectedMonth, curSelectedDay, startH_int, startM_int, 0, cb_ActivateRingForThisBackUpMsg.isChecked(), cb_ActivateVibrateForThisBackUpMsg.isChecked(), false);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        //实体类转json字符串
                        Gson gson = new Gson();
                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = openFileOutput("Options.json", MODE_PRIVATE);
                            fileOutputStream.write(gson.toJson(myCalendarOptions).getBytes());
                            fileOutputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        //标题和内容均不为空
                        if(editMode == -1){  //在添加新的备忘条目
                            try {
                                if(!JSON_UserBackUpMsg.has(String.valueOf(curSelectedYear))){
                                    //不包含当前选中的年
                                    JSON_UserBackUpMsg.put(String.valueOf(curSelectedYear), new JSONObject());
                                }
                                if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).has(String.valueOf(curSelectedMonth))){
                                    //不包含当前选中的月
                                    JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).put(String.valueOf(curSelectedMonth), new JSONObject());
                                }
                                if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).has(String.valueOf(curSelectedDay))) {
                                    //不包含当前选中的日
                                    JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).put(String.valueOf(curSelectedDay), new JSONArray());
                                }
                                JSONObject tmpNewUserBackUpMsg = new JSONObject();
                                tmpNewUserBackUpMsg.put("title", NewUserBackUpMsgTitle);
                                tmpNewUserBackUpMsg.put("content", NewUserBackUpMsgContent);
                                tmpNewUserBackUpMsg.put("starttime", startH + ":" + startM);
                                tmpNewUserBackUpMsg.put("endtime", endH + ":" + endM);
                                tmpNewUserBackUpMsg.put("activatering", cb_ActivateRingForThisBackUpMsg.isChecked());
                                tmpNewUserBackUpMsg.put("activatevibrate", cb_ActivateVibrateForThisBackUpMsg.isChecked());
                                tmpNewUserBackUpMsg.put("pendingIntentRequestCodeCount", myCalendarOptions.getPendingIntentRequestCodeCount());
                                if(NewUserBackUpMsgLocation.equals("")){
                                    tmpNewUserBackUpMsg.put("location", "宇宙 银河系 太阳系 地球");
                                }
                                else {
                                    tmpNewUserBackUpMsg.put("location", NewUserBackUpMsgLocation);
                                }
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).put(tmpNewUserBackUpMsg);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else {
                            try {
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).remove("title");
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).remove("content");
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("title", NewUserBackUpMsgTitle);
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("content", NewUserBackUpMsgContent);
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("starttime", startH + ":" + startM);
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("endtime", endH + ":" + endM);
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("activatering", cb_ActivateRingForThisBackUpMsg.isChecked());
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("activatevibrate", cb_ActivateVibrateForThisBackUpMsg.isChecked());
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("pendingIntentRequestCodeCount",
                                        JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("pendingIntentRequestCodeCount")
                                        );
                                if(NewUserBackUpMsgLocation.equals("")){
                                    JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("location", "宇宙 银河系 太阳系 地球");
                                }
                                else {
                                    JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("location", NewUserBackUpMsgLocation);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //保存到本地文件
                        try {
                            SaveUserBackUpMsgToLocalFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Intent backToMainWithSaveNewUserBackUpMsg = new Intent(Activity_EditUserBackUpMsg.this, MainActivity.class);
                        backToMainWithSaveNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                        setResult(RESULT_OK, backToMainWithSaveNewUserBackUpMsg);
                        this.finish();
                        //startActivity(backToMainWithSaveNewUserBackUpMsg);
                    }
                }
                else if(SourcePage.equals("SHOW_ALL_USERBACKUPMSGS")){

                    //返回到显示所有备忘录的界面
                    if(NewUserBackUpMsgTitle.equals("") || NewUserBackUpMsgContent.equals("")){
                        //如果标题和内容有一个为空
                        Toast.makeText(Activity_EditUserBackUpMsg.this, "内容和标题均不能为空", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //标题和内容均不为空
                        try {
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).remove("title");
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).remove("content");
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("title", NewUserBackUpMsgTitle);
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("content", NewUserBackUpMsgContent);
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("starttime", startH + ":" + startM);
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("endtime", endH + ":" + endM);
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("activatering", cb_ActivateRingForThisBackUpMsg.isChecked());
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("activatevibrate", cb_ActivateVibrateForThisBackUpMsg.isChecked());
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("pendingIntentRequestCodeCount",
                                    JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("pendingIntentRequestCodeCount")
                            );
                            if(NewUserBackUpMsgLocation.equals("")){
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("location", "宇宙 银河系 太阳系 地球");
                            }
                            else {
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).put("location", NewUserBackUpMsgLocation);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        //保存到本地文件
                        try {
                            SaveUserBackUpMsgToLocalFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Intent backToMainWithSaveNewUserBackUpMsg = new Intent(Activity_EditUserBackUpMsg.this, Activity_ShowAllUserBackUpMsgs.class);
                        backToMainWithSaveNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                        setResult(RESULT_OK, backToMainWithSaveNewUserBackUpMsg);
                        this.finish();
                    }
                }
                break;
            }
            case R.id.btn_DelThisUserBackUpMsg:{ //删除这个备忘录
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("你确定要删除此日程吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 按下确定按钮的事件
                        try {
                            btn_DelThisUserBackUpMsg_true();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();

                break;
            }
            case R.id.btn_ShareToQQ:{  //分享到QQ
                Toast.makeText(Activity_EditUserBackUpMsg.this, "正在分享", Toast.LENGTH_SHORT).show();

                final Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT); //分享的类型
                params.putString(QQShare.SHARE_TO_QQ_TITLE, "日程:" + NewUserBackUpMsgTitle); //分享标题
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, txtShowCurDate.getText() + startH + ":" + startM + "-" + endH + ":" + endM + '\n' + NewUserBackUpMsgContent); //要分享的内容摘要
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,"https://github.com/ZooMEISTER");//内容地址
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,"https://raw.githubusercontent.com/ZooMEISTER/myImgs/main/MyCalendar.png");//分享的图片URL
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "MyCalendar");//应用名称
                mTencent.shareToQQ(Activity_EditUserBackUpMsg.this, params, new ShareUiListener());

                break;
            }
            case R.id.btn_ChooseLocation:{  //调用定位功能
                //mLocationClient为第二步初始化过的LocationClient对象
                //调用LocationClient的start()方法，便可发起定位请求
                if(mLocationClient != null) {
                    Toast.makeText(Activity_EditUserBackUpMsg.this, "正在定位", Toast.LENGTH_SHORT).show();
                    mLocationClient.start();
                }

                EditText_NewUserBackUpMsgLocation.setText(myLocationDescribe);


//                PlayAlarm(this);
//                PlayVibrate(this);


                break;
            }
            case R.id.btn_ShowMap:{  //跳转到地图页面
                Intent goToShowMap = new Intent(Activity_EditUserBackUpMsg.this, Activity_Map.class);
                startActivityForResult(goToShowMap, REQUEST_CODE_MAP);

                break;
            }
            case R.id.cb_ActivateRingForThisBackUpMsg:{  //切换是否启用铃声通知

                break;
            }
            case R.id.txtStartTime:{  //选择开始时间
                //用calendar设定时间
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, curSelectedYear);
                calendar.set(Calendar.MONTH, curSelectedMonth - 1);
                calendar.set(Calendar.DAY_OF_MONTH, curSelectedDay);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime_String.substring(0, 2)));
                calendar.set(Calendar.MINUTE, Integer.parseInt(startTime_String.substring(3, 5)));
                calendar.set(Calendar.SECOND, 0);

                TimePickerView pvTime = new TimePickerBuilder(Activity_EditUserBackUpMsg.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        System.out.println(getTime(date));
                        startTime_Date = date;
                        startTime_String = getTime(date);

                        txtStartTime.setText("  开始时间:  " + startTime_String + "  ");
                    }
                })
                        .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                            @Override
                            public void onTimeSelectChanged(Date date) {

                            }
                        })
                        .setType(new boolean[]{false, false, false, true, true, false})
                        .setCancelText("取消")
                        .setCancelColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setSubmitText("确认")
                        .setSubmitColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setSubCalSize(20)
                        .setTitleText("选择开始时间")
                        .setTitleColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setTitleSize(20)
                        .setTitleBgColor(getResources().getColor(R.color.white))
                        .setBgColor(getResources().getColor(R.color.white))
                        .setLabel("年", "月", "日", "时", "分", "秒")
                        .setDate(calendar)
                        .setContentTextSize(20)
                        .setItemVisibleCount(7)
                        .setTextColorCenter(getResources().getColor(R.color.dividespaceviewcolor))
                        .setTextColorOut(getResources().getColor(R.color.light_blue))
                        .setOutSideColor(getResources().getColor(R.color.white))
                        .setDividerColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setDividerType(WheelView.DividerType.WRAP)
                        .setLineSpacingMultiplier(2.5f)
                        .isCyclic(true)
                        .isCenterLabel(true)
                        .build();

                pvTime.show();
                break;

            }
            case R.id.txtEndTime: {  //选择结束时间
                //用calendar设定时间
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, curSelectedYear);
                calendar.set(Calendar.MONTH, curSelectedMonth - 1);
                calendar.set(Calendar.DAY_OF_MONTH, curSelectedDay);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime_String.substring(0, 2)));
                calendar.set(Calendar.MINUTE, Integer.parseInt(endTime_String.substring(3, 5)));
                calendar.set(Calendar.SECOND, 0);

                TimePickerView pvTime = new TimePickerBuilder(Activity_EditUserBackUpMsg.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {
                        System.out.println(getTime(date));
                        endTime_Date = date;
                        endTime_String = getTime(date);

                        txtEndTime.setText("  结束时间:  " + endTime_String + "  ");
                    }
                })
                        .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                            @Override
                            public void onTimeSelectChanged(Date date) {}
                        })
                        .setType(new boolean[]{false, false, false, true, true, false})
                        .setCancelText("取消")
                        .setCancelColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setSubmitText("确认")
                        .setSubmitColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setSubCalSize(20)
                        .setTitleText("选择结束时间")
                        .setTitleColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setTitleSize(20)
                        .setTitleBgColor(getResources().getColor(R.color.white))
                        .setBgColor(getResources().getColor(R.color.white))
                        .setLabel("年", "月", "日", "时", "分", "秒")
                        .setDate(calendar)
                        .setContentTextSize(20)
                        .setItemVisibleCount(7)
                        .setTextColorCenter(getResources().getColor(R.color.dividespaceviewcolor))
                        .setTextColorOut(getResources().getColor(R.color.light_blue))
                        .setOutSideColor(getResources().getColor(R.color.white))
                        .setDividerColor(getResources().getColor(R.color.dividespaceviewcolor))
                        .setDividerType(WheelView.DividerType.WRAP)
                        .setLineSpacingMultiplier(2.5f)
                        .isCyclic(true)
                        .isCenterLabel(true)
                        .build();

                pvTime.show();
                break;
            }//选择结束时间
        }
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }


    /**
     * 自定义监听器实现IUiListener，需要3个方法
     * onComplete完成 onError错误 onCancel取消
     */
    private class ShareUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            //分享成功
            Toast.makeText(Activity_EditUserBackUpMsg.this, "分享成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(UiError uiError) {
            //分享失败
            Toast.makeText(Activity_EditUserBackUpMsg.this, "分享失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            //分享取消
            Toast.makeText(Activity_EditUserBackUpMsg.this, "分享取消", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWarning(int i) {
            Toast.makeText(Activity_EditUserBackUpMsg.this, "onWarning", Toast.LENGTH_SHORT).show();
        }
    }


    //由于this.finish(); 在onclick回调函数中得重写，故单独拿出来
    public void btn_DelThisUserBackUpMsg_true() throws JSONException {
        //先删除该日程的pendingintent
        SetArrangementAlarm(1970,0,1,0,0,0,false,false,true);

//        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
//        alarmIntent.putExtra("isring", false);
//        alarmIntent.putExtra("isvibrate", false);
//        int rc = (int) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(editMode).get("pendingIntentRequestCodeCount");
//        PendingIntent alarmPendingIntent = PendingIntent.getService(this, rc, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        if (alarmPendingIntent != null && alarmMgr != null) {
//            Log.i("cancelAlarm", "cancelAlarm(" + rc + "): " + alarmPendingIntent);
//            alarmMgr.cancel(alarmPendingIntent);
//            alarmPendingIntent.cancel();
//        } else {
//            Log.i("cancelAlarm", "Alarm not found ！");
//        }


        //从json中删除并保存到本地文件
        try {
            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).remove(editMode);
            SaveUserBackUpMsgToLocalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Intent backToMainWithSaveNewUserBackUpMsg = new Intent(Activity_EditUserBackUpMsg.this, MainActivity.class);
        backToMainWithSaveNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
        setResult(RESULT_OK, backToMainWithSaveNewUserBackUpMsg);
        this.finish();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}