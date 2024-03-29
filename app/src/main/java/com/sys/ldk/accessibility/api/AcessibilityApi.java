package com.sys.ldk.accessibility.api;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import com.sys.ldk.MainActivity;
import com.sys.ldk.accessibility.util.LogUtil;
import com.sys.ldk.serverset.MainService;

import java.util.ArrayList;
import java.util.List;

/**
 * 辅助功能API
 */
public class AcessibilityApi {
    public enum ActionType {
        BACK,  //返回键
        HOME,  //home
        SETTING,  //设置
        POWER,  //锁屏
        RECENTS,  //应用列表
        NOTIFICATIONS, //通知
        SCROLL_BACKWARD,  //下滑
        SCROLL_FORWARD, //上划
    }

    private static AccessibilityEvent mAccessibilityEvent = null;
    public static AccessibilityService mAccessibilityService = null;


    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AcessibilityApi.context = context;
    }


    /**
     * 设置数据
     *
     * @param service
     * @param
     */
    public static void setAccessibilityService(AccessibilityService service) {
        synchronized (AcessibilityApi.class) {
            if (service != null && mAccessibilityService == null) {
                mAccessibilityService = service;
            }

        }
    }

    public static void setAccessibilityEvent(AccessibilityEvent event) {
        synchronized (AcessibilityApi.class) {
            if (event != null && mAccessibilityEvent == null) {
                mAccessibilityEvent = event;
            }
        }
    }


    public static int getMaxScrollY() {
        return mAccessibilityEvent.getMaxScrollY();
    }

    public static int getMaxScrollX() {
        return mAccessibilityEvent.getMaxScrollX();
    }


    /**
     * 模拟点击系统相关操作
     */
    public static void performAction(ActionType action) {
        if (mAccessibilityService == null) {
            return;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (action) {
            case BACK:
                LogUtil.I("返回");
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

                break;
            case HOME:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

                break;
            case RECENTS:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);

                break;
            case NOTIFICATIONS:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);

                break;

            case POWER:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);

                break;

            case SETTING:
                mAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);


                break;
            case SCROLL_BACKWARD:
                mAccessibilityService.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);


                break;
            case SCROLL_FORWARD:
                mAccessibilityService.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                LogUtil.I("上滑");
                break;
        }
    }


    //==============================上层api========================================

    /**
     * 根据text查找并点击该节点
     *
     * @param text
     */
    public static boolean clickTextViewByText(String text) {
        boolean flg = false;
        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return flg;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    if (flg = performViewClick(nodeInfo)) {
                        LogUtil.D("已点击");
                    } else {
                        LogUtil.D("未点击");
                    }
                    break;
                }
            }
        } else {
            LogUtil.E("未找到:" + text + "Info");
        }
        return flg;
    }

    /**
     * 根据Id查找并点击该节点
     *
     * @param id
     * @return
     */
    public static boolean clickTextViewByID(String id) {

        boolean flg = false;
        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return flg;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    flg = performViewClick(nodeInfo);
                    break;
                }
            }
        }

        return flg;
    }

    //      查找id获取view
    public static List<AccessibilityNodeInfo> findViewByid_list(String id) {
        List<AccessibilityNodeInfo> ilist = new ArrayList<>();
        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    ilist.add(nodeInfo);
                }
            }
        }

        return ilist;
    }


    //    滑动
    public static boolean ScrollNode(AccessibilityNodeInfo nodeInfo,int direction) {
        boolean flg = false;
        if (nodeInfo == null) {
            LogUtil.E("NodeInfo为空");
            return flg;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isScrollable()) {
                LogUtil.D("可滚动");
                if(direction >= 1){
                    flg = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }else {
                    flg = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                }
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }

        return flg;
    }


    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public static boolean inputTextByNode(AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }

    }


    /**
     * 模拟点击某个节点
     *
     * @param nodeInfo nodeInfo
     */
    public static boolean performViewClick(AccessibilityNodeInfo nodeInfo) {
        boolean flg = false;
        if (nodeInfo == null) {
            LogUtil.E("nodeInfo为空");
            return flg;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                LogUtil.D("nodeInfo可点击");
                flg = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
            LogUtil.E("nodeInfo不可点击,查找父节点");
        }
        return flg;
    }


    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    public static AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && nodeInfo.getText() != null && nodeInfo.getText().toString().equals(text)) {
                    return nodeInfo;
                }
            }
        }

        return null;
    }


    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 根据描述查找控件
     *
     * @param des
     * @return
     */
    public static AccessibilityNodeInfo findViewByDes(String des) {
        if (des == null || "".equals(des)) {
            return null;
        }
        List<AccessibilityNodeInfo> lists = getAllNode(null, null);

        for (AccessibilityNodeInfo node : lists) {
            CharSequence desc = node.getContentDescription();
            if (desc != null && des.equals(desc.toString())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 根据类名模糊查找控件
     *
     * @param cls
     * @return
     */
    public static List<AccessibilityNodeInfo> findViewByCls(String cls) {
        List<AccessibilityNodeInfo> mlist = new ArrayList<>();
        if (cls == null || "".equals(cls)) {
            return null;
        }
        List<AccessibilityNodeInfo> lists = getAllNode(null, null);

        for (AccessibilityNodeInfo node : lists) {
            CharSequence desc = node.getClassName();
            if (desc != null && cls.equals(desc.toString())) {
                mlist.add(node);
            }
        }
        return mlist;
    }

    /**
     * 获取根节点
     *
     * @return
     */
    public static AccessibilityNodeInfo getRootNodeInfo() {
        AccessibilityEvent curEvent = mAccessibilityEvent;
        AccessibilityNodeInfo nodeInfo = null;

        // 建议使用getRootInActiveWindow，这样不依赖当前的事件类型
        if (mAccessibilityService != null) {
            nodeInfo = mAccessibilityService.getRootInActiveWindow();
        }

        return nodeInfo;
    }

    /**
     * 根据Text搜索所有符合条件的节点, 模糊搜索方式
     */
    public static List<AccessibilityNodeInfo> findNodesByText(String text) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (list != null || list.size() > 0) {
                return list;

            }
        }
        return null;

    }


    public static String getEventPkg() {
        if (mAccessibilityService != null) {
            AccessibilityNodeInfo node = getRootNodeInfo();
            if (node != null) {
                return node.getPackageName() == null ? "" : node.getPackageName().toString();
            }
        }
        return "";
    }


    /**
     * 根据Text搜索所有符合条件的节点, 模糊搜索方式
     */
    public static AccessibilityNodeInfo findNodesById(String text) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (list != null || list.size() > 0) {
                return list.get(0);

            }
        }
        return null;

    }

    /**
     * 查找所有节点,比较耗时
     *
     * @param node
     * @param lists
     * @return List<AccessibilityNodeInfo>
     */
    public static List<AccessibilityNodeInfo> getAllNode(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> lists) {
        if (lists == null) {
            lists = new ArrayList<>();
        }
        if (node == null) {
            node = getRootNodeInfo();
        }

        if (node != null) {
            int childNum = node.getChildCount();
            if (childNum > 0) {
                for (int i = 0; i < childNum; i++) {
                    AccessibilityNodeInfo nodeInfo = node.getChild(i);
                    if (nodeInfo != null) {
                        getAllNode(nodeInfo, lists);
                        lists.add(nodeInfo);
                    }
                }
            } else {
                return lists;
            }
        }
        return lists;
    }

    /**
     * 关闭软件盘,需要7.0版本
     */
    public static void closeKeyBoard() {
        if (mAccessibilityService != null) {
            AccessibilityService.SoftKeyboardController softKeyboardController = mAccessibilityService.getSoftKeyboardController();
            softKeyboardController.setShowMode(AccessibilityService.SHOW_MODE_HIDDEN);
        }
    }

    /**
     * 自动返回true
     *
     * @return
     */
    public static boolean AutoKeyBoard() {
        if (mAccessibilityService != null) {
            AccessibilityService.SoftKeyboardController softKeyboardController = mAccessibilityService.getSoftKeyboardController();
            if (softKeyboardController.getShowMode() == AccessibilityService.SHOW_MODE_HIDDEN) {
                softKeyboardController.setShowMode(AccessibilityService.SHOW_MODE_AUTO);
                return true;
            }
        }
        return false;
    }


    /**
     * 获取节点所有包含的动作类型
     *
     * @param node
     */
    public static void getNodeActions(AccessibilityNodeInfo node) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<AccessibilityNodeInfo.AccessibilityAction> accessibilityActions = node.getActionList();

            for (AccessibilityNodeInfo.AccessibilityAction action : accessibilityActions) {

                System.out.print(action.toString());
            }
        }
    }

}
