package com.sys.ldk.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

    /**
     * @param JsonString 一个json格式的字符串
     * @return 正常返回一个json对象 异常返回 null
     * @Summary 获取一个json对象
     */
    public static synchronized JSONObject getJsonObject(String JsonString) {
        JSONObject jsonObject = null;
        try {
            JsonString = getJsonStrFromNetData(JsonString);
            JSONArray entries = new JSONArray(JsonString);
            if (entries.length() > 0) {
                jsonObject = entries.getJSONObject(0);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param JsonString json格式的字符串
     * @return 返回Json 对象的数组
     * @Summary 获取json 对象的数组
     */
    public static synchronized List getJsonObjects(String JsonString) {
        JsonString = getJsonStrFromNetData(JsonString);
        ArrayList array = new ArrayList();
        try {
            JSONArray entries = new JSONArray(JsonString);
            for (int i = 0; i < entries.length(); i++) {
                JSONObject jsObject = entries.getJSONObject(i);
                if (jsObject != null) {
                    array.add(jsObject);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

    /**
     * @param jsonString 包含Json字符串的数据
     * @return json字符串
     * @summary 去除非Json的字符串部分
     */
    public static synchronized String getJsonStrFromNetData(String jsonString) {
        int first = jsonString.indexOf("[");
        int last = jsonString.lastIndexOf("]");
        String result = "";
        if (last > first) {
            result = jsonString.substring(first, last + 1);
        }
        return result;
    }
}