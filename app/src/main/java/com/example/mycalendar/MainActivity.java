package com.example.mycalendar;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.tencent.tauth.Tencent;
import com.unit.MyCalendarOptions;
import com.view.MonthPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    public static final int REQUEST_CODE_EDITBACKUPMSG = 1; //从编辑备忘录的界面返回
    public static final int REQUEST_CODE_OPTIONS = 2; //从设置的界面返回
    public static final int REQUEST_CODE_FULLYEARCALENDAR = 4; //从显示全年日历的界面返回

    static MyCalendarOptions myCalendarOptions = new MyCalendarOptions();

    //表示当前是否需要重设年月选择器
    public boolean isIShouldResetMonthPicker = false;

    //布尔型变量，表示当前日历是否折叠起来
    public boolean isTheCalendarFolded = false;
    public ImageView btn_ShowFullYearView;
    public ImageView btn_FoldTheCalendar;
    public ImageView btn_Options;

    public TextView txtSelectYearAndMonth;

    //这部分从系统直接获取
    static public int curYear = 0;  //当前年份
    static public int curMonth = 0;  //当前月份
    static public int curDay = 0;  //当前日期
    static public int curWeekNum = 0;  //当前星期几

    //这部分由用户选择
    public int curSelectedYear = 0;  //当前选中的日期的年份
    public int curSelectedMonth = 0;  //当前选中的日期的月份
    public int curSelectedDay = 0;  //当前选中的日期
    public int curSelectedWeekNum = 0;  //当前选中的日期的星期几

    public int curSelectedDateIndex = -1;  //表示当前选中的日期在日历中的下标

    public int curSelectedMonthFirWeek = 0;  //表示当月的 1 号是星期几


    //包含日期选择器和按钮的线性布局
    public LinearLayout LinearLayout_DatePicker;

    //假装阴影的view
    public View View_FakeShadow;

    int shadowDown = 22;  //假阴影向下移动的距离

    int myCalendarTitleHeight = 170; //日历最上面包括月份选择，按钮的布局的高度

    int myCalendarRowWeekTitleHeight = 100; //日历的显示星期的行的高度

    int myCalendarRowHeight = 160; //日历的显示日期的每行的高度



    Dialog dialog = null;
    MonthPicker monthPicker = null;


    //包含每行日期的子线性布局
    public LinearLayout subLinearLayout_TheCalendar[] = new LinearLayout[7];

    //单个日期的线性布局
    public LinearLayout LinearLayout_SingleDate[] = new LinearLayout[49];
    //单个日期的主TextView
    public TextView TextView_SingleDate_Main[] = new TextView[49];
    //单个日期的副TextView
    public TextView TextView_SingleDate_Sub[] = new TextView[49];


    //主要ScrollView
    public ScrollView scrollView_main;

    //天气预报scrollview里的布局
    public LinearLayout LinearLayout_WeatherForecast;

    //备忘录scrollview里的布局
    public LinearLayout LinearLayout_UserBackUpMsg;

    //当选中的日期没有备忘录时显示这个线性布局
    public LinearLayout LinearLayout_NoUserBackUpMsg;

    //包含备忘录的最大的布局
    public LinearLayout LinearLayout_BiggestUserBackUpMsg;

    //包含整个备忘录的JSONObject
    public JSONObject JSON_UserBackUpMsg;

    //包含节日数据的json
    public JSONObject JSON_FestivalInfo;


    private static final int DAY_MAX_BACKUP_COUNT = 100;

    //单个备忘录条目的outer线性布局
    public LinearLayout outerLinearLayout_SingleUserBackUpMsg[] = new LinearLayout[DAY_MAX_BACKUP_COUNT];
    //单个备忘录条目的线性布局
    public LinearLayout LinearLayout_SingleUserBackUpMsg[] = new LinearLayout[DAY_MAX_BACKUP_COUNT];

    public LinearLayout LinearLayout_SingleUserBackUpMsg_Time[] = new LinearLayout[DAY_MAX_BACKUP_COUNT];
    //单个备忘录条目的标题TextView
    public TextView TextView_SingleUserBackUpMsg_Title[] = new TextView[DAY_MAX_BACKUP_COUNT];
    //单个备忘录条目的内容TextView
    public TextView TextView_SingleUserBackUpMsg_Content[] = new TextView[DAY_MAX_BACKUP_COUNT];
    //显示该单个备忘录的时间
    public TextView TextView_SingleUserBackUpMsg_Time[] = new TextView[DAY_MAX_BACKUP_COUNT];

    //添加新的备忘录条目的悬浮按钮
    public FloatingActionButton btn_AddNewUserBackUpMsg;
    //返回当前日期的浮动按钮
    public FloatingActionButton btn_BackToToday;

    //今日备忘录通知
    NotificationManager notificationManager;

    //打出当前日期相关变量的值，调试用
    public void PrintCurDateInfo(){
        System.out.println("------------------------------------");
        System.out.println("curYear: " + curYear);
        System.out.println("curMonth: " + curMonth);
        System.out.println("curDay: " + curDay);
        System.out.println("curWeekNum: " + curWeekNum);
        System.out.println("------------------------------------");
        System.out.println("curSelectedYear: " + curSelectedYear);
        System.out.println("curSelectedMonth: " + curSelectedMonth);
        System.out.println("curSelectedDay: " + curSelectedDay);
        System.out.println("curSelectedWeekNum: " + curSelectedWeekNum);
        System.out.println("------------------------------------");
        System.out.println("curSelectedDateIndex: " + curSelectedDateIndex);
        System.out.println("curSelectedMonthFirWeek: " + curSelectedMonthFirWeek);
        System.out.println("------------------------------------");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //腾讯QQ分享SDK
        Tencent.setIsPermissionGranted(true);

        try {
            ReadOptionsInfoFromRaw();
            ReadFestivalInfoFromRaw();
            ReadUserBackUpMsgFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        GetCurDate();
        InitTheCalendarComponents();
        InitOtherComponents();
        try {
            UpdateTheCalendar();
            ShowTodayNotifications(myCalendarOptions.getIsShowTodayNotification());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        View View_FakeShadow0 = new View(this);
        View_FakeShadow0 = (View) findViewById(R.id.View_FakeShadow0);
        View_FakeShadow0.bringToFront();
    }

    //从raw中读options数据并写入本地文件中
    public void ReadOptionsFromRawAndWriteToLocal() throws JSONException, IOException {
        InputStream input = getResources().openRawResource(R.raw.options);
        Reader reader = new InputStreamReader(input);
        StringBuffer stringBuffer = new StringBuffer();
        char b[] = new char[4096];
        int len = -1;
        try {
            while ((len = reader.read(b))!= -1){
                stringBuffer.append(b);
            }
        }catch (IOException e){
            Log.e("ReadingFile","IOException");
        }
        String String_Options = stringBuffer.toString();
        myCalendarOptions.TurnStringOptions2UnitOptions(String_Options);

        //在本地存储中创建一个新的设置文件并写入
        FileOutputStream fileOutputStream = openFileOutput("Options.json", MODE_PRIVATE);
        fileOutputStream.write(String_Options.getBytes());
        fileOutputStream.close();
    }

    //读取设置数据
    public void ReadOptionsInfoFromRaw() throws JSONException, IOException {
        File f = new File("/data/data/com.example.mycalendar/files/Options.json");
        if(f.exists()){   //存在则直接读取
            FileInputStream fileInputStream = openFileInput("Options.json");
            byte[] bytes_Options = new byte[fileInputStream.available()];
            fileInputStream.read(bytes_Options);
            String String_Options = new String(bytes_Options);
            //不同机型对API的支持不同，需要下面这个判断。。。
            if(String_Options.equals("") || String_Options == null){
                String_Options = "{}";
            }

            //myCalendarOptions.TurnStringOptions2UnitOptions(String_Options);

            //判断更新的设置和本地的设置内容是否相同
            //boolean options_local_equals_new = true;

            JSONObject JSON_LocalOptions = new JSONObject(String_Options);

            ReadOptionsFromRawAndWriteToLocal();
            Gson gson = new Gson();
            String new_String_Options = gson.toJson(myCalendarOptions);
            JSONObject JSON_NewOptions = new JSONObject(new_String_Options);

            if(JSON_NewOptions.names().equals(JSON_LocalOptions.names())){
                //System.out.println("---------------------------------equal-------------------------");
                myCalendarOptions.TurnStringOptions2UnitOptions(String_Options);
            }
            else {
                //System.out.println("---------------------------------notequal-------------------------");
                ReadOptionsFromRawAndWriteToLocal();
            }

            fileInputStream.close();
        }
        else {   //不存在则 先从raw里面读取，再在本地存储创建一个，再把从raw里读取的写进去
            ReadOptionsFromRawAndWriteToLocal();
        }

    }

    //读取节日数据
    public void ReadFestivalInfoFromRaw() throws IOException, JSONException {
        InputStream input = getResources().openRawResource(R.raw.festival);
        Reader reader = new InputStreamReader(input);
        StringBuffer stringBuffer = new StringBuffer();
        char b[] = new char[4096];
        int len = -1;
        try {
            while ((len = reader.read(b))!= -1){
                stringBuffer.append(b);
            }
        }catch (IOException e){
            Log.e("ReadingFile","IOException");
        }
        String String_FestivalInfo = stringBuffer.toString();
        JSON_FestivalInfo = new JSONObject(String_FestivalInfo);
        Log.i("festival", String.valueOf(JSON_FestivalInfo));
    }

    //读取备忘录文件里的值，覆盖当前页面内存中的JSON_UserBackUpMsg
    public void ReadUserBackUpMsgFromFile() throws IOException, JSONException {
        //先判断该文件是否存在
        File f = new File("/data/data/com.example.mycalendar/files/MyCalendarUserBackUpMsg.json");
        if(f.exists()){   //存在则直接读取
            FileInputStream fileInputStream = openFileInput("MyCalendarUserBackUpMsg.json");
            byte[] bytes_UserBackUpMsg = new byte[fileInputStream.available()];
            fileInputStream.read(bytes_UserBackUpMsg);
            String String_UserBackUpMsg = new String(bytes_UserBackUpMsg);
            //不同机型对API的支持不同，需要下面这个判断。。。
            if(String_UserBackUpMsg.equals("") || String_UserBackUpMsg == null){
                String_UserBackUpMsg = "{}";
            }
            JSON_UserBackUpMsg = new JSONObject(String_UserBackUpMsg);
            Log.i("backupmodule", String.valueOf(JSON_UserBackUpMsg));
            fileInputStream.close();
        }
        else {   //不存在则创建一个
            FileOutputStream fileOutputStream = openFileOutput("MyCalendarUserBackUpMsg.json", MODE_APPEND);
            fileOutputStream.close();
            JSON_UserBackUpMsg = new JSONObject();
        }
    }

    //将用户的备忘录内容保存到本地
    public void SaveUserBackUpMsgToLocalFile() throws IOException {
        FileOutputStream fileOutputStream = openFileOutput("MyCalendarUserBackUpMsg.json",MODE_PRIVATE);
        String UserBackUpMsg = String.valueOf(JSON_UserBackUpMsg);
        fileOutputStream.write(UserBackUpMsg.getBytes());
        fileOutputStream.close();
    }

    //更新用户的备忘录模块
    public void UpdateUserBackUpMsgModule() throws JSONException, IOException {
        LinearLayout_UserBackUpMsg.removeAllViews();
        //每个日期最多支持十条备忘录
        //包含整个备忘录的JSONObject  JSON_UserBackUpMsg;
        //单个备忘录条目的outer线性布局  outerLinearLayout_SingleUserBackUpMsg[11]
        //单个备忘录条目的线性布局  LinearLayout_SingleUserBackUpMsg[11]
        //单个备忘录条目的标题TextView  TextView_SingleUserBackUpMsg_Title[11]
        //单个备忘录条目的内容TextView  TextView_SingleUserBackUpMsg_Content[11]

        if(GetBackUpMsgsCount(curSelectedYear, curSelectedMonth, curSelectedDay) == 0){
            LinearLayout_NoUserBackUpMsg.setVisibility(View.VISIBLE);
            LinearLayout_BiggestUserBackUpMsg.setVisibility(View.GONE);
            return;
        }
        LinearLayout_NoUserBackUpMsg.setVisibility(View.GONE);
        LinearLayout_BiggestUserBackUpMsg.setVisibility(View.VISIBLE);

        if(JSON_UserBackUpMsg.has(String.valueOf(curSelectedYear))){  //包含当前选中的年
            if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).has(String.valueOf(curSelectedMonth))){  //包含当前选中的月
                if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).has(String.valueOf(curSelectedDay))){  //包含当前选中的日
                    //选中的当天存在备忘录条目
                    JSONArray JSONArray_curSelectedDaysUserBackUpMsg = JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay));
                    System.out.println(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)));

                    //单个备忘录条目的线性布局的Params
                    LinearLayout.LayoutParams LinearLayout_SingleUserBackUpMsg_Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
                    //循环创建备忘录条目，组装备忘录模块
                    for(int i = 0;i < JSONArray_curSelectedDaysUserBackUpMsg.length();++i){
                        int tmpi = i;

                        TextView curTextView_SingleUserBackUpMsg_Title = new TextView(this);
                        TextView_SingleUserBackUpMsg_Title[i] = curTextView_SingleUserBackUpMsg_Title;
                        TextView_SingleUserBackUpMsg_Title[i].setText("" + String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).get("title")));
                        TextView_SingleUserBackUpMsg_Title[i].setTextSize(30);
                        //TextView_SingleUserBackUpMsg_Title[i].setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                        TextView_SingleUserBackUpMsg_Title[i].setHeight(150);
                        TextView_SingleUserBackUpMsg_Title[i].setEllipsize(TextUtils.TruncateAt.END);
                        TextView_SingleUserBackUpMsg_Title[i].setPadding(20,50,0,0);
                        TextView_SingleUserBackUpMsg_Title[i].setTextColor(getResources().getColor(R.color.white));
                        TextView_SingleUserBackUpMsg_Title[i].setSingleLine(true);
                        //TextView_SingleUserBackUpMsg_Title[i].setBackgroundColor(getResources().getColor(R.color.green));
                        TextView_SingleUserBackUpMsg_Title[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpi));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });
                        //TextView_SingleUserBackUpMsg_Title[i].setBackgroundColor(getResources().getColor(R.color.red));

                        TextView curTextView_SingleUserBackUpMsg_Content = new TextView(this);
                        TextView_SingleUserBackUpMsg_Content[i] = curTextView_SingleUserBackUpMsg_Content;
                        TextView_SingleUserBackUpMsg_Content[i].setText("" + String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).get("content")));
                        TextView_SingleUserBackUpMsg_Content[i].setTextSize(15);
                        //TextView_SingleUserBackUpMsg_Content[i].setWidth(300);
                        TextView_SingleUserBackUpMsg_Content[i].setHeight(150);
                        TextView_SingleUserBackUpMsg_Content[i].setEllipsize(TextUtils.TruncateAt.END);
                        TextView_SingleUserBackUpMsg_Content[i].setPadding(20,10,0,0);
                        TextView_SingleUserBackUpMsg_Content[i].setTextColor(getResources().getColor(R.color.white));
                        TextView_SingleUserBackUpMsg_Content[i].setSingleLine(true);
                        //TextView_SingleUserBackUpMsg_Content[i].setBackgroundColor(getResources().getColor(R.color.blue));
                        TextView_SingleUserBackUpMsg_Content[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpi));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });
                        //TextView_SingleUserBackUpMsg_Content[i].setBackgroundColor(getResources().getColor(R.color.red));


                        //判断该活动是否有开始和结束时间和地址和是否启用铃声
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).has("starttime")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).put("starttime", "00:00");
                            SaveUserBackUpMsgToLocalFile();
                        }
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).has("endtime")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).put("endtime", "00:00");
                            SaveUserBackUpMsgToLocalFile();
                        }
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).has("location")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).put("location", "地球");
                            SaveUserBackUpMsgToLocalFile();
                        }
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).has("activatering")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).put("activatering", false);
                            SaveUserBackUpMsgToLocalFile();
                        }
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).has("activatevibrate")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).put("activatevibrate", false);
                            SaveUserBackUpMsgToLocalFile();
                        }



                        TextView curTextView_SingleUserBackUpMsg_Time = new TextView(this);
                        TextView_SingleUserBackUpMsg_Time[i] = curTextView_SingleUserBackUpMsg_Time;
                        TextView_SingleUserBackUpMsg_Time[i].setText(
                                String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).get("starttime"))
                                + '\n' + "-" + '\n' +
                                        String.valueOf(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).getJSONObject(i).get("endtime"))
                        );
                        TextView_SingleUserBackUpMsg_Time[i].setTextSize(20);
                        TextView_SingleUserBackUpMsg_Time[i].setWidth(300);
                        TextView_SingleUserBackUpMsg_Time[i].setHeight(300);
                        TextView_SingleUserBackUpMsg_Time[i].setGravity(Gravity.CENTER);
                        TextView_SingleUserBackUpMsg_Time[i].setEllipsize(TextUtils.TruncateAt.END);
                        //TextView_SingleUserBackUpMsg_Time[i].setPadding(20,10,0,0);
                        TextView_SingleUserBackUpMsg_Time[i].setTextColor(getResources().getColor(R.color.white));
                        //TextView_SingleUserBackUpMsg_Time[i].setBackgroundColor(getResources().getColor(R.color.red));
                        TextView_SingleUserBackUpMsg_Time[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpi));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });

                        LinearLayout curLinearLayout_SingleUserBackUpMsg_Time = new LinearLayout(this);
                        LinearLayout_SingleUserBackUpMsg_Time[i] = curLinearLayout_SingleUserBackUpMsg_Time;
                        LinearLayout_SingleUserBackUpMsg_Time[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        LinearLayout_SingleUserBackUpMsg_Time[i].setGravity(Gravity.END);
                        //LinearLayout_SingleUserBackUpMsg_Time[i].setBackgroundColor(getResources().getColor(R.color.yellow));
                        LinearLayout_SingleUserBackUpMsg_Time[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpi));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });


                        LinearLayout curLinearLayout_SingleUserBackUpMsg = new LinearLayout(this);
                        LinearLayout_SingleUserBackUpMsg[i] = curLinearLayout_SingleUserBackUpMsg;
                        //LinearLayout_SingleUserBackUpMsg[i].setLayoutParams(LinearLayout_SingleUserBackUpMsg_Params);
                        LinearLayout_SingleUserBackUpMsg[i].setLayoutParams(new ViewGroup.LayoutParams(700, 300));
                        //LinearLayout_SingleUserBackUpMsg[i].setBackgroundColor(getResources().getColor(R.color.light_blue));
                        LinearLayout_SingleUserBackUpMsg[i].setOrientation(LinearLayout.VERTICAL);

                        //LinearLayout_SingleUserBackUpMsg[i].setBackground(getDrawable(R.drawable.cardborder_light_blue));
                        //LinearLayout_SingleUserBackUpMsg[i].setBackground(getDrawable(R.drawable.userbackupmsgcard_selector));

                        LinearLayout_SingleUserBackUpMsg[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpi));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });



                        LinearLayout curOuterLinearLayout_SingleUserBackUpMsg = new LinearLayout(this);
                        outerLinearLayout_SingleUserBackUpMsg[i] = curOuterLinearLayout_SingleUserBackUpMsg;
                        outerLinearLayout_SingleUserBackUpMsg[i].setLayoutParams(LinearLayout_SingleUserBackUpMsg_Params);
                        //outerLinearLayout_SingleUserBackUpMsg[i].setBackgroundColor(getResources().getColor(R.color.light_blue));
                        outerLinearLayout_SingleUserBackUpMsg[i].setOrientation(LinearLayout.HORIZONTAL);
                        outerLinearLayout_SingleUserBackUpMsg[i].setBackground(getDrawable(R.drawable.cardborder_light_blue));

                        LinearLayout_SingleUserBackUpMsg[i].addView(TextView_SingleUserBackUpMsg_Title[i]);
                        LinearLayout_SingleUserBackUpMsg[i].addView(TextView_SingleUserBackUpMsg_Content[i]);

                        LinearLayout_SingleUserBackUpMsg_Time[i].addView(TextView_SingleUserBackUpMsg_Time[i]);

                        View spaceView = new View(this);
                        spaceView.setLayoutParams(new ViewGroup.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));

                        outerLinearLayout_SingleUserBackUpMsg[i].addView(spaceView);
                        outerLinearLayout_SingleUserBackUpMsg[i].addView(LinearLayout_SingleUserBackUpMsg[i]);
                        outerLinearLayout_SingleUserBackUpMsg[i].addView(LinearLayout_SingleUserBackUpMsg_Time[i]);



                        View spaceView__ = new View(this);
                        spaceView__.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));

                        spaceView__.setBackgroundColor(getResources().getColor(R.color.dividespaceviewcolor));

                        LinearLayout_UserBackUpMsg.addView(outerLinearLayout_SingleUserBackUpMsg[i]);
                        LinearLayout_UserBackUpMsg.addView(spaceView__);
                    }

                }
                else {
                    Log.i("backupmodule", "no such day");
                }
            }
            else {
                Log.i("backupmodule", "no such month");
            }
        }
        else {
            Log.i("backupmodule", "no such year");
        }

        View_FakeShadow.bringToFront();  //将假阴影放在最上层
    }

    //获取当前日期
    public void GetCurDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String curTime = simpleDateFormat.format(date);
        curYear = Integer.parseInt(curTime.substring(0, 4));
        curMonth = Integer.parseInt(curTime.substring(5, 7));
        curDay = Integer.parseInt(curTime.substring(8, 10));
        curSelectedYear = curYear;
        curSelectedMonth = curMonth;
        curSelectedDay = curDay;
        CalcWeekNum();

        System.out.println("curDate: " + curYear + "/" + curMonth + "/" + curDay);
        System.out.println("thismonthfir: " + curSelectedMonthFirWeek + "  curweek: " + curWeekNum);
    }

    //计算对应日期是周几，一个是本月的 1 号是周几，一个是当前日期是周几
    public void CalcWeekNum(){
        //基姆拉尔森计算公式: Week =  (day + 2 * month + 3 * (month + 1）/ 5 + year + year / 4 - year / 100 + y / 400 )  %  7 + 1
        int calcY = curSelectedYear;
        int calcM = curSelectedMonth;
        int calcD = curSelectedDay;

        if(calcM == 1 || calcM == 2){
            calcM = calcM + 12;
            calcY--;
        }
        curSelectedMonthFirWeek = (1 + 2 * calcM + 3 * (calcM + 1) / 5 + calcY + calcY / 4 - calcY / 100 + calcY / 400 )  %  7 + 1;
        curWeekNum = (calcD + 2 * calcM + 3 * (calcM + 1) / 5 + calcY + calcY / 4 - calcY / 100 + calcY / 400 )  %  7 + 1;
        curSelectedWeekNum = curWeekNum;
        curSelectedDateIndex = curSelectedMonthFirWeek + (curSelectedDay - 1);
    }

    //用于app启动时和更改所选月份时更新日历
    public void UpdateTheCalendar() throws JSONException, IOException {
        PrintCurDateInfo();
        //前42个为日期
        //单个日期的主TextView  TextView_SingleDate_Main[49]
        //单个日期的副TextView  TextView_SingleDate_Sub[49]

        //先去除无关信息
        for(int i = 0;i < 42;++i){
            TextView_SingleDate_Main[i].setText("");
            TextView_SingleDate_Sub[i].setText("");
        }
        //有时候，一个月会横跨6周，有时候只有4周，因此需要管理一下显示日历的行数
        int maxIndex = 0;
        //声明一个数组表示当前选中月有几个星期几了，week[0]表示已有星期日的数量
        int[] week = new int[7];
        for(int i = 0;i < 7;++i) week[i] = 0;
        for(int i = 1;i <= GetSelectedMonthDaysCount();++i){
            if(curSelectedYear == curYear && curSelectedMonth == curMonth && i == curDay){
                TextView_SingleDate_Main[i + curSelectedMonthFirWeek - 1].setTextColor(getResources().getColor(R.color.curdaycolor));
                TextView_SingleDate_Main[i + curSelectedMonthFirWeek - 1].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
            }
            else {
                TextView_SingleDate_Main[i + curSelectedMonthFirWeek - 1].setTextColor(getResources().getColor(R.color.white));
                TextView_SingleDate_Main[i + curSelectedMonthFirWeek - 1].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            TextView_SingleDate_Main[i + curSelectedMonthFirWeek - 1].setText(i + ""); //写上日期

            week[(i + curSelectedMonthFirWeek - 1) % 7]++;

            //写上节日
            if(myCalendarOptions.getIsShowFestival()){
                //明确几月几号的节日
                if(JSON_FestivalInfo.getJSONObject(String.valueOf(curSelectedMonth)).has(String.valueOf(i))){
                    TextView_SingleDate_Sub[i + curSelectedMonthFirWeek - 1].setText((CharSequence) JSON_FestivalInfo.getJSONObject(String.valueOf(curSelectedMonth)).get(String.valueOf(i)));
                }
                //第几个星期几的节日
                if(JSON_FestivalInfo.getJSONObject(String.valueOf(curSelectedMonth)).has("#" + String.valueOf(week[(i + curSelectedMonthFirWeek - 1) % 7]) + String.valueOf((i + curSelectedMonthFirWeek - 1) % 7))){
                    TextView_SingleDate_Sub[i + curSelectedMonthFirWeek - 1].setText(TextView_SingleDate_Sub[i + curSelectedMonthFirWeek - 1].getText() + String.valueOf(JSON_FestivalInfo.getJSONObject(String.valueOf(curSelectedMonth)).get("#" + String.valueOf(week[(i + curSelectedMonthFirWeek - 1) % 7]) + String.valueOf((i + curSelectedMonthFirWeek - 1) % 7))));
                }
            }

            //若该日期下有备忘条目，则加上一个*
            if(JSON_UserBackUpMsg.has(String.valueOf(curSelectedYear))){  //包含当前选中的年
                if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).has(String.valueOf(curSelectedMonth))) {  //包含当前选中的月
                    if (JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).has(String.valueOf(i))) {  //包含当前选中的日
                        if (JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(i)).length() > 0) {
                            TextView_SingleDate_Sub[i + curSelectedMonthFirWeek - 1].setText(TextView_SingleDate_Sub[i + curSelectedMonthFirWeek - 1].getText() + "*");
                        }
                    }
                }
            }

            maxIndex = i + curSelectedMonthFirWeek - 1;
        }
        if(!isTheCalendarFolded){
            if(maxIndex <= 27){
                subLinearLayout_TheCalendar[1].setVisibility(View.VISIBLE);
                subLinearLayout_TheCalendar[5].setVisibility(View.GONE);
                subLinearLayout_TheCalendar[6].setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
                lp.setMargins(0, (myCalendarTitleHeight + myCalendarRowWeekTitleHeight + myCalendarRowHeight * 4) + shadowDown, 0, 0);
                //lp.setMargins(0, 910 + shadowDown, 0, 0);
                View_FakeShadow.setLayoutParams(lp);
                //View_FakeShadow.setPaddingRelative(0,910,0,0);
            }
            else if(maxIndex <= 34){
                subLinearLayout_TheCalendar[1].setVisibility(View.VISIBLE);
                subLinearLayout_TheCalendar[5].setVisibility(View.VISIBLE);
                subLinearLayout_TheCalendar[6].setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
                lp.setMargins(0, (myCalendarTitleHeight + myCalendarRowWeekTitleHeight + myCalendarRowHeight * 5) + shadowDown, 0, 0);
                //lp.setMargins(0, 1070 + shadowDown, 0, 0);
                View_FakeShadow.setLayoutParams(lp);
                //View_FakeShadow.setPaddingRelative(0,1070,0,0);
            }
            else {
                subLinearLayout_TheCalendar[1].setVisibility(View.VISIBLE);
                subLinearLayout_TheCalendar[5].setVisibility(View.VISIBLE);
                subLinearLayout_TheCalendar[6].setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
                lp.setMargins(0, (myCalendarTitleHeight + myCalendarRowWeekTitleHeight + myCalendarRowHeight * 6) + shadowDown, 0, 0);
                //lp.setMargins(0, 1230 + shadowDown, 0, 0);
                View_FakeShadow.setLayoutParams(lp);
                //View_FakeShadow.setPaddingRelative(0,1230,0,0);
            }
            if(curSelectedMonthFirWeek == 7){
                subLinearLayout_TheCalendar[1].setVisibility(View.GONE);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
                lp.setMargins(0, (myCalendarTitleHeight + myCalendarRowWeekTitleHeight + myCalendarRowHeight * 5) + shadowDown, 0, 0);
                //lp.setMargins(0, 1070 + shadowDown, 0, 0);
                View_FakeShadow.setLayoutParams(lp);
            }
        }
        //更改选中的日期的背景颜色
        for(int i = 0;i < 42;++i){
            if(i == curSelectedDateIndex) LinearLayout_SingleDate[i].setBackgroundColor(getResources().getColor(R.color.light_blue));
            else LinearLayout_SingleDate[i].setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        UpdateUserBackUpMsgModule();

        //如果当前选中的不是今天，则显示定位按钮
        if(!(curSelectedYear == curYear && curSelectedMonth == curMonth && curSelectedDay == curDay)){
            btn_BackToToday.setVisibility(View.VISIBLE);
        }
        else {
            btn_BackToToday.setVisibility(View.GONE);
        }
    }

    //获取当前所选中的月份的天数
    public int GetSelectedMonthDaysCount(){
        switch (curSelectedMonth){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12: {
                return 31;
            }
            case 4:
            case 6:
            case 9:
            case 11: {
                return 30;
            }
            case 2:{
                if(curSelectedYear % 4 == 0 && curSelectedYear % 100 != 0 || curSelectedYear % 400 == 0){
                    return 29;
                }
                else return 28;
            }
        }
        return 0;
    }

    //将数字表示的星期几转换为汉字
    public String TurnWeekNum2Chinese(int i){
        switch (i){
            case 0:{ return "日"; }
            case 1:{ return "一"; }
            case 2:{ return "二"; }
            case 3:{ return "三"; }
            case 4:{ return "四"; }
            case 5:{ return "五"; }
            case 6:{ return "六"; }
        }
        return "err";
    }


    //更新其他控件演示等
    public void UpdateOtherComponents(){
        //浮动按钮透明度
        //btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.transparent));
        if(myCalendarOptions.getIsFABTransparent()){
            btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.transparent));
            btn_BackToToday.setColorNormal(getResources().getColor(R.color.transparent));
            btn_BackToToday.setIcon(R.drawable.baseline_gps_fixed_24_white);
        }
        else {
            btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.plusbtn_light_blue));
            btn_BackToToday.setColorNormal(getResources().getColor(R.color.white));
            btn_BackToToday.setIcon(R.drawable.baseline_gps_fixed_24);
        }
    }

    //初始化 其他控件 包括单击事件
    public void InitOtherComponents(){
        LinearLayout_DatePicker = (LinearLayout) findViewById(R.id.LinearLayout_DatePicker);
        View_FakeShadow = (View) findViewById(R.id.View_FakeShadow);
        txtSelectYearAndMonth = (TextView) findViewById(R.id.txtSelectYearAndMonth);
        btn_ShowFullYearView = (ImageView) findViewById(R.id.btn_ShowFullYearView);
        btn_FoldTheCalendar = (ImageView) findViewById(R.id.btn_FoldTheCalendar);
        btn_Options = (ImageView) findViewById(R.id.btn_Options);
        scrollView_main = (ScrollView) findViewById(R.id.scrollView_main);
        LinearLayout_WeatherForecast = (LinearLayout) findViewById(R.id.LinearLayout_WeatherForecast);
        LinearLayout_UserBackUpMsg = (LinearLayout) findViewById(R.id.LinearLayout_UserBackUpMsg);
        btn_AddNewUserBackUpMsg = (FloatingActionButton) findViewById(R.id.btn_AddNewUserBackUpMsg);
        btn_BackToToday = (FloatingActionButton) findViewById(R.id.btn_BackToToday);
        LinearLayout_NoUserBackUpMsg = (LinearLayout) findViewById(R.id.LinearLayout_NoUserBackUpMsg);
        LinearLayout_BiggestUserBackUpMsg = (LinearLayout) findViewById(R.id.LinearLayout_BiggestUserBackUpMsg);


        txtSelectYearAndMonth.setOnClickListener(this);
        btn_ShowFullYearView.setOnClickListener(this);
        btn_FoldTheCalendar.setOnClickListener(this);
        btn_Options.setOnClickListener(this);
        btn_AddNewUserBackUpMsg.setOnClickListener(this);
        btn_BackToToday.setOnClickListener(this);
        //隐藏天气预报模块
        LinearLayout_WeatherForecast.setVisibility(View.GONE);


        txtSelectYearAndMonth.setText("" + curYear + " 年 " + curMonth + " 月");
        LinearLayout.LayoutParams LinearLayout_DatePicker_Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, myCalendarTitleHeight);
        LinearLayout_DatePicker.setLayoutParams(LinearLayout_DatePicker_Params);

        //浮动按钮透明度
        //btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.transparent));
        if(myCalendarOptions.getIsFABTransparent()){
            btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.transparent));
        }
        else {
            btn_AddNewUserBackUpMsg.setColorNormal(getResources().getColor(R.color.plusbtn_light_blue));
        }
        UpdateOtherComponents();
    }

    //动态生成显示每月日历的控件
    //5 行（5 个LinearLayout，内部横向排列）
    //每行 7 个组件，每个组件对于一个日期
    public void InitTheCalendarComponents(){
        //包含整个每月日历的总的线性布局
        LinearLayout LinearLayout_TheCalendar = (LinearLayout) findViewById(R.id.LinearLayout_TheCalendar);

        //每行的线性布局的宽高 和 spaceView的宽高
        LinearLayout.LayoutParams subLinearLayout_TheCalendar_Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, myCalendarRowHeight);
        //LinearLayout.LayoutParams spaceView01_Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10);

        for(int i = 0;i < 7;++i){
            //当场新建LinearLayout并将其赋给 包含每行日期的子线性布局（定义在最顶上）
            LinearLayout curSubLinearLayout_TheCalendar = new LinearLayout(this);
            subLinearLayout_TheCalendar[i] = curSubLinearLayout_TheCalendar;
            subLinearLayout_TheCalendar[i].setOrientation(LinearLayout.HORIZONTAL);
            subLinearLayout_TheCalendar[i].setLayoutParams(subLinearLayout_TheCalendar_Params);
            if(i > 0){
                subLinearLayout_TheCalendar[i].setLayoutParams(subLinearLayout_TheCalendar_Params);

                subLinearLayout_TheCalendar[i].setBackgroundColor(getResources().getColor(R.color.transparent));

            }
            else {
                subLinearLayout_TheCalendar[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, myCalendarRowWeekTitleHeight));
                subLinearLayout_TheCalendar[i].setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            subLinearLayout_TheCalendar[i].setGravity(Gravity.CENTER);

            //间隔空白spaceView
//            View spaceView = new View(this);
//            spaceView.setLayoutParams(spaceView01_Params);

            //动态添加 包含每行日期的子线性布局 和 spaceView 控件
            LinearLayout_TheCalendar.addView(subLinearLayout_TheCalendar[i]);
            //LinearLayout_TheCalendar.addView(spaceView);
        }

        LinearLayout.LayoutParams LinearLayout_SingleDate_Params = new LinearLayout.LayoutParams(145, LinearLayout.LayoutParams.MATCH_PARENT);
        //LinearLayout.LayoutParams spaceView02_Params = new LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.MATCH_PARENT);
        for(int i = 0;i < 49;++i){
            if(i >= 0 && i < 42){  //这里是给单个日期编码
                int tmpi = i;
                LinearLayout curLinearLayout_SingleDate = new LinearLayout(this);
                TextView curTextView_SingleDate_Main = new TextView(this);
                TextView curTextView_SingleDate_Sub = new TextView(this);
//                View spaceView = new View(this);
//                spaceView.setLayoutParams(spaceView02_Params);
//                spaceView.setBackgroundColor(getResources().getColor(R.color.dividespaceviewcolor));

                LinearLayout_SingleDate[i] = curLinearLayout_SingleDate;
                TextView_SingleDate_Main[i] = curTextView_SingleDate_Main;
                TextView_SingleDate_Sub[i] = curTextView_SingleDate_Sub;

                LinearLayout_SingleDate[i].setOrientation(LinearLayout.VERTICAL);
                LinearLayout_SingleDate[i].setLayoutParams(LinearLayout_SingleDate_Params);
                LinearLayout_SingleDate[i].setGravity(Gravity.CENTER);
                if(i == curSelectedDateIndex) LinearLayout_SingleDate[i].setBackgroundColor(getResources().getColor(R.color.light_blue));
                LinearLayout_SingleDate[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(tmpi >= curSelectedMonthFirWeek && tmpi < curSelectedMonthFirWeek + GetSelectedMonthDaysCount()){
                                curSelectedDateIndex = tmpi;
                                curSelectedDay = Integer.parseInt((String) TextView_SingleDate_Main[tmpi].getText());
                                curSelectedWeekNum = (curSelectedDay + 2 * curSelectedMonth + 3 * (curSelectedMonth + 1) / 5 + curSelectedYear + curSelectedYear / 4 - curSelectedYear / 100 + curSelectedYear / 400 )  %  7 + 1;
                                UpdateTheCalendar();
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                TextView_SingleDate_Main[i].setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                TextView_SingleDate_Main[i].setHeight(75);
                TextView_SingleDate_Main[i].setText(String.valueOf(i));
                TextView_SingleDate_Main[i].setGravity(Gravity.CENTER);
                TextView_SingleDate_Main[i].setTextSize(23);
                TextView_SingleDate_Main[i].setTextColor(getResources().getColor(R.color.white));
                TextView_SingleDate_Main[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(tmpi >= curSelectedMonthFirWeek && tmpi < curSelectedMonthFirWeek + GetSelectedMonthDaysCount()){
                                curSelectedDateIndex = tmpi;
                                curSelectedDay = Integer.parseInt((String) TextView_SingleDate_Main[tmpi].getText());
                                curSelectedWeekNum = (curSelectedDay + 2 * curSelectedMonth + 3 * (curSelectedMonth + 1) / 5 + curSelectedYear + curSelectedYear / 4 - curSelectedYear / 100 + curSelectedYear / 400 )  %  7 + 1;
                                UpdateTheCalendar();
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                TextView_SingleDate_Sub[i].setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                TextView_SingleDate_Sub[i].setHeight(50);
                TextView_SingleDate_Sub[i].setText(String.valueOf(i));
                TextView_SingleDate_Sub[i].setGravity(Gravity.CENTER);
                TextView_SingleDate_Sub[i].setTextColor(getResources().getColor(R.color.white));
                TextView_SingleDate_Sub[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(tmpi >= curSelectedMonthFirWeek && tmpi < curSelectedMonthFirWeek + GetSelectedMonthDaysCount()){
                                curSelectedDateIndex = tmpi;
                                curSelectedDay = Integer.parseInt((String) TextView_SingleDate_Main[tmpi].getText());
                                curSelectedWeekNum = (curSelectedDay + 2 * curSelectedMonth + 3 * (curSelectedMonth + 1) / 5 + curSelectedYear + curSelectedYear / 4 - curSelectedYear / 100 + curSelectedYear / 400 )  %  7 + 1;
                                UpdateTheCalendar();
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                LinearLayout_SingleDate[i].addView(TextView_SingleDate_Main[i]);
                LinearLayout_SingleDate[i].addView(TextView_SingleDate_Sub[i]);
//                if(i % 7 == 0){
//                    View spaceView_ = new View(this);
//                    spaceView_.setLayoutParams(spaceView02_Params);
//                    spaceView_.setBackgroundColor(getResources().getColor(R.color.dividespaceviewcolor));
//                    subLinearLayout_TheCalendar[i / 7 + 1].addView(spaceView_);
//                }
                subLinearLayout_TheCalendar[i / 7 + 1].addView(LinearLayout_SingleDate[i]);
                //subLinearLayout_TheCalendar[i / 7 + 1].addView(spaceView);
            }
            else{ //这里是给星期几的标题编码
                LinearLayout curLinearLayout_SingleDate = new LinearLayout(this);
                TextView curTextView_SingleDate_Main = new TextView(this);
//                View spaceView = new View(this);
//                spaceView.setLayoutParams(spaceView02_Params);
//                spaceView.setBackgroundColor(getResources().getColor(R.color.dividespaceviewcolor));

                LinearLayout_SingleDate[i] = curLinearLayout_SingleDate;
                TextView_SingleDate_Main[i] = curTextView_SingleDate_Main;

                LinearLayout_SingleDate[i].setOrientation(LinearLayout.VERTICAL);
                LinearLayout_SingleDate[i].setLayoutParams(LinearLayout_SingleDate_Params);
                LinearLayout_SingleDate[i].setGravity(Gravity.CENTER);
                TextView_SingleDate_Main[i].setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                TextView_SingleDate_Main[i].setHeight(100);
                TextView_SingleDate_Main[i].setText(TurnWeekNum2Chinese(i - 42));
                TextView_SingleDate_Main[i].setGravity(Gravity.CENTER);
                TextView_SingleDate_Main[i].setTextSize(20);
                TextView_SingleDate_Main[i].setTextColor(getResources().getColor(R.color.white));

                LinearLayout_SingleDate[i].addView(TextView_SingleDate_Main[i]);
//                if(i % 7 == 0){
//                    View spaceView_ = new View(this);
//                    spaceView_.setLayoutParams(spaceView02_Params);
//                    spaceView_.setBackgroundColor(getResources().getColor(R.color.dividespaceviewcolor));
//                    subLinearLayout_TheCalendar[0].addView(spaceView_);
//                }
                subLinearLayout_TheCalendar[0].addView(LinearLayout_SingleDate[i]);
                //subLinearLayout_TheCalendar[0].addView(spaceView);
            }
        }
    }


    //年月选择
    private void onDataClick(){
        if(dialog == null){
            System.out.println("new dialog: " + curSelectedYear + "/" + curSelectedMonth + "/" + curSelectedDay);
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.date_year_month_view);
            monthPicker = (MonthPicker) dialog.findViewById(R.id.month_picker);
        }

        Button btn_left = (Button) dialog.findViewById(R.id.btn_left);
        Button btn_right = (Button) dialog.findViewById(R.id.btn_right);

        monthPicker.updateDate(curSelectedYear, curSelectedMonth - 1, curSelectedDay);

        //tv_body_msg.setText(R.string.dialog_msg);
        dialog.setCancelable(true);
        //点击左侧按钮
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //点击右键
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //txtSelectYearAndMonth.setText(String.format("%d.%02d", monthPicker.getYear(), monthPicker.getMonth() + 1));
                txtSelectYearAndMonth.setText("" + monthPicker.getYear() + " 年 " + (monthPicker.getMonth() + 1)+ " 月");
                curSelectedYear = monthPicker.getYear();
                curSelectedMonth = monthPicker.getMonth() + 1;
                if(curSelectedYear == curYear && curSelectedMonth == curMonth){
                    curSelectedDay = curDay;
                }
                else {
                    curSelectedDay = 1;
                }
                CalcWeekNum(); //计算对应日期是周几
                try {
                    UpdateTheCalendar(); //更新日历
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //跳转到添加新的备忘录条目
    public void GoToAddNewUserBackUpMsg(){
        myCalendarOptions.setPendingIntentRequestCodeCount(myCalendarOptions.getPendingIntentRequestCodeCount() + 1);
        Intent gotoAddNewUserBackUpMsg = new Intent(MainActivity.this, Activity_EditUserBackUpMsg.class);
        gotoAddNewUserBackUpMsg.putExtra("editMode", "-1");
        gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curSelectedYear));
        gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curSelectedMonth));
        gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curSelectedDay));
        gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
        gotoAddNewUserBackUpMsg.putExtra("SourcePage", "MAIN");



        startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
        //startActivity(gotoAddNewUserBackUpMsg);
    }

    //返回参数所指定的日期的备忘录条目数量
    public int GetBackUpMsgsCount(int targetYear, int targetMonth, int targetDay) throws JSONException {
        if(JSON_UserBackUpMsg.has(String.valueOf(targetYear))){  //包含当前选中的年
            if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(targetYear)).has(String.valueOf(targetMonth))){  //包含当前选中的月
                if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(targetYear)).getJSONObject(String.valueOf(targetMonth)).has(String.valueOf(targetDay))){  //包含当前选中的日
                    return JSON_UserBackUpMsg.getJSONObject(String.valueOf(targetYear)).getJSONObject(String.valueOf(targetMonth)).getJSONArray(String.valueOf(targetDay)).length();
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    //控制是否显示今日的备忘录通知
    public void ShowTodayNotifications(boolean isShowTodayNotifications) throws JSONException {
        //isShowTodayNotifications 为 true 则显示今日通知, isShowTodayNotifications 为 false 则不显示今日通知
        if(isShowTodayNotifications){  //显示今日通知
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            System.out.println("show notification");

            // Android 8.0之后，需要手动添加NotifacationChannel实现，否则log会有如下提示：
            // D/skia: --- Failed to create image decoder with message 'unimplemented'
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("001", "channel_TodayMainNotification", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setSound(null, null);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

            //不知道为什么，通知的大图标不显示
            Notification notification = new NotificationCompat.Builder(this, "001")
                    .setContentTitle("今日您有 " + GetBackUpMsgsCount(curYear, curMonth, curDay) + " 条日程")
                    .setContentText("点击以跳转到应用界面")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.mycalendar_white)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.mycalendar_white))
                    .setOngoing(true)
                    .setSound(null)
                    .setContentIntent(pi)
                    .build();

            notificationManager.notify(1, notification);
        }
        else {  //不显示今日通知
            if(notificationManager != null) notificationManager.cancel(1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDITBACKUPMSG:{
                //标志
                if (resultCode == RESULT_OK) {
                    try {
                        JSON_UserBackUpMsg = new JSONObject(data.getStringExtra("TheUserBackUpMsg")) ;
                        UpdateUserBackUpMsgModule();
                        UpdateTheCalendar();
                        ShowTodayNotifications(myCalendarOptions.getIsShowTodayNotification());
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case REQUEST_CODE_OPTIONS:{
                if (resultCode == RESULT_OK) {
                    try {
                        //JSONObject JSON_Options = new JSONObject(data.getStringExtra("String_CurOptions"));
                        ReadUserBackUpMsgFromFile();
                        myCalendarOptions.TurnStringOptions2UnitOptions(data.getStringExtra("String_CurOptions"));
                        //System.out.println("new options" + data.getStringExtra("String_CurOptions"));
                        UpdateTheCalendar();
                        UpdateOtherComponents();
                        ShowTodayNotifications(myCalendarOptions.getIsShowTodayNotification());
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case REQUEST_CODE_FULLYEARCALENDAR:{  //从全年日历页面返回
                if(resultCode == RESULT_OK){
                    int newYear = data.getIntExtra("newYear", curYear);
                    int newMonth = data.getIntExtra("newMonth", curMonth);

                    if(!(curSelectedYear == newYear && curSelectedMonth == newMonth)){
                        curSelectedYear = newYear;
                        curSelectedMonth = newMonth;
                        if (curYear == newYear && curMonth == newMonth) {
                            curSelectedDay = curDay;
                        }
                        else {
                            curSelectedDay = 1;
                        }
                    }



                    try {
                        CalcWeekNum();
                        UpdateTheCalendar();

                        if(monthPicker != null){
                            monthPicker.updateDate(curSelectedYear, curSelectedMonth, curSelectedDay);
                        }
                        txtSelectYearAndMonth.setText("" + curSelectedYear + " 年 " + curSelectedMonth + " 月");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                break;
            }
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtSelectYearAndMonth:{  //弹出年月选择框
                onDataClick();
                //System.out.println("select month");
                break;
            }
            case R.id.btn_ShowFullYearView:{  //跳转到显示全年日历
                Toast.makeText(MainActivity.this, "全年日历", Toast.LENGTH_SHORT).show();

                //starActivityForResult()
                //传过去 curSelectedYear, curSelectedMonth, curSelectedDay
                //传回来 newCurSelectedYear, newCurSelectedMonth, newCurSelectedDay = 1
                //可以用 monthPicker.updateDate() 来更新 monthPicker
                Intent gotoFullYearCalendar = new Intent(MainActivity.this, Activity_FullYearCalendar.class);
                gotoFullYearCalendar.putExtra("curSelectedYear", curSelectedYear);
                gotoFullYearCalendar.putExtra("curSelectedMonth", curSelectedMonth);
                gotoFullYearCalendar.putExtra("curSelectedDay", curSelectedDay);
                gotoFullYearCalendar.putExtra("curYear", curYear);
                gotoFullYearCalendar.putExtra("curMonth", curMonth);
                gotoFullYearCalendar.putExtra("curDay", curDay);
                gotoFullYearCalendar.putExtra("userBackUpMsg", String.valueOf(JSON_UserBackUpMsg));

                startActivityForResult(gotoFullYearCalendar, REQUEST_CODE_FULLYEARCALENDAR);

                break;
            }
            case R.id.btn_FoldTheCalendar:{  //折叠日历
                if(isTheCalendarFolded){  //当前已折叠
                    for(int i = 1;i < 7;++i){
                        subLinearLayout_TheCalendar[i].setVisibility(View.VISIBLE);
                    }
                    isTheCalendarFolded = !isTheCalendarFolded;
                    try {
                        UpdateTheCalendar();
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    btn_FoldTheCalendar.setImageResource(R.drawable.baseline_keyboard_arrow_up_24);
                }
                else {  //当前未折叠
                    for(int i = 1;i < 7;++i){
                        if(i != curSelectedDateIndex / 7 + 1) subLinearLayout_TheCalendar[i].setVisibility(View.GONE);
                    }
                    isTheCalendarFolded = !isTheCalendarFolded;
                    btn_FoldTheCalendar.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
                    lp.setMargins(0, (myCalendarTitleHeight + myCalendarRowWeekTitleHeight + myCalendarRowHeight * 1) + shadowDown, 0, 0);
                    View_FakeShadow.setLayoutParams(lp);
                }
                break;
            }
            case R.id.btn_Options:{  //跳转到设置
                //Toast.makeText(MainActivity.this, "Options", Toast.LENGTH_SHORT).show();

                //实体类转json字符串
                Gson gson = new Gson();
                String String_Options = gson.toJson(myCalendarOptions);

                Intent gotoOptions = new Intent(MainActivity.this, Activity_Options.class);
                gotoOptions.putExtra("String_CurOptions", String_Options);
                startActivityForResult(gotoOptions, REQUEST_CODE_OPTIONS);
                break;
            }
            case R.id.btn_AddNewUserBackUpMsg:{  //跳转到添加新的备忘录条目
                try {
                    if(JSON_UserBackUpMsg.has(String.valueOf(curSelectedYear))){  //包含当前选中的年
                        if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).has(String.valueOf(curSelectedMonth))){  //包含当前选中的月
                            if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).has(String.valueOf(curSelectedDay))){  //包含当前选中的日
                                if(JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(curSelectedMonth)).getJSONArray(String.valueOf(curSelectedDay)).length() < DAY_MAX_BACKUP_COUNT){
                                    GoToAddNewUserBackUpMsg();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "该天日程条数已达上限（DAY_MAX_BACKUP_COUNT）", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                GoToAddNewUserBackUpMsg();
                            }
                        }
                        else {
                            GoToAddNewUserBackUpMsg();
                        }
                    }
                    else {
                        GoToAddNewUserBackUpMsg();
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case R.id.btn_BackToToday:{  //跳转到今天
                curSelectedYear = curYear;
                curSelectedMonth = curMonth;
                curSelectedDay = curDay;

                txtSelectYearAndMonth.setText("" + curSelectedYear + " 年 " + curSelectedMonth + " 月");

                try {
                    CalcWeekNum();
                    UpdateTheCalendar();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}