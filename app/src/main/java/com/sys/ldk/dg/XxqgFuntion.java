package com.sys.ldk.dg;

import android.view.accessibility.AccessibilityNodeInfo;

import com.sys.ldk.FloatingWindow;
import com.sys.ldk.ThreadSleepTime;
import com.sys.ldk.accessibility.api.AcessibilityApi;
import com.sys.ldk.accessibility.api.UiApi;
import com.sys.ldk.accessibility.api.User;
import com.sys.ldk.accessibility.util.LogUtil;
import com.sys.ldk.serverset.MyNotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sys.ldk.serverset.MainService.notification;

/**
 * <p>大国共用方法</p>
 *
 * @author: Nine_Dollar
 * @date: 2021/2/20
 */
public class XxqgFuntion {

    /**
     * 用于点击listview
     *
     * @param id   listview每条数据都是一样的id
     * @param text 通过text确定info
     * @return List<AccessibilityNodeInfo>
     */
    public static List<AccessibilityNodeInfo> listinfo(String id, String text) {
        int j = 0;
        List<AccessibilityNodeInfo> Idlist = new ArrayList<>();
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = AcessibilityApi.getAllNode(null, null);
        if (accessibilityNodeInfoList.isEmpty()) {
            LogUtil.W("未找到info");
            return null;
        }
        LogUtil.D("总共：" + accessibilityNodeInfoList.size());
        for (AccessibilityNodeInfo a : accessibilityNodeInfoList
        ) {
            j++;
            if (id.equals(a.getViewIdResourceName() + "")) {
                if (text.equals(accessibilityNodeInfoList.get(j).getText() + "")) {
                    LogUtil.D("text：" + a.getText());
                    Idlist.add(a);
                }
            }
        }
        if (Idlist.isEmpty()) {
            LogUtil.W("未找到");
            return null;
        }
        LogUtil.D("新闻总共：" + Idlist.size());
        return Idlist;
    }

    public static boolean back() {
//        判断三次
        for (int i = 0; i < 6; i++) {
            if (UiApi.findNodeByTextWithTimeOut(500, "学习积分") != null) {
                LogUtil.D("退回到积分页了");
                return true;
            }
            if (XXQG.isPage()) {
                if (ThreadSleepTime.sleep1()) {
                    return false;
                }
                Autoanswer.into_ji_fen_page();
                continue;
            }
            AcessibilityApi.performAction(AcessibilityApi.ActionType.BACK);
        }
        return false;
    }

    public static boolean is_video_time(String str) {
        String regex = "[0-9]{2}:[0-9]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        boolean dateFlag = m.matches();
        if (!dateFlag) {
            return false;
        }
        return true;
    }

    public static boolean fen_shu() {

        AccessibilityNodeInfo accessibilityNodeInfo = User.get_text_after_info("积分规则", 1);

        if (accessibilityNodeInfo != null) {
            LogUtil.D("积分：" + accessibilityNodeInfo.getText());
        }else {
            return false;
        }

//        回UI线程
        FloatingWindow.runimage.post(() -> {
            MyNotificationType.setMessagetitle1(accessibilityNodeInfo.getText() + "");
            notification();
        });
        return true;
    }
}
