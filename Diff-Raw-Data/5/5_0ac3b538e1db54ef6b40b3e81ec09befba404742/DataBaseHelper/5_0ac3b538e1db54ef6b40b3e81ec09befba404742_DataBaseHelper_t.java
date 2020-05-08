 package com.nextgen.database;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.nextgen.bemore.R;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.view.View;
 import android.widget.TextView;
 
 public class DataBaseHelper extends SQLiteOpenHelper{
 
     //The Android's default system path of your application database.
     private static String DB_PATH = "/data/data/com.nextgen.bemore/databases/";
 
     private static String DB_NAME = "EventData";
     private static String EVENTS_TABLE = "events";
     private static String BUY_TABLE = "buy";
     private static String BUY_REC_TABLE = "buy_recommendations";
     //keys for events table
     public static final String KEY_ROWID = "_id";
     public static final String KEY_EVENT_NAME = "name";
     public static final String KEY_GENRE = "genre";
     public static final String KEY_DATE = "date";
     public static final String KEY_TIME = "time";
     public static final String KEY_SHORT_DESC = "short_desc";   
     public static final String KEY_BUY_REC_ID = "buy_rec_id";   
     //Keys for buy recommendations table
     public static final String KEY_BUY_REC_1 = "buy_rec_1";   
     //keys for buy table
     public static final String KEY_BUY_TITLE = "title";   
     public static final String KEY_BUY_DESC = "desc";   
     public static final String KEY_BUY_PRICE = "price";   
 
     private SQLiteDatabase myDataBase; 
 
     private final Context myContext;
 
     /**
      * Constructor
      * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
      * @param context
      */
     public DataBaseHelper(Context context) {
 
         super(context, DB_NAME, null, 1);
         this.myContext = context;
     }   
 
   /**
      * Creates a empty database on the system and rewrites it with your own database.
      * */
     public void createDataBase() throws IOException{
 
         boolean dbExist = checkDataBase();
 
 //        if(dbExist){
 //        //do nothing - database already exist
 //        }else
         {
 
             //By calling this method and empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
             this.getReadableDatabase();
 
             try {
 
                 copyDataBase();
 
             } catch (IOException e) {
 
                 throw new Error("Error copying database");
 
             }
         }
 
     }
 
     /**
      * Check if the database already exist to avoid re-copying the file each time you open the application.
      * @return true if it exists, false if it doesn't
      */
     private boolean checkDataBase(){
 
         SQLiteDatabase checkDB = null;
 
         try{
             String myPath = DB_PATH + DB_NAME;
             checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
         }catch(SQLiteException e){
 
             //database does't exist yet.
 
         }
 
         if(checkDB != null){
 
             checkDB.close();
 
         }
 
         return checkDB != null ? true : false;
     }
 
     /**
      * Copies your database from your local assets-folder to the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transfering bytestream.
      * */
     private void copyDataBase() throws IOException{
 
         //Open your local db as the input stream
         InputStream myInput = myContext.getAssets().open(DB_NAME);
 
         // Path to the just created empty db
         String outFileName = DB_PATH + DB_NAME;
 
         //Open the empty db as the output stream
         OutputStream myOutput = new FileOutputStream(outFileName);
 
         //transfer bytes from the inputfile to the outputfile
         byte[] buffer = new byte[1024];
         int length;
         while ((length = myInput.read(buffer))>0){
             myOutput.write(buffer, 0, length);
         }
 
         //Close the streams
         myOutput.flush();
         myOutput.close();
         myInput.close();
 
     }
 
     public void openDataBase() throws SQLException{
 
         //Open the database
         String myPath = DB_PATH + DB_NAME;
         myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     }
 
     @Override
     public synchronized void close() {
 
             if(myDataBase != null)
                 myDataBase.close();
 
             super.close();
 
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
     }
 
         // Add your public helper methods to access and get content from the database.
        // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
        // to you to create adapters for your views.
     public Cursor fetchAllEvents() {
 
         return myDataBase.query(EVENTS_TABLE, new String[] {KEY_ROWID, KEY_EVENT_NAME,
                 KEY_GENRE, KEY_DATE, KEY_TIME,KEY_SHORT_DESC}, null, null, null, null, null);
     }
     
     /**
      * Return a Cursor positioned at the event that matches the given rowId
      * 
      * @param rowId id of event to retrieve
      * @return Cursor positioned to matching note, if found
      * @throws SQLException if event could not be found/retrieved
      */
     public Cursor fetchEvent(long rowId) throws SQLException {
 
         Cursor mCursor =
             myDataBase.query(true, EVENTS_TABLE, new String[] {KEY_ROWID,
                     KEY_DATE, KEY_EVENT_NAME, KEY_SHORT_DESC}, KEY_ROWID + "='"+Long.toString(rowId)+"'", null,
                     null, null, null, null);
         
         mCursor.moveToFirst();
         return mCursor;
 
     }
 
     /**
      * Return a Cursor positioned at the event that matches the given rowId
      * 
      * @param rowId id of event to retrieve
      * @return Cursor positioned to matching note, if found
      * @throws SQLException if event could not be found/retrieved
      */
     public Cursor fetchBuyRecommendation(long rowId) throws SQLException {
         Integer buyRecRowId=0;
         Integer buyRowId=0;
         
         /*get cursor to the event in the event table*/
         Cursor mCursor =
             myDataBase.query(true, EVENTS_TABLE, new String[] {KEY_ROWID,
                     KEY_BUY_REC_ID}, KEY_ROWID + "='"+Long.toString(rowId)+"'", null,
                     null, null, null, null);
         
         mCursor.moveToFirst();
         
 
             //make sure the cursor is not empty, then get the buy rec id. Then get cursor to the buy recommendations for the event.
             if (mCursor.getCount() > 0) {
                buyRecRowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(DataBaseHelper.KEY_BUY_REC_ID));
                 
                 mCursor =
                     myDataBase.query(true, BUY_REC_TABLE, new String[] {KEY_ROWID,
                             KEY_BUY_REC_1}, KEY_ROWID + "='"+Integer.toString(buyRecRowId)+"'", null,
                             null, null, null, null);              
             }
 
             mCursor.moveToFirst();
             
             //make sure the cursor to the buy recommendations is not empty, then get the 1st buy recommendation
             if (mCursor.getCount() > 0) {
                buyRowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(DataBaseHelper.KEY_BUY_REC_1));
                 
                mCursor =
                     myDataBase.query(true, BUY_TABLE, new String[] {KEY_ROWID,
                             KEY_BUY_TITLE, KEY_BUY_DESC, KEY_BUY_PRICE}, KEY_ROWID + "='"+Integer.toString(buyRowId)+"'", null,
                             null, null, null, null);              
             }            
 
             mCursor.moveToFirst();
      
        //return cursor to the buy table corresponding to the 1st recommendation for this event
         return mCursor;
 
     }
 
 }
 
