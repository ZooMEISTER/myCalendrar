package com.example.mycalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.view.MonthPicker;
import com.view.YearPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.PublicKey;

public class Activity_FullYearCalendar extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    int screenWidth;
    int screenHeight;

    //星期标题的高度(px)
    int HEIGHT_WEEK_TITLE = 50;
    //每一行星期的高度(px)
    int HEIGHT_SINGLE_WEEK = 40;
    //星期标题的每一个的宽度 = 每个日期的宽度
    int WIDTH_SINGLE_DATE = 46;

    String String_UserBackUpMsg;
    JSONObject JSON_UserBackUpMsg;


    //当选中新的年时，选中的月日切换为 1 月 1 号
    public int curSelectedYear;  //当前选中的年
    public int stillCurSelectedYear;  //当前选中的年
    public int curSelectedMonth;  //当前选中的月
    public int curSelectedDay;  //当前选中的日
     public int curYear;  //当前的年
    public int curMonth;  //当前的月
    public int curDay;  //当前的日

    public LinearLayout LinearLayout_DatePicker;

    public TextView txtSelectYear;
    Dialog dialog = null;
    YearPicker yearPicker = null;

    View View_FakeShadow;

    //包含每个月的小日历的线性布局
    public LinearLayout LinearLayout_SingleMonth[] = new LinearLayout[12];
    //每个月的小日历的星期标题
    public LinearLayout LinearLayout_SingleMonthSingleRowTitle[] = new LinearLayout[12];
    //每个月的星期几的标题的单个textview
    public TextView txtSingleWeekTitle[] = new TextView[84];
    //每个月的小日历的其中一行
    public LinearLayout LinearLayout_SingleMonthSingleRowDate[] = new LinearLayout[72];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_year_calendar);

        Intent gotoFullYearCalendar = getIntent();
        curSelectedYear = gotoFullYearCalendar.getIntExtra("curSelectedYear", 1970);
        stillCurSelectedYear = curSelectedYear;
        curSelectedMonth = gotoFullYearCalendar.getIntExtra("curSelectedMonth", 1);
        curSelectedDay = gotoFullYearCalendar.getIntExtra("curSelectedDay", 1);
        curYear = gotoFullYearCalendar.getIntExtra("curYear", 1970);
        curMonth = gotoFullYearCalendar.getIntExtra("curMonth", 1);
        curDay = gotoFullYearCalendar.getIntExtra("curDay", 1);
        String_UserBackUpMsg = gotoFullYearCalendar.getStringExtra("userBackUpMsg");
        try {
            JSON_UserBackUpMsg = new JSONObject(String_UserBackUpMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        screenWidth = getScreenWidth(this);
        screenHeight = getScreenHeight(this);

        try {
            InitFullYearCalendarComponents();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        InitOtherComponents();

        View_FakeShadow.bringToFront();
    }

    //获得某年某月的 1 号是星期几
    public int GetSpecificYearnMonthFirWeek(int calcY, int calcM){
        if(calcM == 1 || calcM == 2){
            calcM = calcM + 12;
            calcY--;
        }
        int curSelectedMonthFirWeek = (1 + 2 * calcM + 3 * (calcM + 1) / 5 + calcY + calcY / 4 - calcY / 100 + calcY / 400 )  %  7 + 1;
        return curSelectedMonthFirWeek;
    }

    //获取某年某月有多少天
    public int GetSpecificMonthDaysCount(int calcY, int calcM){
        switch (calcM){
            case 1: case 3: case 5: case 7: case 8: case 10:
            case 12: {
                return 31;
            }
            case 4: case 6: case 9:
            case 11: {
                return 30;
            }
            case 2:{
                if(calcY % 4 == 0 && calcY % 100 != 0 || calcY % 400 == 0){ return 29; }
                else return 28;
            }
        }
        return 0;
    }

    //将数字星期转换为汉字星期
    public String TurnWeekNum2WeekChinese(int weekNum){
        switch (weekNum){
            case 0: return "日";
            case 1: return "一";
            case 2: return "二";
            case 3: return "三";
            case 4: return "四";
            case 5: return "五";
            case 6: return "六";
        }
        return "未知";
    }

    //返回到主页
    public void BackToMain(int newYear, int newMonth){
        Intent backToMainNewYear = new Intent(Activity_FullYearCalendar.this, MainActivity.class);
        backToMainNewYear.putExtra("newYear", newYear);
        backToMainNewYear.putExtra("newMonth", newMonth);
        setResult(RESULT_OK, backToMainNewYear);
        this.finish();
    }

    //初始化显示全年日历的相关控件
    public void InitFullYearCalendarComponents() throws JSONException {
        //包含每个月的小日历的线性布局
        LinearLayout_SingleMonth[0] = (LinearLayout) findViewById(R.id.LinearLayout_1_0);
        LinearLayout_SingleMonth[1] = (LinearLayout) findViewById(R.id.LinearLayout_2_0);
        LinearLayout_SingleMonth[2] = (LinearLayout) findViewById(R.id.LinearLayout_3_0);
        LinearLayout_SingleMonth[3] = (LinearLayout) findViewById(R.id.LinearLayout_4_0);
        LinearLayout_SingleMonth[4] = (LinearLayout) findViewById(R.id.LinearLayout_5_0);
        LinearLayout_SingleMonth[5] = (LinearLayout) findViewById(R.id.LinearLayout_6_0);
        LinearLayout_SingleMonth[6] = (LinearLayout) findViewById(R.id.LinearLayout_7_0);
        LinearLayout_SingleMonth[7] = (LinearLayout) findViewById(R.id.LinearLayout_8_0);
        LinearLayout_SingleMonth[8] = (LinearLayout) findViewById(R.id.LinearLayout_9_0);
        LinearLayout_SingleMonth[9] = (LinearLayout) findViewById(R.id.LinearLayout_10_0);
        LinearLayout_SingleMonth[10] = (LinearLayout) findViewById(R.id.LinearLayout_11_0);
        LinearLayout_SingleMonth[11] = (LinearLayout) findViewById(R.id.LinearLayout_12_0);

        //先清空
        for(int i = 0;i < 12;++i) LinearLayout_SingleMonth[i].removeAllViews();

        //先加上每个月的标题
        for(int i = 0;i < 12;++i){
            TextView txtSingleMonthTitle = new TextView(this);
            txtSingleMonthTitle.setText(" " + (i + 1) + " 月");
            if(curSelectedYear == curYear && (i + 1) == curMonth){
                txtSingleMonthTitle.setTextColor(getResources().getColor(R.color.red));
            }
            else if(curSelectedYear == stillCurSelectedYear && (i + 1) == curSelectedMonth){
                txtSingleMonthTitle.setTextColor(getResources().getColor(R.color.green));
            }
            else {
                txtSingleMonthTitle.setTextColor(getResources().getColor(R.color.white));
            }

            txtSingleMonthTitle.setTextSize(30);
            txtSingleMonthTitle.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            txtSingleMonthTitle.setHeight(110);
            //txtSingleMonthTitle.setBackgroundColor(getResources().getColor(R.color.red));

            int finalI = i;
            txtSingleMonthTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackToMain(curSelectedYear, finalI + 1);
                }
            });

            LinearLayout_SingleMonth[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackToMain(curSelectedYear, finalI + 1);
                }
            });

            LinearLayout_SingleMonth[i].addView(txtSingleMonthTitle);
        }


        //将星期的标题加进去
        for(int i = 0;i < 12;++i){
            LinearLayout curLinearLayout_SingleMonthSingleRowTitle = new LinearLayout(this);
            LinearLayout_SingleMonthSingleRowTitle[i] = curLinearLayout_SingleMonthSingleRowTitle;
            LinearLayout_SingleMonthSingleRowTitle[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HEIGHT_WEEK_TITLE));
            LinearLayout_SingleMonthSingleRowTitle[i].setOrientation(LinearLayout.HORIZONTAL);
            for(int m = 0;m < 7;++m){
                TextView txtWeekTitle = new TextView(this);
                txtWeekTitle.setWidth(WIDTH_SINGLE_DATE);
                txtWeekTitle.setHeight(HEIGHT_WEEK_TITLE);
                txtWeekTitle.setText(TurnWeekNum2WeekChinese(m));
                txtWeekTitle.setTextSize(10);
                txtWeekTitle.setTextColor(getResources().getColor(R.color.white));
                txtWeekTitle.setGravity(Gravity.CENTER);
                //txtWeekTitle.setBackgroundColor(getResources().getColor(R.color.green));
                LinearLayout_SingleMonthSingleRowTitle[i].addView(txtWeekTitle);
            }
            LinearLayout_SingleMonth[i].addView(LinearLayout_SingleMonthSingleRowTitle[i]);
        }


        //将详细的日期加进去
        for(int i = 0;i < 12;++i){  //循环每个月
            int thisMonthFirWeek = GetSpecificYearnMonthFirWeek(curSelectedYear, i + 1);
            int thisMonthDaysCount = GetSpecificMonthDaysCount(curSelectedYear, i + 1);
            int curAddedDays = 0;  //目前已加入的天数
            boolean isStartAddRealDate = false; //是否已开始添加日期
            for(int m = 0;m < 6;++m){  //循环每一周
                LinearLayout curLinearLayout_SingleMonthSingleRowDate = new LinearLayout(this);
                LinearLayout_SingleMonthSingleRowDate[m + i * 6] = curLinearLayout_SingleMonthSingleRowDate;
                LinearLayout_SingleMonthSingleRowDate[m + i * 6].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HEIGHT_SINGLE_WEEK));
                LinearLayout_SingleMonthSingleRowDate[m + i * 6].setOrientation(LinearLayout.HORIZONTAL);

                for(int n = 0;n < 7;++n){  //循环每一天
                    if(m == 0){ //正在添加第一行
                        if(n >= (thisMonthFirWeek % 7)) isStartAddRealDate = true;
                    }
                    TextView txtDate = new TextView(this);
                    txtDate.setWidth(WIDTH_SINGLE_DATE);
                    txtDate.setHeight(HEIGHT_SINGLE_WEEK);
                    //上色
                    if(curSelectedYear == curYear && (i + 1) == curMonth && (curAddedDays + 1) == curDay){
                        txtDate.setTextColor(getResources().getColor(R.color.red));
                        //txtDate.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    }
                    else if(curSelectedYear == stillCurSelectedYear && (i + 1) == curSelectedMonth && (curAddedDays + 1) == curSelectedDay){
                        txtDate.setTextColor(getResources().getColor(R.color.green));
                    }
                    else {
                        txtDate.setTextColor(getResources().getColor(R.color.white));
                    }
                    //上下划线
                    if(
                            JSON_UserBackUpMsg.has(String.valueOf(curSelectedYear)) &&
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).has(String.valueOf(i + 1)) &&
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(i + 1)).has(String.valueOf(curAddedDays + 1)) &&
                            JSON_UserBackUpMsg.getJSONObject(String.valueOf(curSelectedYear)).getJSONObject(String.valueOf(i + 1)).getJSONArray(String.valueOf(curAddedDays + 1)).length() > 0
                    ){
                        txtDate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    }
                    txtDate.setGravity(Gravity.CENTER);
                    if(isStartAddRealDate && curAddedDays < thisMonthDaysCount){
                        txtDate.setText(String.valueOf(curAddedDays + 1));
                        curAddedDays++;
                    }
                    else {
                        txtDate.setText("");
                    }
                    txtDate.setTextSize(10);

                    //txtDate.setBackgroundColor(getResources().getColor(R.color.yellow));
                    int finalI = i;
                    txtDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BackToMain(curSelectedYear, finalI + 1);
                        }
                    });

                    LinearLayout_SingleMonthSingleRowDate[m + i * 6].addView(txtDate);
                }

                LinearLayout_SingleMonth[i].addView(LinearLayout_SingleMonthSingleRowDate[m + i * 6]);
            }
        }

    }

    //初始化其他控件
    public void InitOtherComponents(){
        LinearLayout_DatePicker = (LinearLayout) findViewById(R.id.LinearLayout_DatePicker);
        txtSelectYear = (TextView) findViewById(R.id.txtSelectYear);
        View_FakeShadow = (View) findViewById(R.id.View_FakeShadow);

        txtSelectYear.setOnClickListener(this);

        LinearLayout_DatePicker.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 170));
        txtSelectYear.setText(" " + curSelectedYear + " 年 ");
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(View_FakeShadow.getLayoutParams());
        lp.setMargins(0, 170, 0, 0);
        View_FakeShadow.setLayoutParams(lp);
    }


    //年份选择
    private void onDataClick(){
        if(dialog == null){
            System.out.println("new dialog: " + curSelectedYear + "/" + curSelectedMonth + "/" + curSelectedDay);
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.date_year_view);
            yearPicker = (YearPicker) dialog.findViewById(R.id.year_picker);
        }

        Button btn_left = (Button) dialog.findViewById(R.id.btn_left);
        Button btn_right = (Button) dialog.findViewById(R.id.btn_right);

        yearPicker.updateDate(curSelectedYear, curSelectedMonth - 1, curSelectedDay);

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
                txtSelectYear.setText(" " + yearPicker.getYear() + " 年 ");
                if(curSelectedYear != yearPicker.getYear()){
                    curSelectedMonth = 1;
                    curSelectedDay = 1;
                }
                curSelectedYear = yearPicker.getYear();

                try {
                    InitFullYearCalendarComponents();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtSelectYear:{
                onDataClick();

                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

     //获得屏幕的宽度
     public static int getScreenWidth(Context ctx) {
         //从系统服务中获取窗口管理器
         WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
         DisplayMetrics dm = new DisplayMetrics();// 从默认显示器中获取显示参数保存到dm对象中
         wm.getDefaultDisplay().getMetrics(dm);
         return dm.widthPixels; // 返回屏幕的宽度数值
     }
     //获得屏幕的高度
     public static int getScreenHeight(Context ctx) {
        //从系统服务中获取窗口管理器
         WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
         DisplayMetrics dm = new DisplayMetrics();// 从默认显示器中获取显示参数保存到dm对象中
         wm.getDefaultDisplay().getMetrics(dm);
         return dm.heightPixels; // 返回屏幕的高度数值
     }
}