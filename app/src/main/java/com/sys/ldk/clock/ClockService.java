package com.sys.ldk.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sys.ldk.MyApplication;
import com.sys.ldk.app.Startapp;
import com.sys.ldk.serverset.MyNotificationType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class ClockService extends Service {
    private AlarmManager am;
    private MyDatabaseHelper myDatabaseHelper;
    private SQLiteDatabase db;

    private List<PendingIntent> pendingIntentList = new ArrayList<>();
    private Vibrator vibrator;
    private ClockBean clockBean;
    private MediaPlayer mMediaPlayer;
    private Messenger mService;
    private boolean mIsBound;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (PendingIntent p : pendingIntentList) {
            am.cancel(p);
        }
        db.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getStringExtra("flag") != null) {
            if (intent.getStringExtra("flag").equals("ClockReceiver")) {
                myDatabaseHelper = new MyDatabaseHelper(this, "Items.db", null, 1);
                db = myDatabaseHelper.getWritableDatabase();
                Cursor cursor2 = db.query("clocks", new String[]{"*"}, "id=?",
                        new String[]{intent.getStringExtra("code")}, null, null, null, null);
                while (cursor2.moveToNext()) {
                    clockBean = new ClockBean(cursor2.getInt(cursor2.getColumnIndex("id")),
                            cursor2.getString(cursor2.getColumnIndex("time")),
                            cursor2.getString(cursor2.getColumnIndex("repeat")),
                            cursor2.getInt(cursor2.getColumnIndex("isSwitchOn")),
                            cursor2.getString(cursor2.getColumnIndex("apptype")),
                            cursor2.getString(cursor2.getColumnIndex("create_time")));
                }
                if (clockBean.getIsSwitchOn() == 1) {
                    startAlertActivity(this, clockBean);
                }
            } else {
                am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
                myDatabaseHelper = new MyDatabaseHelper(ClockService.this, "Items.db", null, 1);
                db = myDatabaseHelper.getWritableDatabase();
                Cursor cursor = db.query("clocks", null, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    ClockBean clockBean = new ClockBean(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("time")),
                            cursor.getString(cursor.getColumnIndex("repeat")),
                            cursor.getInt(cursor.getColumnIndex("isSwitchOn")),
                            cursor.getString(cursor.getColumnIndex("apptype")),
                            cursor.getString(cursor.getColumnIndex("create_time"))
                    );
                    pendingIntentList.add(initRemind(Integer.parseInt(clockBean.getTime().split(":")[0]),
                            Integer.parseInt(clockBean.getTime().split(":")[1]), clockBean.getId()));
                    Toast.makeText(getApplicationContext(), "开启", Toast.LENGTH_SHORT).show();
                }
                for (PendingIntent p : pendingIntentList) {
                    Log.i("pendingIntentList", p.getCreatorPackage());
                }
            }
        } else {
            am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
            myDatabaseHelper = new MyDatabaseHelper(ClockService.this, "Items.db", null, 1);
            db = myDatabaseHelper.getWritableDatabase();
            Cursor cursor = db.query("clocks", null, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ClockBean clockBean = new ClockBean(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("time")),
                        cursor.getString(cursor.getColumnIndex("repeat")),
                        cursor.getInt(cursor.getColumnIndex("isSwitchOn")),
                        cursor.getString(cursor.getColumnIndex("apptype")),
                        cursor.getString(cursor.getColumnIndex("create_time"))
                );
                pendingIntentList.add(initRemind(Integer.parseInt(clockBean.getTime().split(":")[0]),
                        Integer.parseInt(clockBean.getTime().split(":")[1]), clockBean.getId()));
                Toast.makeText(getApplicationContext(), "开启", Toast.LENGTH_SHORT).show();
            }
            for (PendingIntent p : pendingIntentList) {
                Log.i("pendingIntentList", p.getCreatorPackage());
            }
        }
        return START_STICKY;
    }

    private void startAlertActivity(Context context, ClockBean clockBean) {
        Calendar calendar = Calendar.getInstance();
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (clockBean.getRepeat().equals("仅一次")) {
            showDialog(clockBean);
            db.delete("clocks", "id=?", new String[]{clockBean.getId() + ""});
        } else if (clockBean.getRepeat().equals("周一到周五")) {
            if (day_of_week >= 1 && day_of_week <= 5) {
                showDialog(clockBean);
            }
        } else if (clockBean.getRepeat().equals("每天")) {
            showDialog(clockBean);
        }
    }

    private void shake() {
        vibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
        //数组的a[0]表示静止的时间，a[1]代表的是震动的时间，
        long[] time = {1000, 2000};
        //震动的毫秒值
        //vibrate的第二参数表示从哪里开始循环
        vibrator.vibrate(time, 0);
    }

    private PendingIntent initRemind(int huorOfDay, int minute, int requestCode) {

        Intent intent2 = new Intent(this, ClockReceiver.class);
        intent2.putExtra("requestcode", requestCode);
        intent2.setAction("com.sys.ldk.clock.RING");
        PendingIntent pi = PendingIntent.getBroadcast(this, requestCode, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        long systemTime = System.currentTimeMillis();//java.lang.System.currentTimeMillis()，它返回从 UTC 1970 年 1 月 1 日午夜开始经过的毫秒数。
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); //  这里时区需要设置一下，不然会有8个小时的时间差
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, huorOfDay);//
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);            //选择的定时时间
        long selectTime = calendar.getTimeInMillis();    //计算出设定的时间
        //  如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }// 进行闹铃注册
        if (Build.VERSION.SDK_INT < 19) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, selectTime, am.INTERVAL_DAY, pi);
        } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            am.setExact(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        } else if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    selectTime, pi);
        }
        return pi;
    }

    private void showDialog(ClockBean clockBean) {
      /*  Intent intent = new Intent(this, StartAppReceiver.class);
        intent.putExtra("apptype", clockBean.getApptype());
        intent.setAction("com.sys.ldk");
        sendBroadcast(intent);*/
        Log.d("ClockService", "定时时间到");
        doBindService();
    }

    /**
     * @description 通过IPC启动app
     * @param
     * @return
     * @author Nine_Dollar
     * @time 2020/11/5 1:38
     */
    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ClockService", "连接成功");
            mService = new Messenger(service);
//            Message message = Message.obtain(null, MyNotificationType.case3);
            /*Bundle bundle = new Bundle();
            bundle.putString("app", clockBean.getApptype());
            message.setData(bundle);
            try {
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ClockService", "连接失败");
        }
    };

    public void doBindService() {
        Intent intent = new Intent(this, Startapp.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                Log.d("ClockService: ", "关闭app启动的服务");
                MyApplication.getContext().unbindService(mConnection);
                mIsBound = false;
            }
        } else {
            Log.d("ClockService: ", "未创建服务");
        }
    }
}


