package com.unit;


//设置实体类
//1.是否显示节日


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class MyCalendarOptions {
    boolean isShowFestival = true; //是否显示节日
    boolean isShowTodayNotification = false; //是否显示今日通知
    boolean isFABTransparent = false; //浮动按钮是否透明是否显示今日通知

    int pendingIntentRequestCodeCount = 0;  //表示通知的编号，用于标识不同的pendingintent

    String ringName = "ciao";

    public MyCalendarOptions(){}
    public MyCalendarOptions(boolean isShowFestival, boolean isShowTodayNotification, boolean isFABTransparent, int pendingIntentRequestCodeCount, String ringName){
        this.isShowFestival = isShowFestival;
        this.isShowTodayNotification = isShowTodayNotification;
        this.isFABTransparent = isFABTransparent;
        this.pendingIntentRequestCodeCount = pendingIntentRequestCodeCount;
        this.ringName = ringName;
    }

    public void setIsShowFestival(boolean isShowFestival){
        this.isShowFestival = isShowFestival;
    }
    public void setIsShowTodayNotification(boolean isShowTodayNotification){
        this.isShowTodayNotification = isShowTodayNotification;
    }
    public void setIsFABTransparent(boolean isFABTransparent){
        this.isFABTransparent = isFABTransparent;
    }
    public void setPendingIntentRequestCodeCount(int pendingIntentRequestCodeCount){
        this.pendingIntentRequestCodeCount = pendingIntentRequestCodeCount;
    }
    public void setRingName(String ringName){
        this.ringName = ringName;
    }

    public boolean getIsShowFestival(){
        return this.isShowFestival;
    }
    public boolean getIsShowTodayNotification(){
        return this.isShowTodayNotification;
    }
    public boolean getIsFABTransparent(){
        return this.isFABTransparent;
    }
    public int getPendingIntentRequestCodeCount(){
        return this.pendingIntentRequestCodeCount;
    }
    public String getRingName(){
        return this.ringName;
    }

    //将JSON字符串转换为实体类
    public void TurnStringOptions2UnitOptions(String String_Options) throws JSONException {
        JSONObject JSON_Options = new JSONObject(String_Options);
        this.setIsShowFestival(JSON_Options.getBoolean("isShowFestival"));
        this.setIsShowTodayNotification(JSON_Options.getBoolean("isShowTodayNotification"));
        this.setIsFABTransparent(JSON_Options.getBoolean("isFABTransparent"));
        this.setPendingIntentRequestCodeCount(JSON_Options.getInt("pendingIntentRequestCodeCount"));
        this.setRingName(JSON_Options.getString("ringName"));
    }

    public void ToString(){
        Log.i("MyCalendarOptions-isShowFestival: ", String.valueOf(this.isShowFestival));
        Log.i("MyCalendarOptions-isShowTodayNotification: ", String.valueOf(this.isShowTodayNotification));
        Log.i("MyCalendarOptions-isFABTransparent: ", String.valueOf(this.isFABTransparent));
        Log.i("MyCalendarOptions-pendingIntentRequestCodeCount: ", String.valueOf(this.pendingIntentRequestCodeCount));
        Log.i("MyCalendarOptions-ringName: ", this.getRingName());
    }
}
