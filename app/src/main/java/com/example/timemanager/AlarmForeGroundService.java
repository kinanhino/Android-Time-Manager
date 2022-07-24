package com.example.timemanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AlarmForeGroundService extends Service {

    private static myViewModel model;
    private String dateFormat = "HH:mm dd/MM/yyyy";
    String CHANNEL_ID = "CHANNEL_SAMPLE";
    Notification.Builder NotifyBuilder;
    NotificationManager notificationManager;
    private static Thread thread;
    static boolean run ;

    @Override
    public void onCreate() {
        super.onCreate();
        this.model = MainActivity.getViewModel();
        createNotificationChannel();
        Notification notification = createEmptyNotification();
        this.startForeground(1,notification);
        run = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(run) {
                    int id = 2;
                    ArrayList<Task> tasks = model.getAllTasks();
                    for (Task t : tasks) {
                        if (!t.isCompleted()) {
                            Calendar taskDate = new GregorianCalendar();
                            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
                            try {
                                taskDate.setTime(fmt.parse(t.getDate()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Long timeDiff = getTimeDifference(taskDate);
                            if (isAlarmTime(timeDiff)) {
                                notificationManager.notify(id++,createNotification("Task : "+ t.getName() + " is due in " + (int)Math.ceil(((timeDiff.doubleValue()/1000)/60))+ " minutes"));
                            }
                        }
                    }
                    try {
                        Thread.sleep(60000);//run every minute
                    } catch (InterruptedException e) {
                        if(!run) {
                            stopForeground(true);
                            stopSelf();
                        }
                    }
                }
        }});
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }


    public static void interruptThread()
    {
        if(thread != null)
            thread.interrupt();
    }

    public static void stop()
    {
        run = false;
        interruptThread();
    }

    private long getTimeDifference(Calendar taskDate) {
        return (taskDate.getTimeInMillis() - System.currentTimeMillis());
    }

    private boolean isAlarmTime(long diff)
    {
        return (((diff <= 1000*60*5) && (diff >= ((1000*60*4)))) || (diff <= 1000*60*15 && (diff >= ((1000*60*14)))) || (diff <= 1000*60*30 && (diff >= ((1000*60*29)))));
    }


    public void createNotificationChannel(){
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public Notification createNotification(String title) {
        NotifyBuilder = new Notification.Builder(this,CHANNEL_ID)
                .setAutoCancel(true)
                .setContentText("Time Manager")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title);
        Notification notification = NotifyBuilder.build();
        return notification;
    }

    private Notification createEmptyNotification() {
        NotifyBuilder = new Notification.Builder(this,CHANNEL_ID)
                .setAutoCancel(true);
        Notification notification = NotifyBuilder.build();
        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
