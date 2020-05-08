 
 package com.wh.dm.db;
 
 import com.wh.dm.type.ArticleMagzine;
 import com.wh.dm.type.Favorite;
 import com.wh.dm.type.LoadInfo;
 import com.wh.dm.type.Magazine;
 import com.wh.dm.type.NewsContent;
 import com.wh.dm.type.Photo;
 import com.wh.dm.type.PhotoDetails;
 import com.wh.dm.type.PicWithTxtNews;
 import com.wh.dm.type.PictureMagzine;
 import com.wh.dm.type.PostMessage;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import java.util.ArrayList;
 
 public class DatabaseImpl implements Database {
 
     // news
     private static final String DB_NAME = "wh_dm.db";
     private static final String TABLE_HEAD = "head";
     private static final String TABLE_HOUSE = "house";
     private static final String TABLE_CAR = "car";
     private static final String TABLE_FASHION = "fashion";
     private static final String TABLE_LIFE = "life";
     private static final String TABLE_TRAVEL = "travel";
     private static final String TABLE_NEWSDETAIL = "newsdetail";
     // photos
     private static final String TABLE_PHOTO = "photo";
     private static final String TABLE_HOT_PHOTO = "hotphoto";
     private static final String TABLE_CAR_PHOTO = "carphoto";
     private static final String TABLE_GIRL_PHOTO = "girlphoto";
     private static final String TABLE_PHOTOGRAPH_PHOTO = "photographphoto";
     private static final String TABLE_FUN_PHOTO = "funphoto";
     // photos details
     private static final String TABLE_PHOTO_DET = "photodet";
     private static final String TABLE_HOT_PHOTO_DET = "hotphotodet";
     private static final String TABLE_CAR_PHOTO_DET = "carphotodet";
     private static final String TABLE_GIRL_PHOTO_DET = "girlphotodet";
     private static final String TABLE_PHOTOGRAPH_PHOTO_DET = "photographphotodet";
     private static final String TABLE_FUN_PHOTO_DET = "funphotodet";
     // magazine
     private static final String TABLE_MAGAZINE_HOT = "magazinehot";
     private static final String TABLE_MAGAZINE_CAR = "magazinecar";
     private static final String TABLE_MAGAZINE_GIRL = "magazinegirl";
     private static final String TABLE_MAGAZINE_PHOTOGRAPH = "magazinephotograph";
     private static final String TABLE_MAGAZINE_FUN = "magazinefun";
 
     // favorite
     private static final String TABLE_FAVORITE = "favorite";
 
     // subcribe
     private static final String TABLE_SUBCRIBE = "subcribe";
     // load magazine body
     private static final String TABLE_MAGEZINE_BODY = "magazinebody";
 
     private static final String TABLE_MAGAZINE_PIC = "magazinepic";
     // load info
     private static final String TABLE_LOAD_INFO = "loadinfo";
     // postmessage
     private static final String TABLE_MESSAGE = "postmessage";
     private final Context context;
 
     public DatabaseImpl(Context _context) {
 
         this.context = _context;
         create();
     }
 
     public void create() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         // local news
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_HEAD
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_HOUSE
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_CAR
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_FASHION
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_LIFE
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_TRAVEL
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, title VARCHAR, litpic VARCHAR,"
                 + " description VARCHAR, sortrank INTEGER, isfirst INTEGER, ishot INTEGER, date VARCHAR, isUrl VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_NEWSDETAIL
                 + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, typeid INTEGER, sortrank INTEGER, title VARCHAR, source VARCHAR,"
                 + " litpic VARCHAR, pubdate VARCHAR, isfirst INTEGER, ishot INTEGER, isUrl VARCHAR, body VARCHAR)");
         // photos
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_HOT_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_CAR_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_GIRL_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_PHOTOGRAPH_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_FUN_PHOTO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, aid INTERGER, aname VARCHAR, title VARCHAR, litpic VARCHAR, atype INTEGER, author VARCHAR, "
                 + "click INTEGER, description VARCHAR, isgood INTERGER, istop INTEGER, isflash INTEGER, ordernum INTERGER, addtime VARCHAR, pcount INTEGER, fcount INTEGER, cname VARCHAR)");
         // photo details
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_HOT_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_CAR_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_GIRL_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_PHOTOGRAPH_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_FUN_PHOTO_DET
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, id INTEGER, aid INTEGER,cover INTEGER, click INTEGER, description VARCHAR, ordernum VARCHAR,"
                 + "pic VARCHAR, addtime VARCHAR, edittime VARCHAR, ext1 INTEGER, ext2 VARCHAR, ext3 VARCHAR)");
         // magazine
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_HOT
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER UNIQUE, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
         // subcribe
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_SUBCRIBE
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER UNIQUE, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_CAR
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_GIRL
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
 
         // favorite
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_FAVORITE
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, fid INTEGER, nid INTEGER, type INTEGER, title VARCHAR, addtime VARCHAR)");
 
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_PHOTOGRAPH
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_FUN
                 + "( uid INTEGER PRIMARY KEY AUTOINCREMENT, no INTEGER, sid INTEGER, cid VARCHAR, editor VARCHAR, template INTEGER, memo VARCHAR, isfeedback INTEGER,"
                 + "_limit INTEGER, addtime VARCHAR, sname VARCHAR, shortname VARCHAR, pic VARCHAR, spic VARCHAR, titlepic VARCHAR)");
 
         // load arcticle magazine
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGEZINE_BODY
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, sid INTEGER , author VARCHAR, writer VARCHAR,"
                 + "no INTEGER, id INTEGER UNIQUE, title VARCHAR, source VARCHAR, litpic VARCHAR, pubdate VARCHAR, body VARCHAR, ispic INTEGER)");
         // load info
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_LOAD_INFO
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, sid INTEGER UNIQUE, pic_path VARCHAR, title VARCHAR, is_finish INTEGER, is_start VARCHAR, is_pause VARCHAR, pro VARCHAR)");
         // load imaga magazine
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MAGAZINE_PIC
                 + "(uid INTEGER PRIMARY KEY AUTOINCREMENT, sid INTEGER , no INTEGER, id INTEGER UNIQUE, aid INTEGER, cover INTEGER, description VARCHAR, pic VARCHAR, addtime VARCHAR)");
         // post message
         db.execSQL("CREATE TABLE IF NOT EXISTS "
                 + TABLE_MESSAGE
                 + "(id_key INTEGER PRIMARY KEY AUTOINCREMENT, id INTEGER, title VARCHAR, mid INTEGER, mname VARCHAR, pid INTEGER, temp INTEGER, uid INTEGER, is_read INTEGER)");
         db.close();
     }
 
     @Override
     public void deleteAllData() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         // local news
         db.delete(TABLE_HEAD, null, null);
         db.delete(TABLE_HOUSE, null, null);
         db.delete(TABLE_CAR, null, null);
         db.delete(TABLE_FASHION, null, null);
         db.delete(TABLE_LIFE, null, null);
         db.delete(TABLE_TRAVEL, null, null);
         // photos
         db.delete(TABLE_PHOTO, null, null);
         db.delete(TABLE_HOT_PHOTO, null, null);
         db.delete(TABLE_CAR_PHOTO, null, null);
         db.delete(TABLE_GIRL_PHOTO, null, null);
         db.delete(TABLE_PHOTOGRAPH_PHOTO, null, null);
         db.delete(TABLE_FUN_PHOTO, null, null);
         // photo details
         db.delete(TABLE_PHOTO_DET, null, null);
         db.delete(TABLE_HOT_PHOTO_DET, null, null);
         db.delete(TABLE_CAR_PHOTO_DET, null, null);
         db.delete(TABLE_GIRL_PHOTO_DET, null, null);
         db.delete(TABLE_PHOTOGRAPH_PHOTO_DET, null, null);
         db.delete(TABLE_FUN_PHOTO_DET, null, null);
 
         // favorite
         db.delete(TABLE_FAVORITE, null, null);
 
         // magazine
         db.delete(TABLE_MAGAZINE_HOT, null, null);
         db.delete(TABLE_MAGEZINE_BODY, null, null);
         db.delete(TABLE_LOAD_INFO, null, null);
        // subcribe
        db.delete(TABLE_SUBCRIBE, null, null);
        // message
        db.delete(TABLE_MESSAGE, null, null);
         // down load
         db.delete(TABLE_MAGEZINE_BODY, null, null);
         db.delete(TABLE_MAGAZINE_PIC, null, null);
         db.delete(TABLE_LOAD_INFO, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteAllNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_HEAD, null, null);
         db.delete(TABLE_HOUSE, null, null);
         db.delete(TABLE_CAR, null, null);
         db.delete(TABLE_FASHION, null, null);
         db.delete(TABLE_LIFE, null, null);
         db.delete(TABLE_TRAVEL, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteHeadNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_HEAD, null, null);
         db.close();
     }
 
     @Override
     public void deleteHouseNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_HOUSE, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteCarNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_CAR, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteFashionNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_FASHION, null, null);
         db.close();
     }
 
     @Override
     public void deleteLifeNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_LIFE, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteTravelNews() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_TRAVEL, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteNewsContent() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_NEWSDETAIL, null, null);
         db.close();
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getHeadNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         try {
             Cursor query = db.query(TABLE_HEAD, null, null, null, null, null, null);
             if (query != null) {
                 query.moveToFirst();
                 while (!query.isAfterLast()) {
                     news.add(builder.build(query));
                     query.moveToNext();
                 }
                 query.close();
             }
         } catch (Exception e) {
             e.printStackTrace();
             Log.d("db", e.toString());
         } finally {
             db.close();
             return news;
         }
 
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getHouseNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_HOUSE, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 news.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return news;
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getCarNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_CAR, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 news.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return news;
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getFashionNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_FASHION, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 news.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return news;
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getLifeNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_LIFE, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 news.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return news;
     }
 
     @Override
     public ArrayList<PicWithTxtNews> getTravelNews() {
 
         ArrayList<PicWithTxtNews> news = new ArrayList<PicWithTxtNews>();
         PicWithTxtNewsBuilder builder = new PicWithTxtNewsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_TRAVEL, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 news.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return news;
     }
 
     @Override
     public void addHeadNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_HEAD, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addHouseNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_HOUSE, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         }
         db.close();
     }
 
     @Override
     public void addCarNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_CAR, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addFashionNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_FASHION, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addLifeNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_LIFE, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addTravelNews(ArrayList<PicWithTxtNews> news) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < news.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PicWithTxtNewsBuilder()).deconstruct(news.get(i)));
                 db.insert(TABLE_TRAVEL, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addNewsContent(NewsContent content) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         ContentValues values = new ContentValues();
         values.putAll((new NewsContentBuilder()).deconstruct(content));
         try {
             db.insert(TABLE_NEWSDETAIL, null, values);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             db.close();
         }
     }
 
     @Override
     public NewsContent getNewsContent(int id) {
 
         NewsContent newsContent = null;
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         String[] selectionArgs = {
             "" + id
         };
         Cursor query = db.query(TABLE_NEWSDETAIL, null, "id = ?", selectionArgs, null, null, null);
         if (query != null && query.getCount() > 0) {
             query.moveToFirst();
             newsContent = (new NewsContentBuilder()).build(query);
             query.close();
             db.close();
             return newsContent;
         } else {
             db.close();
             return null;
         }
 
     }
 
     // photo
     @Override
     public void addAllPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addHotPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_HOT_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addCarPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_CAR_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addGirlPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_GIRL_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addPhotographPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_PHOTOGRAPH_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addFunPhoto(ArrayList<Photo> photo) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photo.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoBuilder()).deconstruct(photo.get(i)));
                 db.insert(TABLE_FUN_PHOTO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void deleteAllPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_PHOTO, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteHotPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_HOT_PHOTO, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteCarPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_CAR_PHOTO, null, null);
         db.close();
     }
 
     @Override
     public void deleteGirlPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_GIRL_PHOTO, null, null);
         db.close();
 
     }
 
     @Override
     public void deletePhotographPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_PHOTOGRAPH_PHOTO, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteFunPhoto() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_FUN_PHOTO, null, null);
         db.close();
 
     }
 
     @Override
     public ArrayList<Photo> getAllPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_PHOTO, null, null, null, null, null, null);
         if (query != null && query.getCount() > 0) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public ArrayList<Photo> getHotPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_HOT_PHOTO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public ArrayList<Photo> getCarPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_CAR_PHOTO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public ArrayList<Photo> getGirlPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_GIRL_PHOTO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public ArrayList<Photo> getPhotographPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_PHOTOGRAPH_PHOTO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public ArrayList<Photo> getFunPhoto() {
 
         ArrayList<Photo> photo = new ArrayList<Photo>();
         PhotoBuilder builder = new PhotoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_FUN_PHOTO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photo.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return photo;
     }
 
     @Override
     public void addAllPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addHotPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_HOT_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addCarPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_CAR_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addGirlPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_GIRL_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addPhotographPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_PHOTOGRAPH_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addFunPhotoDet(ArrayList<PhotoDetails> photoDet) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < photoDet.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PhotoDetailsBuilder()).deconstruct(photoDet.get(i)));
                 db.insert(TABLE_FUN_PHOTO_DET, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void deleteAllPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteHotPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_HOT_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteCarPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_CAR_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteGirlPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_GIRL_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public void deletePhotographPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_PHOTOGRAPH_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteFunPhotoDet() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_FUN_PHOTO_DET, null, null);
         db.close();
 
     }
 
     @Override
     public ArrayList<PhotoDetails> getAllPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     @Override
     public ArrayList<PhotoDetails> getHotPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_HOT_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     @Override
     public ArrayList<PhotoDetails> getCarPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_CAR_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     @Override
     public ArrayList<PhotoDetails> getGirlPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_GIRL_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     @Override
     public ArrayList<PhotoDetails> getPhotographPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_PHOTOGRAPH_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     @Override
     public ArrayList<PhotoDetails> getFunPhotoDet() {
 
         ArrayList<PhotoDetails> photoDets = new ArrayList<PhotoDetails>();
         PhotoDetailsBuilder builder = new PhotoDetailsBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_FUN_PHOTO_DET, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 photoDets.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
             db.close();
         }
 
         return photoDets;
     }
 
     // magazine
     @Override
     public void deleteHotMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_HOT, null, null);
         db.close();
     }
 
     @Override
     public void deleteCarMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_CAR, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteGirlMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_GIRL, null, null);
         db.close();
 
     }
 
     @Override
     public void deletePhotographMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_PHOTOGRAPH, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteFunMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_FUN, null, null);
         db.close();
 
     }
 
     @Override
     public void addHotMagazine(ArrayList<Magazine> magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazine.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazine.get(i)));
                 db.insert(TABLE_MAGAZINE_HOT, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addCarMagazine(ArrayList<Magazine> magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazine.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazine.get(i)));
                 db.insert(TABLE_MAGAZINE_CAR, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
     }
 
     @Override
     public void addGirlMagazine(ArrayList<Magazine> magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazine.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazine.get(i)));
                 db.insert(TABLE_MAGAZINE_GIRL, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addPhotographMagazine(ArrayList<Magazine> magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazine.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazine.get(i)));
                 db.insert(TABLE_MAGAZINE_PHOTOGRAPH, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public void addFunMagazine(ArrayList<Magazine> magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazine.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazine.get(i)));
                 db.insert(TABLE_MAGAZINE_FUN, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
 
     }
 
     @Override
     public ArrayList<Magazine> getHotMagazine() {
 
         ArrayList<Magazine> magazine = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_HOT, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazine.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazine;
     }
 
     @Override
     public ArrayList<Magazine> getCarMagazine() {
 
         ArrayList<Magazine> magazine = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_CAR, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazine.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazine;
     }
 
     @Override
     public ArrayList<Magazine> getGirlMagazine() {
 
         ArrayList<Magazine> magazine = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_GIRL, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazine.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazine;
     }
 
     @Override
     public ArrayList<Magazine> getPhotographMagazine() {
 
         ArrayList<Magazine> magazine = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_PHOTOGRAPH, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazine.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazine;
     }
 
     @Override
     public ArrayList<Magazine> getFunMagazine() {
 
         ArrayList<Magazine> magazine = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_FUN, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazine.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazine;
     }
 
     @Override
     public void addFavorite(ArrayList<Favorite> favorite) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < favorite.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new FavoriteBuilder().deconstruct(favorite.get(i)));
                 db.insert(TABLE_FAVORITE, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
     }
 
     @Override
     public ArrayList<Favorite> getFavorite() {
 
         // TODO Auto-generated method stub
         ArrayList<Favorite> favorite = new ArrayList<Favorite>();
         FavoriteBuilder builder = new FavoriteBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_FAVORITE, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 favorite.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return favorite;
     }
 
     @Override
     public void deleteFavorite() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_FAVORITE, null, null);
         db.close();
     }
 
     @Override
     public boolean deleteOneFavorite(int id) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         String sql = "delete from " + TABLE_FAVORITE + " where nid=" + id;
         try {
             db.execSQL(sql);
             db.close();
             return true;
         } catch (Exception ex) {
             db.close();
             return false;
         }
     }
 
     @Override
     public ArrayList<Magazine> getSubcribedMagazine() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         ArrayList<Magazine> magazines = new ArrayList<Magazine>();
         MagazineBuilder builder = new MagazineBuilder();
         Cursor query = db.query(TABLE_SUBCRIBE, null, null, null, null, null, null);
         try {
             if (query != null && query.getCount() > 0) {
                 query.moveToFirst();
                 while (!query.isAfterLast()) {
                     magazines.add(builder.build(query));
                     query.moveToNext();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         query.close();
         db.close();
         return magazines;
     }
 
     @Override
     public void addMagazine(Magazine magazine) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         try {
             ContentValues values = new ContentValues();
             values.putAll(new MagazineBuilder().deconstruct(magazine));
             db.insert(TABLE_SUBCRIBE, null, values);
         } catch (Exception e) {
             e.printStackTrace();
         }
         db.close();
     }
 
     @Override
     public void delMagazine(int sid) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         String whereClause = "sid=?";
         String[] whereArgs = {
             "" + sid
         };
         try {
             db.delete(TABLE_SUBCRIBE, whereClause, whereArgs);
         } catch (Exception e) {
             e.printStackTrace();
         }
         db.close();
     }
 
     @Override
     public void addMagazines(ArrayList<Magazine> magazines) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < magazines.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll(new MagazineBuilder().deconstruct(magazines.get(i)));
                 db.insert(TABLE_SUBCRIBE, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
     }
 
     @Override
     public void delMagazines() {
 
         SQLiteDatabase db = context
                 .openOrCreateDatabase(TABLE_SUBCRIBE, Context.MODE_PRIVATE, null);
         try {
             db.delete(TABLE_SUBCRIBE, null, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
         db.close();
     }
 
     @Override
     public Magazine getMagazine(int sid) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         String[] selectionArgs = {
             "" + sid
         };
         Cursor query = db.query(TABLE_SUBCRIBE, null, "sid = ?", selectionArgs, null, null, null,
                 null);
         if (query != null) {
             query.moveToFirst();
             Magazine magazine = (new MagazineBuilder()).build(query);
             query.close();
             db.close();
             return magazine;
         } else {
             db.close();
             return null;
         }
 
     }
 
     @Override
     public ArrayList<PostMessage> getPostMessage() {
 
         ArrayList<PostMessage> messages = new ArrayList<PostMessage>();
         PostMessageBuilder builder = new PostMessageBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MESSAGE, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 messages.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return messages;
     }
 
     @Override
     public void deletePostMessage() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MESSAGE, null, null);
         db.close();
 
     }
 
     @Override
     public void addPostMessage(ArrayList<PostMessage> postMessages) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < postMessages.size(); i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PostMessageBuilder()).deconstruct(postMessages.get(i)));
                 db.insert(TABLE_MESSAGE, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         db.close();
     }
 
     // magazine body
     @Override
     public ArrayList<ArticleMagzine> getMagazineBody(int sid) {
 
         ArrayList<ArticleMagzine> magazineList = new ArrayList<ArticleMagzine>();
         ArticleMagzineBuilder builder = new ArticleMagzineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGEZINE_BODY, null, "sid=?", new String[] {
             String.valueOf(sid)
         }, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazineList.add(builder.build(query));
                 query.moveToNext();
             }
         }
         query.close();
         db.close();
         return magazineList;
     }
 
     @Override
     public void deleteMagazineBody() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGEZINE_BODY, null, null);
         db.close();
 
     }
 
     @Override
     public void addMagazineBody(ArrayList<ArticleMagzine> magazineList) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         int size = magazineList.size();
 
         for (int i = 0; i < size; i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new ArticleMagzineBuilder()).deconstruct(magazineList.get(i)));
                 db.insert(TABLE_MAGEZINE_BODY, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         db.close();
     }
 
     @Override
     public ArticleMagzine getOneMagazineBody(int id) {
 
         ArticleMagzine oneMagazine = new ArticleMagzine();
         ArticleMagzineBuilder builder = new ArticleMagzineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGEZINE_BODY, null, "id=?", new String[] {
             String.valueOf(id)
         }, null, null, null);
         if (query != null) {
             query.moveToFirst();
             oneMagazine = builder.build(query);
             db.close();
             query.close();
             return oneMagazine;
         }
         db.close();
         return null;
     }
 
     // load info
 
     @Override
     public ArrayList<LoadInfo> getLoadInfo() {
 
         ArrayList<LoadInfo> loadInfos = new ArrayList<LoadInfo>();
         LoadInfoBuilder builder = new LoadInfoBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_LOAD_INFO, null, null, null, null, null, null);
         if (query != null) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 loadInfos.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
         }
 
         db.close();
         return loadInfos;
     }
 
     @Override
     public void deleteLoadInfo() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_LOAD_INFO, null, null);
         db.close();
 
     }
 
     @Override
     public void addLoadInfo(ArrayList<LoadInfo> loadInfos) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         int size = loadInfos.size();
 
         for (int i = 0; i < size; i++) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new LoadInfoBuilder()).deconstruct(loadInfos.get(i)));
                 db.insert(TABLE_LOAD_INFO, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         db.close();
     }
 
     @Override
     public boolean isLoad(int sid) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_LOAD_INFO, new String[] {
             "is_finish"
         }, "sid=?", new String[] {
             String.valueOf(sid)
         }, null, null, null);
         if (query != null && query.getCount() > 0) {
             query.moveToFirst();
             int load = query.getInt(query.getColumnIndex("is_finish"));
             db.close();
             query.close();
             if (load == 1) {
                 return true;
             }
         }
         db.close();
         return false;
     }
 
     @Override
     public void addMagazinePic(ArrayList<PictureMagzine> magazineList) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
 
         for (PictureMagzine pic : magazineList) {
             try {
                 ContentValues values = new ContentValues();
                 values.putAll((new PictureMagzineBuilder()).deconstruct(pic));
                 db.insert(TABLE_MAGAZINE_PIC, null, values);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         }
 
         db.close();
     }
 
     @Override
     public ArrayList<PictureMagzine> getMagazinePic(int sid) {
 
         ArrayList<PictureMagzine> magazinePics = new ArrayList<PictureMagzine>();
         PictureMagzineBuilder builder = new PictureMagzineBuilder();
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         Cursor query = db.query(TABLE_MAGAZINE_PIC, null, "sid=?", new String[] {
             String.valueOf(sid)
         }, null, null, null);
         if (query != null && query.getCount() > 0) {
             query.moveToFirst();
             while (!query.isAfterLast()) {
                 magazinePics.add(builder.build(query));
                 query.moveToNext();
             }
             query.close();
         }
         db.close();
         return magazinePics;
     }
 
     @Override
     public void deleteMagazinePic() {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.delete(TABLE_MAGAZINE_PIC, null, null);
         db.close();
 
     }
 
     @Override
     public void deleteLoad(int sid, int template) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         if (template == 0) {
             db.delete(TABLE_MAGEZINE_BODY, "sid=?", new String[] {
                 String.valueOf(sid)
             });
         } else if (template == 1) {
             db.delete(TABLE_MAGAZINE_PIC, "sid=?", new String[] {
                 String.valueOf(sid)
             });
         }
         db.close();
     }
 
     @Override
     public void addAllLoad(ArrayList<PictureMagzine> picList,
             ArrayList<ArticleMagzine> articleList, ArrayList<LoadInfo> loadInfoList) {
 
         SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         db.beginTransaction();
         try {
             for (PictureMagzine pic : picList) {
 
                 ContentValues values = new ContentValues();
                 values.putAll((new PictureMagzineBuilder()).deconstruct(pic));
                 db.insert(TABLE_MAGAZINE_PIC, null, values);
             }
             for (ArticleMagzine article : articleList) {
 
                 ContentValues values = new ContentValues();
                 values.putAll((new ArticleMagzineBuilder()).deconstruct(article));
                 db.insert(TABLE_MAGEZINE_BODY, null, values);
             }
             db.delete(TABLE_LOAD_INFO, null, null);
             for (LoadInfo info : loadInfoList) {
 
                 ContentValues values = new ContentValues();
                 values.putAll((new LoadInfoBuilder()).deconstruct(info));
                 db.insert(TABLE_LOAD_INFO, null, values);
             }
             db.setTransactionSuccessful();
         } catch (Exception e) {
             e.printStackTrace();
         }
         db.endTransaction();
         db.close();
     }
 
 }
