package com.sys.ldk.accessibility.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.sys.ldk.shellService.SocketClient;

/**
 * api 内部需要用到的类
 */
public class ApiUtil {

    /**
     * 判断指定的应用的辅助功能是否开启,
     *
     * @param context 上下文
     * @param
     * @return 是否开启
     */
    public static boolean isAccessibilityServiceOn(@NonNull Context context, Class cls) {
        int ok = 0;
        String serName = context.getPackageName() + "/" + cls.getCanonicalName();

        try {
            ok = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
        if (ok == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    if (accessibilityService.equalsIgnoreCase(serName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * 触发系统rebind通知监听服务
     *
     * @param context      上下文
     * @param serviceClass 辅助功能服务的类
     */
    public static void rebindAccessibilityService(Context context, Class serviceClass) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, serviceClass),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
        pm.setComponentEnabledSetting(
                new ComponentName(context, serviceClass),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    /**
     * 通过命令关闭软键盘
     */
    public static void closeKeyBorad() {

        String cmdStr = "input keyevent 111 ";
        execRootCmdSilent(cmdStr);
    }

    /**
     * 全局滑动操作
     */
    public static void perforGlobalSwipe(int x0, int y0, int x1, int y1) {

        String cmd = "input touchscreen swipe " + x0 + " " + y0 + " " + x1 + " " + y1;

        execRootCmdSilent(cmd);

    }


    /**
     * 全局点击
     */
    public static void perforGlobalClick(float x, float y) {

        String cmd = "input tap " + x + " " + y;

        execRootCmdSilent(cmd);

    }


    /**
     * 执行命令但不关注结果输出
     */

    public static void execRootCmdSilent(final String cmd) {
       /* DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("手机未root");
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/

        if (TextUtils.isEmpty(cmd)) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClient(cmd, new SocketClient.onServiceSend() {
                    @Override
                    public void getSend(String result) {

                    }
                });
            }
        }).start();
    }

    /**
     * 线程睡眠，以毫秒为单位
     * @param time
     * @return 抛出异常为trun
     */
    public static boolean sleepTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LogUtil.I("抛出线程中断");
            e.printStackTrace();
            return true;
        }
        return false;
    }

}
