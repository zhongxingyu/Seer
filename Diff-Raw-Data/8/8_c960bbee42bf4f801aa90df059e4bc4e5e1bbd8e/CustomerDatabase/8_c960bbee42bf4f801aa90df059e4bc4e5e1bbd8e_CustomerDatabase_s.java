 package com.goliathonline.android.greenstreetcrm.provider;
 
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
 import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.CustomersColumns;
 import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.SyncColumns;
 
import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 /**
  * Helper for managing {@link SQLiteDatabase} that stores data for
  * {@link CustomerProvider}.
  */
 public class CustomerDatabase extends SQLiteOpenHelper {
     private static final String TAG = "CustomerDatabase";
 
     private static final String DATABASE_NAME = "customers.db";
 
     // NOTE: carefully update onUpgrade() when bumping database versions to make
     // sure user data is saved.
 
    private static final int VER_LAUNCH = 1;
 
     private static final int DATABASE_VERSION = VER_LAUNCH;
 
     interface Tables {
         String CUSTOMERS = "customers";
     }
 
     public CustomerDatabase(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
 
         db.execSQL("CREATE TABLE " + Tables.CUSTOMERS + " ("
                 + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                 + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                 + CustomersColumns.CUSTOMER_ID + " TEXT NOT NULL,"
                 + CustomersColumns.CUSTOMER_LASTNAME + " TEXT,"
                 + CustomersColumns.CUSTOMER_FIRSTNAME + " TEXT,"
                 + CustomersColumns.CUSTOMER_COMPANY + " TEXT,"
                 + CustomersColumns.CUSTOMER_ADDRESS + " TEXT,"
                 + CustomersColumns.CUSTOMER_CITY + " TEXT,"
                 + CustomersColumns.CUSTOMER_STATE + " TEXT,"
                 + CustomersColumns.CUSTOMER_ZIPCODE + " TEXT,"
                 + CustomersColumns.CUSTOMER_PHONE + " TEXT,"
                 + CustomersColumns.CUSTOMER_MOBILE + " TEXT,"
                 + CustomersColumns.CUSTOMER_EMAIL+ " TEXT,"
                 + CustomersColumns.CUSTOMER_STARRED + " INTEGER NOT NULL DEFAULT 0,"
                 + "UNIQUE (" + CustomersColumns.CUSTOMER_ID + ") ON CONFLICT REPLACE)");
         
         db.execSQL("INSERT INTO " + Tables.CUSTOMERS + "("
     			+ SyncColumns.UPDATED + ","
     			+ CustomersColumns.CUSTOMER_ID + ","
     			+ CustomersColumns.CUSTOMER_FIRSTNAME + ","
     			+ CustomersColumns.CUSTOMER_LASTNAME + ","
     			+ CustomersColumns.CUSTOMER_CITY + ","
     			+ CustomersColumns.CUSTOMER_EMAIL + ") VALUES ("
     			+ "0,'jszechy','Jared','Szechy','Fairborn','jared.szechy@gmail.com')");
 
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
 
         // NOTE: This switch statement is designed to handle cascading database
         // updates, starting at the current version and falling through to all
         // future upgrade cases. Only use "break;" when you want to drop and
         // recreate the entire database.
         int version = oldVersion;
 
         switch (version) {
             case VER_LAUNCH:
                 // Do nothing as of now.
             	
             	version = VER_LAUNCH;
 
         }
 
         Log.d(TAG, "after upgrade logic, at version " + version);
         if (version != DATABASE_VERSION) {
             Log.w(TAG, "Destroying old data during upgrade");
 
             db.execSQL("DROP TABLE IF EXISTS " + Tables.CUSTOMERS);
 
             onCreate(db);
         }
     }
 }
