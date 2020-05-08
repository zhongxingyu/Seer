 package com.joewoo.ontime.support.util;
 
 import android.app.Application;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 
 import com.joewoo.ontime.support.sql.MyMaidSQLHelper;
 
 import static com.joewoo.ontime.support.info.Defines.TAG;
 
 /**
  * Created by JoeWoo on 13-11-21.
  */
 public final class GlobalContext extends Application {
 
     private static GlobalContext globalContext;
     private static String accessToken;
     private static String uid;
     private static String screenName;
     private static String picFilePath;
     private static String draft;
     private static BitmapDrawable profileImg;
     private static BitmapDrawable smallProfileImg;
     private static SQLiteDatabase sql;
 
     private static final int SMALL_PROFILE_IMG_WIDTH_HEIGHT = 50;
 
     @Override
     public void onCreate() {
         Log.e(TAG, "MyMaid START!");
         super.onCreate();
 
         globalContext = this;
 
         sql = new MyMaidSQLHelper(GlobalContext.getAppContext(),
                 MyMaidSQLHelper.SQL_NAME, null, MyMaidSQLHelper.SQL_VERSION).getWritableDatabase();
 
         Cursor c = sql.query(MyMaidSQLHelper.USER_TABLE, new String[]{MyMaidSQLHelper.PROFILE_IMG, MyMaidSQLHelper.UID, MyMaidSQLHelper.SCREEN_NAME, MyMaidSQLHelper.DRAFT, MyMaidSQLHelper.ACCESS_TOKEN, MyMaidSQLHelper.PIC_FILE_PATH}, MyMaidSQLHelper.LAST_LOGIN + "=?", new String[]{"1"}, null, null, null);
 

         if (c != null && c.getCount() > 0 && c.moveToFirst()) {
             setUID(c.getString(c.getColumnIndex(MyMaidSQLHelper.UID)));
             setAccessToken(c.getString(c.getColumnIndex(MyMaidSQLHelper.ACCESS_TOKEN)));
             setScreenName(c.getString(c.getColumnIndex(MyMaidSQLHelper.SCREEN_NAME)));
             setProfileImg(c.getBlob(c.getColumnIndex(MyMaidSQLHelper.PROFILE_IMG)));
             Log.e(TAG, "Login: " + getScreenName());
             try {
                 setDraft(c.getString(c.getColumnIndex(MyMaidSQLHelper.DRAFT)));
                 setPicPath(c.getString(c.getColumnIndex(MyMaidSQLHelper.PIC_FILE_PATH)));
             } catch (Exception e) {
                 Log.e(TAG, "No Draft or Pic");
             }
             c.close();
         } else {
             Log.e(TAG, "No Last Login User Info");
         }
 
     }
 
 
     public static SQLiteDatabase getSQL() {
         return sql;
     }
 
     public static String getResString(int resId) {
         return getAppContext().getString(resId);
     }
 
     public static GlobalContext getInstance() {
         return globalContext;
     }
 
     public static Context getAppContext() {
         return globalContext.getApplicationContext();
     }
 
     public static String getAccessToken() {
         return accessToken;
     }
 
     public static void setAccessToken(String accessToken) {
         GlobalContext.accessToken = accessToken;
     }
 
     public static String getUID() {
         return uid;
     }
 
     public static void setUID(String UID) {
         GlobalContext.uid = UID;
     }
 
     public static String getScreenName() {
         return screenName;
     }
 
     public static void setScreenName(String screenName) {
         GlobalContext.screenName = screenName;
     }
 
     public static String getPicPath() {
         return picFilePath;
     }
 
     public static void setPicPath(String picPath) {
         GlobalContext.picFilePath = picPath;
     }
 
     public static String getDraft() {
         return draft;
     }
 
     public static void setDraft(String draft) {
         GlobalContext.draft = draft;
     }
 
     public static BitmapDrawable getProfileImg() {
         return profileImg;
     }
 
     public static void setProfileImg(byte[] img) {
         if (img != null)
             GlobalContext.profileImg = new BitmapDrawable(getAppContext().getResources(), BitmapFactory
                     .decodeByteArray(img, 0, img.length));
         else
             GlobalContext.profileImg = null;
 
         setSmallProfileImg();
     }
 
     public static BitmapDrawable getSmallProfileImg() {
         return smallProfileImg;
     }
 
     private static void setSmallProfileImg() {
         if (getProfileImg() != null)
             GlobalContext.smallProfileImg = new BitmapDrawable(getAppContext().getResources(), Bitmap.createScaledBitmap(getProfileImg().getBitmap(), SMALL_PROFILE_IMG_WIDTH_HEIGHT, SMALL_PROFILE_IMG_WIDTH_HEIGHT, true));
         else
             GlobalContext.smallProfileImg = null;
     }
 
     public static void clear() {
         setDraft(null);
         setPicPath(null);
         setUID(null);
         setAccessToken(null);
         setScreenName(null);
         setProfileImg(null);
     }
 
 }
