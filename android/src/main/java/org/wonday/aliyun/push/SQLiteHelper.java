package org.wonday.aliyun.push;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

/**
 * Created on 2018/10/17.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    final static String TABLE_NAME = "PushMsg";

    final static String key_msgId = "msgId";
    final static String key_msgtype = "msgtype";
    final static String key_timeString = "timeString";

    final static String key_data_title = "title";
    final static String key_data_date = "date";
    final static String key_data_time = "time";
    final static String key_data_account = "account";


    final static String key_data_body = "body";

    final static String key_data_describe = "describe";
    final static String key_data_image = "image";
    final static String key_data_redirectType = "redirectType";
    final static String key_data_linkUrl = "linkUrl";
    final static String key_data_amount = "amount";
    final static String key_data_redirectData = "redirectData";

    final static String SQL = new StringBuffer()
            .append("create table ")
            .append(TABLE_NAME)
            .append("(id integer primary key autoincrement,")
            .append(key_msgId + " integer,")
            .append(key_msgtype + " varchar(64),")
            .append(key_timeString + " varchar(64),")
            .append(key_data_title + " varchar(64),")
            .append(key_data_date + " varchar(64),")
            .append(key_data_time + " varchar(64),")
            .append(key_data_account + " varchar(64),")
            .append(key_data_body + " varchar(500),")
            .append(key_data_describe + " varchar(500),")
            .append(key_data_image + " varchar(200),")
            .append(key_data_redirectType + " varchar(64),")
            .append(key_data_linkUrl + " varchar(200),")
            .append(key_data_amount + " varchar(64),")
            .append(key_data_redirectData + " varchar(200))")
            .toString();


    private SQLiteDatabase database;
    private static SQLiteHelper instance;

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (SQLiteHelper.class) {
                if (instance == null) {
                    instance = new SQLiteHelper(context);
                }
            }
        }
        return instance;
    }

    private SQLiteHelper(@Nullable Context context) {
        super(context, "aliyun_push", null, 1);
        database = getWritableDatabase();
    }

    public void insertMsg(String content) {
        try {
            JSONObject object = new JSONObject(content);
            String msgtype = object.optString(key_msgtype);
            String msgId = object.optString(key_msgId);
            String timeString = object.optString(key_timeString);

            JSONObject data = object.optJSONObject("data");
            Log.v(getClass().getName(), data.toString());
            String account = data.optString(key_data_account);
            String title = data.optString(key_data_title);
            String date = data.optString(key_data_date);
            String time = data.optString(key_data_time);
            if ("url".equals(msgtype)) {
                String describe = data.optString(key_data_describe);
                String image = data.optString(key_data_image);
                String linkUrl = data.optString(key_data_linkUrl);
                insertAccountUrl(msgtype, msgId, timeString, title, date, time, account,
                        describe, image, linkUrl);
            } else if ("text".equals(msgtype)) {
                String body = data.optString(key_data_body);
                insertAccountText(msgtype, msgId, timeString, title, date, time, account,
                        body);
            } else if ("account_notice".equals(msgtype)) {
                String amount = data.optString(key_data_amount);
                String body = data.optString(key_data_body);
                String describe = data.optString(key_data_describe);
                String redirectType = data.optString(key_data_redirectType);
                String redirectData = data.optString(key_data_redirectData);
                insertAccountNotice(msgtype, msgId, timeString, title, date, time, account,
                        amount, body, describe, redirectType, redirectData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertAccountUrl(String msgtype, String msgId, String timeString,
                                  String title, String date, String time, String account,
                                  String describe, String image, String linkUrl) {
        ContentValues values = createValues(msgtype, msgId, timeString, title, date, time, account);
        values.put(key_data_describe, describe);
        values.put(key_data_image, image);
        values.put(key_data_linkUrl, linkUrl);
        database.insert(TABLE_NAME, null, values);
    }

    private void insertAccountText(String msgtype, String msgId, String timeString,
                                   String title, String date, String time, String account,
                                   String body) {
        ContentValues values = createValues(msgtype, msgId, timeString, title, date, time, account);
        values.put(key_data_body, body);
        database.insert(TABLE_NAME, null, values);
    }

    private void insertAccountNotice(String msgtype, String msgId, String timeString,
                                     String title, String date, String time, String account,
                                     String amount, String body, String describe, String redirectType, String redirectData) {
        ContentValues values = createValues(msgtype, msgId, timeString, title, date, time, account);
        values.put(key_data_amount, amount);
        values.put(key_data_body, body == null ? "" : body.replace(" ", ""));//body
        values.put(key_data_describe, describe == null ? "" : describe.replace(" ", ""));//describe
        values.put(key_data_redirectType, redirectType);
        values.put(key_data_redirectData, redirectData);
        database.insert(TABLE_NAME, null, values);
    }

    private ContentValues createValues(String msgtype, String msgId, String timeString,
                                       String title, String date, String time, String account) {
        ContentValues values = new ContentValues();
        values.put(key_msgtype, msgtype);
        values.put(key_msgId, msgId);
        values.put(key_timeString, timeString);

        values.put(key_data_time, title);
        values.put(key_data_date, date);
        values.put(key_data_time, time);
        values.put(key_data_account, account);
        return values;
    }

    public WritableArray query(String keyAccount, int page, int size) {
        String limit = (page - 1) * size + "," + size;
        String selection = key_data_account + " = ?";
        String orderBy = key_timeString + " DESC";
        String[] selectionArgs = new String[]{keyAccount};
        Cursor cursor = database.query(TABLE_NAME, null, "" + selection, selectionArgs, null, null,
                "" + orderBy, "" + limit);
        WritableArray array = Arguments.createArray();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String msgId = cursor.getString(cursor.getColumnIndex(key_msgId));
                String msgtype = cursor.getString(cursor.getColumnIndex(key_msgtype));
                long timeString = cursor.getLong(cursor.getColumnIndex(key_timeString));
                String title = cursor.getString(cursor.getColumnIndex(key_data_title));
                String date = cursor.getString(cursor.getColumnIndex(key_data_date));
                String time = cursor.getString(cursor.getColumnIndex(key_data_time));


                String account = cursor.getString(cursor.getColumnIndex(key_data_account));
                String body = cursor.getString(cursor.getColumnIndex(key_data_body));
                String describe = cursor.getString(cursor.getColumnIndex(key_data_describe));
                String image = cursor.getString(cursor.getColumnIndex(key_data_image));
                String redirectType = cursor.getString(cursor.getColumnIndex(key_data_redirectType));
                String linkUrl = cursor.getString(cursor.getColumnIndex(key_data_linkUrl));
                String amount = cursor.getString(cursor.getColumnIndex(key_data_amount));
                String redirectData = cursor.getString(cursor.getColumnIndex(key_data_redirectData));
                WritableMap object = createHeader(msgId, msgtype, timeString);
                if ("url".equals(msgtype)) {
                    object.putMap("data", createMapUrl(title, date, time, account,
                            describe, image, linkUrl));
                } else if ("text".equals(msgtype)) {
                    object.putMap("data", createMapText(title, date, time, account,
                            body));
                } else if ("account_notice".equals(msgtype)) {
                    object.putMap("data", createMapAccountNotice(title, date, time, account,
                            amount, body, describe, redirectType, redirectData));
                }
                array.pushMap(object);
//                JSONObject s = new JSONObject();
//                try {
//                    s.put(key_msgId, msgId);
//                    s.put(key_msgtype, msgtype);
//                    s.put(key_timeString, timeString);
//                    s.put(key_data_title, title);
//                    s.put(key_data_date, date);
//                    s.put(key_data_time, time);
//
//                    s.put(key_data_account, account);
//                    s.put(key_data_body, body);
//                    s.put(key_data_describe, describe);
//                    s.put(key_data_image, image);
//                    s.put(key_data_redirectType, redirectType);
//                    s.put(key_data_linkUrl, linkUrl);
//                    s.put(key_data_amount, amount);
//                    s.put(key_data_redirectData, redirectData);
//
//                    Log.v(getClass().getName(), "\n\n" + id);
//                    Log.v(getClass().getName(), s.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        }

        return array;
    }
    public void delete(){
        String selection = key_data_account + " = ?";
        String[] selectionArgs = new String[]{""};
        database.delete(TABLE_NAME,null,null);
    }
    WritableMap createMapData(String title, String date, String time, String account) {
        WritableMap data = Arguments.createMap();
        data.putString(key_data_title, title);
        data.putString(key_data_date, date);
        data.putString(key_data_time, time);
        data.putString(key_data_account, account);
        return data;
    }

    WritableMap createMapUrl(String title, String date, String time, String account,
                             String describe, String image, String linkUrl) {
        WritableMap url = createMapData(title, date, time, account);

        url.putString(key_data_describe, describe);
        url.putString(key_data_image, image);
        url.putString(key_data_linkUrl, linkUrl);
        return url;
    }

    WritableMap createMapText(String title, String date, String time, String account,
                              String body) {
        WritableMap text = createMapData(title, date, time, account);

        text.putString(key_data_body, body);
        return text;
    }

    WritableMap createMapAccountNotice(String title, String date, String time, String account,
                                       String amount, String body, String describe, String redirectType, String redirectData) {
        WritableMap text = createMapData(title, date, time, account);

        text.putString(key_data_amount, amount);
        text.putString(key_data_body, body);
        text.putString(key_data_describe, describe);
        text.putString(key_data_redirectType, redirectType);
        text.putString(key_data_redirectData, redirectData);
        return text;
    }

    WritableMap createHeader(String msgId, String msgtype, long timeString) {

        WritableMap map = Arguments.createMap();
        map.putString(key_msgId, msgId);
        map.putString(key_msgtype, msgtype);
        map.putDouble(key_timeString, timeString);
        return map;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(getClass().getName(), SQL);
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
