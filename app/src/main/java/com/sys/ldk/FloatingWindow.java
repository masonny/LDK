package com.sys.ldk;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sys.ldk.accessibility.api.AcessibilityApi;
import com.sys.ldk.accessibility.api.UiApi;
import com.sys.ldk.accessibility.api.User;
import com.sys.ldk.accessibility.util.ApiUtil;
import com.sys.ldk.accessibility.util.LogUtil;
import com.sys.ldk.dg.Autoanswer;
import com.sys.ldk.dg.SandTimer;
import com.sys.ldk.dg.XXQG;
import com.sys.ldk.dg.XxqgFuntion;
import com.sys.ldk.easyfloat.EasyFloat;
import com.sys.ldk.easyfloat.anim.AppFloatDefaultAnimator;
import com.sys.ldk.easyfloat.enums.ShowPattern;
import com.sys.ldk.easyfloat.enums.SidePattern;
import com.sys.ldk.easyfloat.interfaces.OnFloatCallbacks;
import com.sys.ldk.easyfloat.permission.PermissionUtils;
import com.sys.ldk.http.Http;
import com.sys.ldk.http.JsonHelper;
import com.sys.ldk.http.JsonString;
import com.sys.ldk.serverset.Binding;
import com.sys.ldk.serverset.KeepLiveUtils;
import com.sys.ldk.serverset.MainService;
import com.sys.ldk.serverset.MyNotificationType;
import com.sys.ldk.shellService.Main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class FloatingWindow {
    private static int clickNum = 0;
    @SuppressLint("StaticFieldLeak")
    public static Context mcontext;
    @SuppressLint("StaticFieldLeak")
    public static ImageView runimage;
    private static ConstraintLayout constraintLayout;
    private static AnimationDrawable drawable;
    @SuppressLint("StaticFieldLeak")
    private static Button btn_start;
    @SuppressLint("StaticFieldLeak")
    private static Button btn_stop;

    public static void start_float_windows() {
        mcontext = MainActivity.getMycontext();
        if (!PermissionUtils.checkPermission(mcontext)) {
            AlertDialog alertDialog = new AlertDialog.Builder(mcontext)
                    .setTitle("提示")
                    .setMessage("使用浮窗功能，需要您授权悬浮窗权限")
                    .setPositiveButton("去开启", (dialog, which) -> {
                        Floating_windows_1();
                    })
                    .setNegativeButton("取消", null)
                    .create();
            alertDialog.show();
        } else {
            Floating_windows_1();
//            test_float();
        }
    }

    @SuppressLint("RtlHardcoded")
    private static void Floating_windows_1() {
        EasyFloat.with(mcontext)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setTag("1")
                .setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT)
                .setLayout(R.layout.float1, View -> {
                    runimage = (ImageView) View.findViewById(R.id.icon);

                    runimage.setOnClickListener(v -> {
                        clickNum++;
                        // 初始化定时器
                        MainActivity.getTimer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //这里写处理功能
                                if (clickNum == 1) {
                                    LogUtil.V("单击");
                                    runimage.post(FloatingWindow::lick_runimage);
                                } else if (clickNum == 2) {
                                    LogUtil.V("双击");
                                    mcontext.startActivity(new Intent(mcontext, MainActivity.class));
//                                    handler.removeCallbacks(runnable);
                                }
                                clickNum = 0;
                            }
                        }, 200);
                    });

                    runimage.setOnLongClickListener(v -> {
                        EasyFloat.hideAppFloat("1");
                        Toast.makeText(mcontext, "隐藏悬浮窗", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                })
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, @Nullable String msg, @Nullable View view) {
                        LogUtil.V("创建第1个悬浮窗");
                    }

                    @Override
                    public void show(@NotNull View view) {
                        LogUtil.V("显示第1个悬浮窗");
                    }

                    @Override
                    public void hide(@NotNull View view) {
                        LogUtil.V("隐藏第1个悬浮窗");
                    }

                    @Override
                    public void dismiss() {
                        LogUtil.V("关闭第1个悬浮窗");
                    }

                    @Override
                    public void touchEvent(@NotNull View view, @NotNull MotionEvent event) {
//                        LogUtil.I("触摸第1个悬浮窗");
                    }

                    @Override
                    public void drag(@NotNull View view, @NotNull MotionEvent event) {
//                        LogUtil.I("拖动第1个悬浮窗");
                    }

                    @Override
                    public void dragEnd(@NotNull View view) {
                        LogUtil.V("第1个悬浮窗拖动结束");
                    }
                })
                .show();
    }


    public static void xuan_zhuan() {
        LogUtil.D("旋转");
//        回主线程
        runimage.post(() -> {
            runimage.setBackgroundResource(R.drawable.run_xml);
            drawable = (AnimationDrawable) runimage.getBackground();

            LogUtil.D("旋转开始");
            if (drawable != null) {
                drawable.start();
            }
        });
    }

    private static void lick_runimage() {
        LogUtil.V("单击");
        if (EasyFloat.appFloatIsShow("2")) {
            EasyFloat.hideAppFloat("2");
        } else {
            EasyFloat.showAppFloat("2");
            if (!EasyFloat.appFloatIsShow("2")) {
                Floating_windows_2();
            }
        }
    }

    //    悬浮窗
    private static void Floating_windows_2() {
        EasyFloat.with(mcontext)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT)
                .setAppFloatAnimator(new AppFloatDefaultAnimator())
                .setTag("2")
                .setLayout(R.layout.activity_floatingwindow, View -> {
                    constraintLayout = View.findViewById(R.id.parent_float_windows);

                    btn_start = View.findViewById(R.id.start);
                    btn_start.setOnClickListener(v1 -> start());

                    View.findViewById(R.id.btn_dati).setOnClickListener(v1 -> dati());

                    btn_stop = View.findViewById(R.id.btn_stop);
                    btn_stop.setOnClickListener(v1 -> stop());

                    View.findViewById(R.id.ivClose).setOnClickListener(v1 -> hide());
                })
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, @Nullable String msg, @Nullable View view) {
                        LogUtil.V("创建第2个悬浮窗");
                        EasyFloat.hideAppFloat("1");
                    }

                    @Override
                    public void show(@NotNull View view) {
                        LogUtil.V("显示第2个悬浮窗");
                        EasyFloat.hideAppFloat("1");
                    }

                    @Override
                    public void hide(@NotNull View view) {
                        LogUtil.V("隐藏第2个悬浮窗");
                        EasyFloat.showAppFloat("1");
                    }

                    @Override
                    public void dismiss() {
                        LogUtil.V("关闭第2个悬浮窗");
                    }

                    @Override
                    public void touchEvent(@NotNull View view, @NotNull MotionEvent event) {
//                        LogUtil.I("触摸第2个悬浮窗");
                    }

                    @Override
                    public void drag(@NotNull View view, @NotNull MotionEvent event) {
//                        LogUtil.I("拖动第2个悬浮窗");
                        constraintLayout.setBackgroundResource(R.drawable.corners);
                    }

                    @Override
                    public void dragEnd(@NotNull View view) {
                        LogUtil.V("第2个悬浮窗拖动结束");
                        int[] location = new int[2];
//                        返回x，y坐标
                        view.getLocationOnScreen(location);
                        constraintLayout.setBackgroundResource(location[0] > 3 ? R.drawable.corners_left : R.drawable.corners_right);
                    }
                })
                .show();
    }

    private static void start() {
        if (!ApiUtil.isAccessibilityServiceOn(mcontext, MainAccessService.class)) {
            Toast.makeText(mcontext, "请开启辅助服务", Toast.LENGTH_SHORT).show();
            return;
        }
        init();
    }

    @SuppressLint("ResourceAsColor")
    private static void init() {
        LogUtil.I("---" + DG_Thread.get_modeThread());
        switch (DG_Thread.get_modeThread()) {
            case DG_Thread.runing:
                btn_start.setText("恢复");
                btn_start.setTextColor(mcontext.getResources().getColor(R.color.violet, null));
                btn_stop.setEnabled(true);
                btn_stop.setTextColor(mcontext.getResources().getColor(R.color.colorAccent, null));
                DG_Thread.zhanting();
                break;
            case DG_Thread.zan_ting:
                btn_start.setText("暂停");
                btn_start.setTextColor(mcontext.getResources().getColor(R.color.colorAccent, null));
                btn_stop.setEnabled(true);
                btn_stop.setTextColor(mcontext.getResources().getColor(R.color.colorAccent, null));
                DG_Thread.huifu();
                EasyFloat.hideAppFloat("2");
                break;

            case DG_Thread.no_run:
            case "未就绪":
                btn_start.setText("暂停");
                btn_start.setTextColor(mcontext.getResources().getColor(R.color.colorAccent, null));
                btn_stop.setEnabled(true);
                btn_stop.setTextColor(mcontext.getResources().getColor(R.color.colorAccent, null));

                EasyFloat.hideAppFloat("2");
                EasyFloat.showAppFloat("1");

                DG_Thread.start();
                break;
        }
    }


    private static void dati() {
        if (!ApiUtil.isAccessibilityServiceOn(mcontext, MainAccessService.class)) {
            Toast.makeText(mcontext, "请开启辅助服务", Toast.LENGTH_SHORT).show();
            return;
        }
        hide();
        image_run();
        new Thread(() -> {
            Autoanswer.startanswer();
            runimage.post(FloatingWindow::image_stop);
        }).start();
    }

    public static void stop() {
//        加载悬浮窗
        AcessibilityApi.AutoKeyBoard();
        Floating_windows_3();
        DG_Thread.stop();
        new Thread(new Runnable() {
            int i = 20;

            @Override
            public void run() {
                while (i-- > 0) {
                    LogUtil.W("关闭第 " + i + " 次");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (DG_Thread.get_modeThread().equals(DG_Thread.no_run)) {
                        LogUtil.D("线程成功停止");
                        Message msg = Message.obtain();
                        msg.what = 50;   //标志消息的标志
                        handler1.sendMessage(msg);
                        break;
                    }

                    DG_Thread.stop();
                }
                if (i <= 0) {
                    LogUtil.W("线程关闭失败");
                }
            }
        }).start();
    }

    //    UI线程
    public static Handler handler1 = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 50:
                    LogUtil.D("更新UI");
                    btn_start.setText("开始学习");
                    btn_stop.setTextColor(mcontext.getResources().getColor(R.color.font_common_1, null));
                    btn_stop.setEnabled(false);

                    if (EasyFloat.appFloatIsShow("3")) {
                        LogUtil.I("关闭加载悬浮窗");
                        EasyFloat.dismissAppFloat("3");
                    }
                    image_stop();
                    Toast.makeText(mcontext, "停止", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private static void hide() {
        /* Floating_windows_1();*/
        EasyFloat.hideAppFloat("2");
    }

    /*public static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ClockService", "连接成功");
            Messenger mService = new Messenger(service);
            Message message = Message.obtain(null, MyNotificationType.case3);
            Bundle bundle = new Bundle();
            bundle.putString("app", "test");
            message.setData(bundle);
            try {
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ClockService", "连接失败");
        }
    };*/

    private static void test_float() {
        EasyFloat.with(mcontext)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setGravity(Gravity.END, 0, 200)
                .setAppFloatAnimator(new AppFloatDefaultAnimator())
                .setTag("4")
                .setLayout(R.layout.test3, View -> {
                    View.findViewById(R.id.btn_test1).setOnClickListener(v ->
                            test1()
                    );

                    View.findViewById(R.id.btn_test2).setOnClickListener(v ->
                            test2());

                    View.findViewById(R.id.btn_test3).setOnClickListener(v -> test3()
                    );

                    View.findViewById(R.id.btn_test4).setOnClickListener(v -> test4()
                    );
                })
                .show();
    }

    private static void test1() {
        LogUtil.D("点击悬浮窗第1个按钮");

        List<AccessibilityNodeInfo> accessibilityNodeInfoList = AcessibilityApi.findViewByid_list("cn.xuexi.android:id/general_carousel_card_image_id");

        LogUtil.D("size:" + accessibilityNodeInfoList.size());
        AcessibilityApi.performViewClick(accessibilityNodeInfoList.get(0));
//        AcessibilityApi.ScrollNode(Objects.requireNonNull(AcessibilityApi.findViewByCls("cn.xuexi.android:id/general_carousel_card_image_id")).get(3), 1);

    }

    private static void test2() {
//        AcessibilityApi.ScrollNode(Objects.requireNonNull(AcessibilityApi.findViewByCls("android.webkit.WebView")).get(0), 1);
    }

    private static void test3() {
        LogUtil.D("点击悬浮窗第3个按钮");

        for (int i = 0; i < 5; i++) {
            List<AccessibilityNodeInfo> accessibilityNodeInfoList = AcessibilityApi.findViewByid_list("cn.xuexi.android:id/general_card_title_id");
            assert accessibilityNodeInfoList != null;
            int m = accessibilityNodeInfoList.size();
            LogUtil.I("总共：" + m);

            for (AccessibilityNodeInfo a : accessibilityNodeInfoList
            ) {
                LogUtil.D("text：" + a.getText());
            }
           AcessibilityApi.ScrollNode(Objects.requireNonNull(AcessibilityApi.findViewByCls("android.widget.ListView")).get(3), 1);

            ThreadSleepTime.sleep2();
        }
    }

    private static void test4() {
        User.getallInfottext(true);
    }


    private static void Floating_windows_3() {
        EasyFloat.with(mcontext)
                .setSidePattern(SidePattern.DEFAULT)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setGravity(Gravity.CENTER)
                .setAppFloatAnimator(new AppFloatDefaultAnimator())
                .setTag("3")
                .setDragEnable(false)
                .setLayout(R.layout.float_progressbar, View -> {
                    ProgressBar progressBar = View.findViewById(R.id.progressbar);
                    progressBar.setVisibility(android.view.View.VISIBLE);
                })
                .show();
    }

    public static void image_run() {
        xuan_zhuan();
    }

    public static void image_hui_fu() {
        drawable.start();
    }

    public static void image_zan_ting() {
        drawable.stop();
    }

    public static void image_stop() {
        runimage.setBackgroundResource(R.drawable.stop);
    }
}
