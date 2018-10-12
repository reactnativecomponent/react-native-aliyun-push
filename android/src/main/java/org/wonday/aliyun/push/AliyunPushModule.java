/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.wonday.aliyun.push;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;

import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

public class AliyunPushModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final ReactApplicationContext context;
    private int badgeNumber;

    public AliyunPushModule(ReactApplicationContext reactContext) {

        super(reactContext);
        this.context = reactContext;
        this.badgeNumber = 0;
        AliyunPushMessageReceiver.context = reactContext;
        ThirdPartMessageActivity.context = reactContext;

        context.addLifecycleEventListener(this);

    }

    //module name
    @Override
    public String getName() {
        return "AliyunPush";
    }

    @ReactMethod
    public void getDeviceId(final Promise promise) {
        String deviceID = PushServiceFactory.getCloudPushService().getDeviceId();
        if (deviceID!=null && deviceID.length()>0) {
            promise.resolve(deviceID);
        } else {
            // 或许还没有初始化完成，等3秒钟再次尝试
            try{
                Thread.sleep(3000);
                deviceID = PushServiceFactory.getCloudPushService().getDeviceId();

                if (deviceID!=null && deviceID.length()>0) {
                    promise.resolve(deviceID);
                    return;
                }
            } catch (Exception e) {

            }

            promise.reject("getDeviceId() failed.");
        }
    }

    @ReactMethod
    public void setApplicationIconBadgeNumber(int badgeNumber, final Promise promise) {

        if (MIUIUtils.isMIUI()) { //小米特殊处理
            FLog.d(ReactConstants.TAG, "setApplicationIconBadgeNumber for xiaomi");

            if (badgeNumber==0) {
                promise.resolve("");
                return;
            }

            try {

                MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
                this.badgeNumber = badgeNumber;
                promise.resolve("");

            } catch (Exception e) {

                promise.reject(e.getMessage());

            }


        } else {
            FLog.d(ReactConstants.TAG, "setApplicationIconBadgeNumber for normal");

            try {
                ShortcutBadger.applyCount(this.context, badgeNumber);
                this.badgeNumber = badgeNumber;
                promise.resolve("");
            } catch (Exception e){
                promise.reject(e.getMessage());
            }
        }

    }

    @ReactMethod
    public void getApplicationIconBadgeNumber(Callback callback) {
        callback.invoke(this.badgeNumber);
    }

    @ReactMethod
    public void bindAccount(String account, final Promise promise) {
        PushServiceFactory.getCloudPushService().bindAccount(account, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void unbindAccount(final Promise promise) {
        PushServiceFactory.getCloudPushService().unbindAccount(new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void bindTag(int target, ReadableArray tags, String alias, final Promise promise) {

        String[] tagStrs = new String[tags.size()];
        for(int i=0; i<tags.size();i++) tagStrs[i] = tags.getString(i);

        PushServiceFactory.getCloudPushService().bindTag(target, tagStrs, alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void unbindTag(int target, ReadableArray  tags, String alias, final Promise promise) {

        String[] tagStrs = new String[tags.size()];
        for(int i=0; i<tags.size();i++) tagStrs[i] = tags.getString(i);

        PushServiceFactory.getCloudPushService().unbindTag(target, tagStrs, alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void listTags(int target, final Promise promise) {
        PushServiceFactory.getCloudPushService().listTags(target, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void addAlias(String alias, final Promise promise) {
        PushServiceFactory.getCloudPushService().addAlias(alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void removeAlias(String alias, final Promise promise) {
        PushServiceFactory.getCloudPushService().removeAlias(alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void listAliases(final Promise promise) {
        PushServiceFactory.getCloudPushService().listAliases(new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }
    @ReactMethod
    public void getAllNotificationMessages(Promise promise){
        WritableArray array = Arguments.createArray();
        if (AliyunPushMessageReceiver.instance != null) {
            for (Map<String, Object> map : AliyunPushMessageReceiver.instance.array) {
                WritableMap item = Arguments.createMap();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        item.putString(entry.getKey(), (String) entry.getValue());
                    } else if (value instanceof HashMap) {
                        WritableMap item2 = Arguments.createMap();
                        Map<String, String> map2 = (Map<String, String>) value;
                        for (Map.Entry<String, String> entry2 : map2.entrySet()) {
                            item2.putString(entry2.getKey(), entry2.getValue());
                        }
                        item.putMap(entry.getKey(), item2);
                    }
                }
                array.pushMap(item);
            }
            promise.resolve(array);
        } else {
            promise.resolve(array);
        }
    }

    @Override
    public void onHostResume() {
        ThirdPartMessageActivity.mainClass = getCurrentActivity().getClass();
    }

    @Override
    public void onHostPause() {

        //小米特殊处理, 处于后台时更新角标， 否则会被系统清除，看不到
        if (MIUIUtils.isMIUI()) {
            FLog.d(ReactConstants.TAG, "onHostPause:setBadgeNumber for xiaomi");
            MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
        }

    }

    @Override
    public void onHostDestroy() {

        //小米特殊处理, 处于后台时更新角标， 否则会被系统清除，看不到
        if (MIUIUtils.isMIUI()) {
            FLog.d(ReactConstants.TAG, "onHostDestroy:setBadgeNumber for xiaomi");
            MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
        }

    }

}