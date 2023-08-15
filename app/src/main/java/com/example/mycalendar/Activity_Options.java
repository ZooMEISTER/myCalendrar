package com.example.mycalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tencent.tauth.Tencent;
import com.unit.MyCalendarOptions;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Activity_Options extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    public LinearLayout LinearLayout_btn_BackToMain_background;

    public MyCalendarOptions myCalendarOptions;
    JSONObject JSON_CurOptions;

    //返回按钮的ImageView
    public ImageView btn_BackToMain;
    //播放所选铃声的按钮
    public ImageView btn_PlayRing;

    //是否显示节日checkbox
    public CheckBox cb_ShowFestival;
    //是否显示节日的checkbox
    public CheckBox cb_ShowTodayNotification;
    public CheckBox cb_FABTransparent;

    //当前选择的铃声
    //public Spinner spinnerCurrentRing;
    public NiceSpinner spinnerCurrentRing;
    String curSelectRing;
    private ArrayAdapter<String> ringsArrayAdapter;
    private String[] rings;

    //查看所有备忘录的按钮
    public Button btn_CheckAllUserBackUpMsgs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //从主界面传过来的数据值
        Intent gotoOptions = getIntent();
        String String_CurOptions = gotoOptions.getStringExtra("String_CurOptions");
        try {
            myCalendarOptions = new MyCalendarOptions();
            myCalendarOptions.TurnStringOptions2UnitOptions(String_CurOptions);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        InitComponents();
    }

    //初始化组件
    public void InitComponents(){
        LinearLayout_btn_BackToMain_background = (LinearLayout) findViewById(R.id.LinearLayout_btn_BackToMain_background);
        btn_BackToMain = (ImageView) findViewById(R.id.btn_BackToMain);
        btn_PlayRing = (ImageView) findViewById(R.id.btn_PlayRing);
        cb_ShowFestival = (CheckBox) findViewById(R.id.cb_ShowFestival);
        cb_ShowTodayNotification = (CheckBox) findViewById(R.id.cb_ShowTodayNotification);
        cb_FABTransparent = (CheckBox) findViewById(R.id.cb_FABTransparent);
        //spinnerCurrentRing = (Spinner) findViewById(R.id.spinnerCurrentRing);
        spinnerCurrentRing = (NiceSpinner) findViewById(R.id.spinnerCurrentRing);
        List<String> ringsNameArray = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.rings)));
        spinnerCurrentRing.attachDataSource(ringsNameArray);
        btn_CheckAllUserBackUpMsgs = (Button) findViewById(R.id.btn_CheckAllUserBackUpMsgs);


        btn_BackToMain.setOnClickListener(this);
        btn_PlayRing.setOnClickListener(this);
        cb_ShowFestival.setOnClickListener(this);
        cb_ShowTodayNotification.setOnClickListener(this);
        cb_FABTransparent.setOnClickListener(this);
        btn_CheckAllUserBackUpMsgs.setOnClickListener(this);

        cb_ShowFestival.setChecked(myCalendarOptions.getIsShowFestival());
        cb_ShowTodayNotification.setChecked(myCalendarOptions.getIsShowTodayNotification());
        cb_FABTransparent.setChecked(myCalendarOptions.getIsFABTransparent());
//        rings = getResources().getStringArray(R.array.rings);
//        ringsArrayAdapter = new ArrayAdapter<>(this, R.layout.rings_spin_item_layout, rings);
//        spinnerCurrentRing.setAdapter(ringsArrayAdapter);
//        int curRingIndex = 0;
//        for(int i = 0;i < rings.length;++i){
//            if(myCalendarOptions.getRingName().equals(rings[i])){
//                curRingIndex = i;
//                break;
//            }
//        }
//        spinnerCurrentRing.setSelection(curRingIndex);
//        spinnerCurrentRing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                curSelectRing = parent.getItemAtPosition(position).toString();//获取i所在的文本
//                myCalendarOptions.setRingName(curSelectRing);
//                //Toast.makeText(Activity_Options.this, curSelectRing, Toast.LENGTH_SHORT).show();
//
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });

        spinnerCurrentRing.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                curSelectRing = (String) parent.getItemAtPosition(position);
                myCalendarOptions.setRingName(curSelectRing);
                Toast.makeText(Activity_Options.this, curSelectRing, Toast.LENGTH_SHORT).show();
            }
        });

        //String str[] = getResources().getStringArray(R.array.rings);
        int pos = 0;
        for(int i = 0;i < getResources().getStringArray(R.array.rings).length;++i){
            if (myCalendarOptions.getRingName().equals(getResources().getStringArray(R.array.rings)[i])){
                pos = i;
            }
        }

        curSelectRing = myCalendarOptions.getRingName();
        spinnerCurrentRing.setSelectedIndex(pos);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_BackToMain:{
                //实体类转json字符串
                Gson gson = new Gson();
                String String_Options = gson.toJson(myCalendarOptions);

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = openFileOutput("Options.json", MODE_PRIVATE);
                    fileOutputStream.write(String_Options.getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //myCalendarOptions.ToString();

                Intent backToMainWithSaveNewOptions = new Intent(Activity_Options.this, MainActivity.class);
                backToMainWithSaveNewOptions.putExtra("String_CurOptions", String_Options);
                setResult(RESULT_OK, backToMainWithSaveNewOptions);
                this.finish();
                break;
            }
            case R.id.btn_PlayRing:{  //播放所选铃声
                Uri uri = null;
                if(curSelectRing.equals("ciao")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.ciao);  //从资源文件中获取铃音
                }
                else if(curSelectRing.equals("apple")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.apple);
                }
                else if(curSelectRing.equals("amogus")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.amogus);
                }
                else if(curSelectRing.equals("aughhh")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.aughhh);
                }
                else if(curSelectRing.equals("helikopter")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.helikopter);
                }
                else if(curSelectRing.equals("metalpipe")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.metalpipe);
                }
                else if(curSelectRing.equals("siu")){
                    uri = Uri.parse("android.resource://" + this.getPackageName() +"/" + R.raw.siu);
                }

                Toast.makeText(Activity_Options.this, curSelectRing, Toast.LENGTH_SHORT).show();
                Ringtone rt = RingtoneManager.getRingtone(this, uri);
                rt.play();


                break;
            }
            case R.id.cb_ShowFestival:{  //切换是否显示节日
                myCalendarOptions.setIsShowFestival(cb_ShowFestival.isChecked());
                //myCalendarOptions.ToString();
                break;
            }
            case R.id.cb_ShowTodayNotification:{  //切换是否显示节日
                myCalendarOptions.setIsShowTodayNotification(cb_ShowTodayNotification.isChecked());
                //myCalendarOptions.ToString();
                break;
            }
            case R.id.cb_FABTransparent:{  //切换是否使用透明FAB
                myCalendarOptions.setIsFABTransparent(cb_FABTransparent.isChecked());
                //myCalendarOptions.ToString();
                break;
            }
            case R.id.btn_CheckAllUserBackUpMsgs:{  //跳转到显示所有备忘录
                Toast.makeText(Activity_Options.this, "所有日程", Toast.LENGTH_SHORT).show();
                Intent goToShowAllUserBackUpMsgs = new Intent(this, Activity_ShowAllUserBackUpMsgs.class);
                startActivity(goToShowAllUserBackUpMsgs);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}