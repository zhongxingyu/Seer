 package com.lovepig.engine.database;
 
 import java.io.File;
 
 import android.database.sqlite.SQLiteDatabase;
 
 import com.lovepig.main.Configs;
 import com.lovepig.utils.LogInfo;
 
 public class DBEngine {
     static {
         close();
         create();
     }
     public static SQLiteDatabase db;
     public static final String TAG = "DBEngine";
 
     public static void create() {
         try {
             File file = new File("/data/data/com.lovepig.main/databases/");
             if (!file.isDirectory()) {
                 file.mkdirs();
             }
             db = SQLiteDatabase.openDatabase("/data/data/com.lovepig.main/databases/" + Configs.DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
         } catch (Exception e) {
             try {
                 LogInfo.LogOut(TAG + " open db error and create db start ");
                 db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.lovepig.main/databases/" + Configs.DATABASE_NAME, null);
                 createOnlineNews();
                 createNewsDetails();
                 createPigFactory();
                 db.setVersion(1);
                 LogInfo.LogOut(TAG + " open db error and create db end");
             } catch (Exception e1) {// 捕获手机sdCard不存在的异常
                 e1.printStackTrace();
             }
         }
     }
 
     /**
      * 在线新闻 public String title;// 标题 public String summary;// 简介 public String
      * order; public int id; public int top; public String iconPath;//新闻图片
      * 
      * @return
      */
     private static boolean createOnlineNews() {
         try {
             db.execSQL("CREATE TABLE onlinenews (" + "table_id INTEGER PRIMARY KEY autoincrement, " + // 表id
                     "id INTEGER ," + // 新闻序号
                     "title TEXT ," + // 视频名称
                     "newsOrder INTEGER ," + // 新闻发布时间
                     "top INTEGER ," + // 新闻来源
                     "iconPath TEXT ," + // 新闻简介
                     "summary TEXT ," + // 新闻内容
                    "catId INTEGER" + // 新闻来源
                     ");");
             LogInfo.LogOut(TAG + " Create Table onlinenews ok");
             return true;
         } catch (Exception e) {
             LogInfo.LogOut(TAG + " Create Table onlinenews err,table exists." + e.getMessage());
         }
         return false;
     }
     /**
      * 猪场列表数据 public String title;// 标题 public String summary;// 简介 public String
      * order; public int id; public int top; public String iconPath;//新闻图片
      * 
      * @return
      */
     private static boolean createPigFactory() {
     	try {
     		db.execSQL("CREATE TABLE pigfactory (" + "table_id INTEGER PRIMARY KEY autoincrement, " + // 表id
     				"id INTEGER ," + // 猪场ID
     				"title TEXT ," + // 猪场名称
     				"summary TEXT ," + // 内容简介
     				"recommendNum FLOAT" + // 推荐指数
     				"provinceId INTEGER" + //省份ID
     				"type INTEGER ," + // 品系
    				"scale INTEGER ," + //规模
     				");");
     		LogInfo.LogOut(TAG + " Create Table pigfactory ok");
     		return true;
     	} catch (Exception e) {
     		LogInfo.LogOut(TAG + " Create Table pigfactory err,table exists." + e.getMessage());
     	}
     	return false;
     }
 
     private static boolean createNewsDetails() {
         try {
             db.execSQL("CREATE TABLE newsdetail (" + "table_id INTEGER PRIMARY KEY autoincrement, " + // 表id
                     "news_id INTEGER ," + // 新闻id
                     "title TEXT ," + // 新闻名称
                     "summary TEXT ," + // 新闻内容
                     "newsOrder TEXT ," + // 新闻发布时间
                     "iconUri TEXT ," + // 新闻来源
                     "content TEXT ," + // 新闻简介
                     "cTime TEXT ," + // 新闻来源
                     "imgUri TEXT ," + // 新闻简介
                     "newsfrom TEXT ," + // 新闻简介
                     "subTitle TEXT" + // 新闻来源
                     ");");
             LogInfo.LogOut(TAG + " Create Table newsdetail ok");
             return true;
         } catch (Exception e) {
             LogInfo.LogOut(TAG + " Create Table newsdetail err,table exists." + e.getMessage());
         }
         return false;
     }
 
     public static void close() {
         try {
             SQLiteDatabase.releaseMemory();
             if (db != null) {
                 db.close();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
