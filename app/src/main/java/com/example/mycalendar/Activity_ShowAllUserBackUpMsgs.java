package com.example.mycalendar;

import static com.example.mycalendar.MainActivity.REQUEST_CODE_EDITBACKUPMSG;
import static com.example.mycalendar.MainActivity.curDay;
import static com.example.mycalendar.MainActivity.curMonth;
import static com.example.mycalendar.MainActivity.curYear;
import static com.example.mycalendar.MainActivity.myCalendarOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Activity_ShowAllUserBackUpMsgs extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    //所有用户备忘录条目上限为10000条
    private static final int ALL_USER_BACKUPMSGS_COUNT = 10000;
    LinearLayout LinearLayout_AllUserBackUpMsgs;

    NotificationManager notificationManager;

    public LinearLayout LinearLayout_SingleUserBackUpMsg[] = new LinearLayout[ALL_USER_BACKUPMSGS_COUNT];
    //单个备忘录条目的标题TextView
    public TextView TextView_SingleUserBackUpMsg_Time[] = new TextView[ALL_USER_BACKUPMSGS_COUNT];
    //单个备忘录条目的内容TextView
    public TextView TextView_SingleUserBackUpMsg_Title[] = new TextView[ALL_USER_BACKUPMSGS_COUNT];

    public ImageButton btn_BackToMain;

    //json格式的所有备忘录的值
    JSONObject JSON_UserBackUpMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_user_back_up_msgs);

        try {
            ReadUserBackUpMsgFromFile();
            InitShowAllUserBackUpMsgsComponents();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        InitOtherComponents();
    }

    public void SaveUserBackUpMsgToLocalFile() throws IOException {
        FileOutputStream fileOutputStream = openFileOutput("MyCalendarUserBackUpMsg.json",MODE_PRIVATE);
        String UserBackUpMsg = String.valueOf(JSON_UserBackUpMsg);
        fileOutputStream.write(UserBackUpMsg.getBytes());
        fileOutputStream.close();
    }

    //初始化显示所有用户备忘录的相关组件
    public void InitShowAllUserBackUpMsgsComponents() throws JSONException, IOException {
        //包含所有的用户备忘录的线性布局
        LinearLayout_AllUserBackUpMsgs = (LinearLayout) findViewById(R.id.LinearLayout_AllUserBackUpMsgs);
        LinearLayout_AllUserBackUpMsgs.removeAllViews();

        int curUserBackUpMsgCount = 0;

//        for(int m = 0;m < JSON_UserBackUpMsg.length();++m){ //有几个年
//            for(int n = 0; n < JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).length(); ++n){ //这个年有几个月
//                for(int p = 0; p < JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).length(); ++p){
//
//                }
//            }
//        }

        //System.out.println(JSON_UserBackUpMsg.names());
        for(int m = 0;m < JSON_UserBackUpMsg.length();++m){ //有几个年
            int curUserBackUpMsgYear = Integer.parseInt((String) JSON_UserBackUpMsg.names().get(m)) ;
            for(int n = 0; n < JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).length(); ++n){ //这个年有几个月
                int curUserBackUpMsgMonth = Integer.parseInt((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n));
                for(int p = 0; p < JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).length(); ++p){ //这个月有几天
                    int curUserBackUpMsgDay = Integer.parseInt((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).names().get(p));
                    //当前所遍历到的年月日分别为 curUserBackUpMsgYear, curUserBackUpMsgMonth, curUserBackUpMsgDay
                    //该日的所有备忘录的JSONArray为 JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).getJSONArray((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).names().get(p))
                    //输出测试：
                    System.out.println(curUserBackUpMsgYear + "/" + curUserBackUpMsgMonth + "/" + curUserBackUpMsgDay + ":");
                    System.out.println(JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).getJSONArray((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).names().get(p)));
                    //---------------------------------------------------------------------

                    for(int q = 0;q < JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).getJSONArray((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).getJSONObject((String) JSON_UserBackUpMsg.getJSONObject((String) JSON_UserBackUpMsg.names().get(m)).names().get(n)).names().get(p)).length();++q){
                        int tmpq = q;
                        //这里遍历到了每个单独的备忘录条目，条目编号为q
                        //单个用户备忘条目的线性布局
                        LinearLayout curLinearLayout_SingleUserBackUpMsg = new LinearLayout(this);
                        TextView curTextView_SingleUserBackUpMsg_Time = new TextView(this);
                        TextView curTextView_SingleUserBackUpMsg_Title = new TextView(this);

                        LinearLayout.LayoutParams LinearLayout_SingleUserBackUpMsg_Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300);
                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount] = curLinearLayout_SingleUserBackUpMsg;
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount] = curTextView_SingleUserBackUpMsg_Time;
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount] = curTextView_SingleUserBackUpMsg_Title;

                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).has("starttime")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).put("starttime", "00:00");
                            SaveUserBackUpMsgToLocalFile();
                        }
                        if(!JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).has("endtime")){
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).put("endtime", "00:00");
                            SaveUserBackUpMsgToLocalFile();
                        }


                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setText(
                                " " + curUserBackUpMsgYear + "/" + curUserBackUpMsgMonth + "/" + curUserBackUpMsgDay + "  " +
                                JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).get("starttime")
                                + " - " +
                                        JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear)).getJSONObject(String.valueOf(curUserBackUpMsgMonth)).getJSONArray(String.valueOf(curUserBackUpMsgDay)).getJSONObject(q).get("endtime")
                        );
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setTextSize(20);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        //TextView_SingleUserBackUpMsg_Time[i].setWidth(100);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setHeight(150);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setEllipsize(TextUtils.TruncateAt.END);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setPadding(20,50,0,0);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setTextColor(getResources().getColor(R.color.white));
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setSingleLine(true);
                        TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(Activity_ShowAllUserBackUpMsgs.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpq));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curUserBackUpMsgYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curUserBackUpMsgMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curUserBackUpMsgDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "SHOW_ALL_USERBACKUPMSGS");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });


                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setText(" " +
                                (CharSequence) JSON_UserBackUpMsg.getJSONObject(String.valueOf(curUserBackUpMsgYear))
                                        .getJSONObject(String.valueOf(curUserBackUpMsgMonth))
                                        .getJSONArray(String.valueOf(curUserBackUpMsgDay))
                                        .getJSONObject(q).get("title")
                        );
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setTextSize(25);
                        //TextView_SingleUserBackUpMsg_Title[i].setWidth();
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setHeight(150);
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setEllipsize(TextUtils.TruncateAt.END);
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setPadding(20,10,0,0);
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setTextColor(getResources().getColor(R.color.white));
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setSingleLine(true);
                        TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(Activity_ShowAllUserBackUpMsgs.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpq));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curUserBackUpMsgYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curUserBackUpMsgMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curUserBackUpMsgDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "SHOW_ALL_USERBACKUPMSGS");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });


                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].setOrientation(LinearLayout.VERTICAL);
                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].setLayoutParams(LinearLayout_SingleUserBackUpMsg_Params);
                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].setBackground(getDrawable(R.drawable.cardborder_light_blue));
                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent gotoAddNewUserBackUpMsg = new Intent(Activity_ShowAllUserBackUpMsgs.this, Activity_EditUserBackUpMsg.class);
                                gotoAddNewUserBackUpMsg.putExtra("editMode", String.valueOf(tmpq));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedYear", String.valueOf(curUserBackUpMsgYear));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedMonth", String.valueOf(curUserBackUpMsgMonth));
                                gotoAddNewUserBackUpMsg.putExtra("curSelectedDay", String.valueOf(curUserBackUpMsgDay));
                                gotoAddNewUserBackUpMsg.putExtra("TheUserBackUpMsg", String.valueOf(JSON_UserBackUpMsg));
                                gotoAddNewUserBackUpMsg.putExtra("SourcePage", "SHOW_ALL_USERBACKUPMSGS");

                                startActivityForResult(gotoAddNewUserBackUpMsg, REQUEST_CODE_EDITBACKUPMSG);
                            }
                        });


                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].addView(TextView_SingleUserBackUpMsg_Time[curUserBackUpMsgCount]);
                        LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount].addView(TextView_SingleUserBackUpMsg_Title[curUserBackUpMsgCount]);
                        LinearLayout_AllUserBackUpMsgs.addView(LinearLayout_SingleUserBackUpMsg[curUserBackUpMsgCount]);

                        View spaceView = new View(this);
                        spaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                        LinearLayout_AllUserBackUpMsgs.addView(spaceView);

                        curUserBackUpMsgCount++;
                    }
                }
            }
        }
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

//    //控制是否显示今日的备忘录通知
    public void ShowTodayNotifications(boolean isShowTodayNotifications) throws JSONException {
        //isShowTodayNotifications 为 true 则显示今日通知, isShowTodayNotifications 为 false 则不显示今日通知
        if(isShowTodayNotifications){  //显示今日通知
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            System.out.println("show notification");

            // Android 8.0之后，需要手动添加NotifacationChannel实现，否则log会有如下提示：
            // D/skia: --- Failed to create image decoder with message 'unimplemented'
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel("001", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
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
                    .setContentIntent(pi)
                    .build();

            notificationManager.notify(1, notification);
        }
        else {  //不显示今日通知
            if(notificationManager != null) notificationManager.cancel(1);
        }
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

    //初始化其他组件
    public void InitOtherComponents(){
        btn_BackToMain = (ImageButton) findViewById(R.id.btn_BackToMain);

        btn_BackToMain.setOnClickListener(this);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDITBACKUPMSG:{
                //标志
                if (resultCode == RESULT_OK) {
                    try {
                        JSON_UserBackUpMsg = new JSONObject(data.getStringExtra("TheUserBackUpMsg")) ;
                        InitShowAllUserBackUpMsgsComponents();
                        ShowTodayNotifications(myCalendarOptions.getIsShowTodayNotification());
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
            case R.id.btn_BackToMain:{  //返回按钮
                Intent backToOptions = new Intent(this, Activity_Options.class);
                startActivity(backToOptions);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}