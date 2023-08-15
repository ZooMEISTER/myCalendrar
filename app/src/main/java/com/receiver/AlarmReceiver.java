package com.receiver;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

import com.example.mycalendar.R;
import com.service.NotificationService;


public class AlarmReceiver extends BroadcastReceiver {

    //震动
    private static void PlayVibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        long[] vibrationPattern = new long[]{0, 180, 80, 120};
        vibrator.vibrate(vibrationPattern, -1);
    }

    //响铃
    private static void PlayAlarm(Context context, String ring) {
        //Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri uri = null;
        if(ring.equals("ciao")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.ciao);  //从资源文件中获取铃音
        }
        else if(ring.equals("apple")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.apple);
        }
        else if(ring.equals("amogus")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.amogus);
        }
        else if(ring.equals("aughhh")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.aughhh);
        }
        else if(ring.equals("helikopter")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.helikopter);
        }
        else if(ring.equals("metalpipe")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.metalpipe);
        }
        else if(ring.equals("siu")){
            uri = Uri.parse("android.resource://" + context.getPackageName() +"/" + R.raw.siu);
        }
        
        Ringtone rt = RingtoneManager.getRingtone(context, uri);
        rt.play();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("通知来喽");

        boolean ring = intent.getBooleanExtra("isring", false);
        boolean vibrate = intent.getBooleanExtra("isvibrate", false);
        String title = intent.getStringExtra("title");
        String ringName = intent.getStringExtra("ringname");

        if(ring || vibrate) {
            Toast.makeText(context, "有日程已开始", Toast.LENGTH_SHORT).show();
            //调用通知服务
            Intent notificationService = new Intent(context, NotificationService.class);
            notificationService.putExtra("title", title);
            context.startService(notificationService);
        }

        if (ring){
            System.out.println("铃声");
            PlayAlarm(context, ringName);
        }
        if (vibrate){
            System.out.println("震动");
            PlayVibrate(context);
        }
    }
}
