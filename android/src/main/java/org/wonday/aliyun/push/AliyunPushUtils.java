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
}
