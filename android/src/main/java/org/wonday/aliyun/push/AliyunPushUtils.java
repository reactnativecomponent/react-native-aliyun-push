package org.wonday.aliyun.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created on 2018/7/17.
 */

public class AliyunPushUtils {

    public final static String publicmsg = "publicmsg";
    public final static String privatemsg = "privatemsg";

    public static WritableMap createMsg(File file) {
        WritableMap map = Arguments.createMap();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            buffer = outputStream.toByteArray();
            JSONObject msg = new JSONObject(new String(buffer));

            for (Iterator<String> it = msg.keys(); it.hasNext(); ) {
                String key = it.next();
                map.putString(key, msg.optString(key));
            }
            map.putDouble("timestramp", Long.valueOf(file.getName()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static List<WritableMap> createListMsg(File dir) {
        List<WritableMap> data = new ArrayList<>();
        if (dir.exists() && dir.listFiles() != null) {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    data.add(createMsg(f));
                } else if (f.isDirectory()) {
                    data.addAll(createListMsg(f));
                }
            }
        }
        return data;
    }

    public static File getAliyunPushDir(Context context) {
        return new File(context.getFilesDir(), context.getPackageName());/*context.getFilesDir() Environment.getExternalStorageDirectory()*/
    }

    public static void saveAliyunPushMessage(Context context, String eventName, String type, CPushMessage cPushMessage) {
        String messageId = cPushMessage.getMessageId();
        String body = cPushMessage.getContent();
        String title = cPushMessage.getTitle();
        String appId = cPushMessage.getAppId();
        String traceInfo = cPushMessage.getTraceInfo();
        JSONObject object = new JSONObject();
        FileOutputStream outputStream = null;
        try {
            JSONObject bodyJson = new JSONObject(body);
            JSONObject data = bodyJson.optJSONObject("data");
            String account = null;
            if (data != null) {
                account = data.optString("account");
            }
            object.put("messageId", messageId);
            object.put("body", body);
            object.put("title", title);
            object.put("appId", appId);
            object.put("traceInfo", traceInfo);
            File dir = new File(getAliyunPushDir(context), TextUtils.isEmpty(account) ? publicmsg : privatemsg + "/" + account);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Log.v("saveAliyunPushMessage", dir.getAbsolutePath());
            String fn = "" + System.currentTimeMillis();
            outputStream = new FileOutputStream(new File(dir, fn));
            outputStream.write(object.toString().getBytes());
            outputStream.flush();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static WritableMap createBody(String content){
        try {
            JSONObject object = new JSONObject(content);
            String msgtype = object.optString(SQLiteHelper.key_msgtype);
            String msgId = object.optString(SQLiteHelper.key_msgId);
            String timeString = object.optString(SQLiteHelper.key_timeString);

            JSONObject data = object.optJSONObject("data");
            Log.v(AliyunPushUtils.class.getName(), data.toString());
            String account = data.optString(SQLiteHelper.key_data_account);
            String title = data.optString(SQLiteHelper.key_data_title);
            String date = data.optString(SQLiteHelper.key_data_date);
            String time = data.optString(SQLiteHelper.key_data_time);
            WritableMap obj = createHeader(msgId, msgtype, timeString);


            if ("url".equals(msgtype)) {
                String describe = data.optString(SQLiteHelper.key_data_describe);
                String image = data.optString(SQLiteHelper.key_data_image);
                String linkUrl = data.optString(SQLiteHelper.key_data_linkUrl);
                obj.putMap("data", createMapUrl(title, date, time, account,
                        describe, image, linkUrl));
            } else if ("text".equals(msgtype)) {
                String body = data.optString(SQLiteHelper.key_data_body);
                obj.putMap("data", createMapText(title, date, time, account,
                        body));
            } else if ("account_notice".equals(msgtype)) {
                String amount = data.optString(SQLiteHelper.key_data_amount);
                String body = data.optString(SQLiteHelper.key_data_body);
                String describe = data.optString(SQLiteHelper.key_data_describe);
                String redirectType = data.optString(SQLiteHelper.key_data_redirectType);
                String redirectData = data.optString(SQLiteHelper.key_data_redirectData);
                obj.putMap("data", createMapAccountNotice(title, date, time, account,
                        amount, body, describe, redirectType, redirectData));
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arguments.createMap();
    }
    public static WritableMap createMapData(String title, String date, String time, String account) {
        WritableMap data = Arguments.createMap();
        data.putString(SQLiteHelper.key_data_title, title);
        data.putString(SQLiteHelper.key_data_date, date);
        data.putString(SQLiteHelper.key_data_time, time);
        data.putString(SQLiteHelper.key_data_account, account);
        return data;
    }

    public static WritableMap createMapUrl(String title, String date, String time, String account,
                             String describe, String image, String linkUrl) {
        WritableMap url = createMapData(title, date, time, account);

        url.putString(SQLiteHelper.key_data_describe, describe);
        url.putString(SQLiteHelper.key_data_image, image);
        url.putString(SQLiteHelper.key_data_linkUrl, linkUrl);
        return url;
    }

    public static WritableMap createMapText(String title, String date, String time, String account,
                              String body) {
        WritableMap text = createMapData(title, date, time, account);

        text.putString(SQLiteHelper.key_data_body, body);
        return text;
    }

    public static WritableMap createMapAccountNotice(String title, String date, String time, String account,
                                       String amount, String body, String describe, String redirectType, String redirectData) {
        WritableMap text = createMapData(title, date, time, account);

        text.putString(SQLiteHelper.key_data_amount, amount);
        text.putString(SQLiteHelper.key_data_body, body);
        text.putString(SQLiteHelper.key_data_describe, describe);
        text.putString(SQLiteHelper.key_data_redirectType, redirectType);
        text.putString(SQLiteHelper.key_data_redirectData, redirectData);
        return text;
    }

    public static WritableMap createHeader(String msgId, String msgtype, String timeString) {

        WritableMap map = Arguments.createMap();
        map.putString(SQLiteHelper.key_msgId, msgId);
        map.putString(SQLiteHelper.key_msgtype, msgtype);
        map.putString(SQLiteHelper.key_timeString, timeString);
        return map;
    }
}
