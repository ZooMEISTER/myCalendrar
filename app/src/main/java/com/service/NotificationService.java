package com.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.mycalendar.MainActivity;
import com.example.mycalendar.R;

public class NotificationService extends Service {
    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ShowNotification(intent.getStringExtra("title"));

        return super.onStartCommand(intent, flags, startId);
    }

    public void ShowNotification(String title){
        //Service 的功能，创建通知栏通知
        NotificationManager notificationManager;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android 8.0之后，需要手动添加NotifacationChannel实现，否则log会有如下提示：
        // D/skia: --- Failed to create image decoder with message 'unimplemented'
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("002", "channel_ArrangementStartNotification", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent_Notification = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent_Notification, 0);

        //不知道为什么，通知的大图标不显示
        Notification notification = new NotificationCompat.Builder(this, "002")
                .setContentTitle("日程： " + title + " 已开始")
                .setContentText("点击以跳转到应用界面")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.mycalendar_white)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.mycalendar_white))
                .setOngoing(false)
                .setSound(null)
                .setNotificationSilent()
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        int randomnum = (int) (Math.random() * 100);
        notificationManager.notify(randomnum, notification);
    }
}