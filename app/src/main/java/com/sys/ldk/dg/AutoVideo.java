package com.sys.ldk.dg;

import android.view.accessibility.AccessibilityNodeInfo;

import com.sys.ldk.ThreadSleepTime;
import com.sys.ldk.accessibility.api.AcessibilityApi;
import com.sys.ldk.accessibility.api.UiApi;
import com.sys.ldk.accessibility.api.User;
import com.sys.ldk.accessibility.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.sys.ldk.dg.LdkConfig.getVideo_time_second;
import static com.sys.ldk.dg.ReturnType.SUCCESS;
import static com.sys.ldk.dg.ReturnType.my_stop;
import static java.sql.Types.NULL;

public class AutoVideo {
    private static int q = 0;//次数
    private static int time = 0;

    public static boolean auto_video() {
        if (!AcessibilityApi.clickTextViewByText("电视台")) {
            return false;
        }
        if (ThreadSleepTime.sleep2()) {
            return false;
        }
        return startvideo(Objects.requireNonNull(di_yi_ping_dao()));
    }

    public static boolean start_duan_video() {
        List<String> stringList = User.getallInfottext(false);
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = AcessibilityApi.getAllNode(null, null);
        HashMap<String[],AccessibilityNodeInfo> hashMap = new HashMap<>();
        HashMap<String[],Integer> hashMap1 = new HashMap<>();

        for (String s : stringList
        ) {
            if (XxqgFuntion.is_video_time(s)) {
                String[] strings = {s, "null"};
                hashMap1.put(strings, 1);
                hashMap = User.get_alter_info(accessibilityNodeInfoList, hashMap1);
            }
        }
        if(hashMap.isEmpty()){
            LogUtil.W("未找到短视频");
            return false;
        }

        Object myKey = hashMap.keySet().toArray()[0];
        AcessibilityApi.performViewClick(hashMap.get(myKey));

        long time = LdkConfig.getDuan_video_time_second();
        LogUtil.D("观看短视频：" + time+ " 分钟");
        if (ThreadSleepTime.sleep(LdkConfig.getDuan_video_time_Mill())) {
            return false;
        }

        LogUtil.D("短视频观看结束");
        AcessibilityApi.performAction(AcessibilityApi.ActionType.BACK);

        return true;
    }

    public static boolean startvideo(List<AccessibilityNodeInfo> accessibilityNodeInfos) {

        if (accessibilityNodeInfos.isEmpty()) {
            return false;
        }
        LogUtil.D("总共：" + accessibilityNodeInfos.size() + " 个视频");
        for (AccessibilityNodeInfo a : accessibilityNodeInfos
        ) {
            LogUtil.D("text: " + a.getText());
        }

        for (AccessibilityNodeInfo a : accessibilityNodeInfos
        ) {
            q++;
            //最大观看时间
            if (q > LdkConfig.getVideoing_times()) {
                LogUtil.I("到达视频最大观看次数");
                if (ThreadSleepTime.sleep0D5()) {
                    return false;
                }
                break;
            }
            int video_max = getVideo_time_second();
            LogUtil.D("开始观看第 " + q + " 个视频");
            LogUtil.D("text: " + a.getText());
            if (ThreadSleepTime.sleep0D5()) {
                return false;
            }
            UiApi.clickNodeWithTimeOut(0, a);
            if (UiApi.findNodeByTextWithTimeOut(0, "欢迎发表你的观点") == null) {
                LogUtil.W("观看失败");
                return false;
            } else {
                LogUtil.I("正在观看");
            }
//            流量播放
            AcessibilityApi.performViewClick(UiApi.findNodeByTextWithTimeOut(0, "继续播放"));

            LogUtil.I("观看视频：" + video_max + "秒");
            while (true) {
                LogUtil.V("睡眠：" + time + "秒");

                int i = watch_end();
                if (time++ > video_max || i == SUCCESS) {
                    break;
                } else if (i == my_stop) {
                    return false;
                }

            }
            time = 0;
            LogUtil.D("第" + q + " 个视频结束");
            AcessibilityApi.performAction(AcessibilityApi.ActionType.BACK);
        }
        q = 0;
        LogUtil.D("视频观看完毕");
        return true;
    }

    //    观看是否结束
    private static int watch_end() {
        if (AcessibilityApi.findViewByText("重新播放") != null) {
            LogUtil.D("观看完毕");
            return SUCCESS;
        } else {
            if (ThreadSleepTime.sleep1()) {
                return my_stop;
            }
        }
        return NULL;
    }


    /**
     * 获取第一视频里的info
     *
     * @return
     */
    public static List<AccessibilityNodeInfo> di_yi_ping_dao() {
        int sum = 0;
        int j = 0;
        int i = 0;
        List<AccessibilityNodeInfo> timeinfo = new ArrayList<>();
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = AcessibilityApi.getAllNode(null, null);
        sum = accessibilityNodeInfoList.size() - 2;
        for (AccessibilityNodeInfo a : accessibilityNodeInfoList
        ) {
            if (j < sum) {
                i = j;
            }
            if (XxqgFuntion.is_video_time(a.getText() + "")) {
                if ("中央广播电视总台".equals(accessibilityNodeInfoList.get(i - 2).getText() + "") || "中央广播电视总台".equals(accessibilityNodeInfoList.get(i + 2).getText() + "")) {
                    timeinfo.add(a);
                }
            }
            i = 0;
            j++;
        }
        if (timeinfo.isEmpty()) {
            return null;
        }
        LogUtil.D("第一视频总数：" + timeinfo.size());
        return timeinfo;
    }
}
