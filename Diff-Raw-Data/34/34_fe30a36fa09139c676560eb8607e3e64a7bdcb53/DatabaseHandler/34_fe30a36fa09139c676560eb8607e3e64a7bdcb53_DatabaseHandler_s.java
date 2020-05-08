 package com.example.getconnected.sqllite;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import java.util.HashMap;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
  
 public class DatabaseHandler extends SQLiteOpenHelper {
  
     // All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "busstopManager";
  
     // Contacts table name
     private static final String TABLE_BUSSTOPS = "busstops";
  
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
  
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
         String CREATE_BUSSTOP_TABLE = "CREATE TABLE `busstops` (" + 
 		  "`id` varchar(20) NOT NULL," +
 		  "`halteNummer_overig` varchar(20) DEFAULT NULL," +
 		  "`GPS_Longtitude` varchar(15) DEFAULT NULL," +
 		  "`GPS_Latitude` varchar(15) DEFAULT NULL," +
 		  "`naam` varchar(200) DEFAULT NULL," +
 		  "`opt_dieptehaltekom` varchar(8) DEFAULT NULL," +
 		  "`opt_halteerlengte1` varchar(8) DEFAULT NULL," +
 		  "`opt_halteerlengte2` varchar(8) DEFAULT NULL," +
 		  "`opt_halteerlengte3` varchar(8) DEFAULT NULL," +
 		  "`opt_inrijhoek` varchar(10) DEFAULT NULL," +
 		  "`opt_uitrijhoek` varchar(10) DEFAULT NULL," +
 		  "`opt_perronband` varchar(10) DEFAULT NULL," +
 		  "`opt_perronhoogte` varchar(8) DEFAULT NULL," +
 		  "`opt_perronbreedte` varchar(8) DEFAULT NULL," +
 		  "`opt_perronlengte` varchar(8) DEFAULT NULL," +
 		  "`opt_barrierevrije_doorgang` varchar(8) DEFAULT NULL," +
 		  "`opt_hellingshoek` varchar(20) DEFAULT NULL," +
 		  "`opt_breedteaanlooproute` varchar(8) DEFAULT NULL," +
 		  "`opt_hoogteverschilperron` varchar(8) DEFAULT NULL," +
 		  "`opt_markeringperronrand` varchar(1) DEFAULT NULL," +
 		  "`opt_geleidelijn` varchar(1) DEFAULT NULL," +
 		  "`opt_aanwezigheidabri` varchar(1) DEFAULT NULL," +
 		  "`opt_afstandperronabri` varchar(8) DEFAULT NULL," +
 		  "`opt_halteaanduiding` varchar(25) DEFAULT NULL," +
 		  "`opt_reisinformatie` varchar(10) DEFAULT NULL," +
 		  "`opt_infoomgeving` varchar(1) DEFAULT NULL," +
 		  "`opt_zitgelegenheid` varchar(1) DEFAULT NULL," +
 		  "`opt_verlichting` varchar(10) DEFAULT NULL," +
 		  "`opt_afvalbak` varchar(1) DEFAULT NULL," +
 		  "`opt_fietsparkeervoorziening` varchar(1) DEFAULT NULL," +
 		  "`category` varchar(15) DEFAULT NULL," +
 		  "`city` varchar(15) DEFAULT NULL," +
 		  "PRIMARY KEY (`id`)" +
 		  ")";
         db.execSQL(CREATE_BUSSTOP_TABLE);
     }
  
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUSSTOPS);
  
         // Create tables again
         onCreate(db);
     }
     
     public void addBusstops() {
     	String fileName = Environment.getExternalStorageDirectory() + "/bushaltes_csv.csv";
     	FileReader file = null;
 		try {
 			file = new FileReader(fileName);
 		} catch (FileNotFoundException e1) {
 			System.out.println("Cannot find file: " + fileName);
 			e1.printStackTrace();
 		}
     	BufferedReader buffer = new BufferedReader(file);
     	String line = "";
     	String tableName = TABLE_BUSSTOPS;
     	String str1 = "INSERT INTO " + tableName + " values(";
     	String str2 = ");";
     	SQLiteDatabase db = this.getWritableDatabase();
     	db.beginTransaction();
     	try {
 			while ((line = buffer.readLine()) != null) {
 			    StringBuilder sb = new StringBuilder(str1);
 			    String[] str = line.split(",");
 			    for ( int i = 0; i < str.length; i ++ ) {
			    	sb.append(str[i] + ",");
 			    }
 			    sb.append(str2);
 			    db.execSQL(sb.toString());
 			    System.out.println(line);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     	db.setTransactionSuccessful();
     	db.endTransaction();
     }
  
     /**
      * All CRUD(Create, Read, Update, Delete) Operations
      */
  
     // Getting single contact
    HashMap<String, String> getBusStop(int id) {
         SQLiteDatabase db = this.getReadableDatabase();
  
         Cursor cursor = db.query(TABLE_BUSSTOPS, null, "id=?",
                 new String[] { String.valueOf(id) }, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
  
         HashMap<String, String> busStops = new HashMap<String, String>();
         for ( int i = 0; i < cursor.getColumnCount(); i ++ ) {
         	busStops.put(cursor.getColumnName(i), cursor.getString(i));
         }
         // return contact
         return busStops;
     }
      
     // Getting All Contacts
 //    public List<ArrayList<String>> getAllContacts() {
 //        List<Contact> contactList = new ArrayList<Contact>();
 //        // Select All Query
 //        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 // 
 //        SQLiteDatabase db = this.getWritableDatabase();
 //        Cursor cursor = db.rawQuery(selectQuery, null);
 // 
 //        // looping through all rows and adding to list
 //        if (cursor.moveToFirst()) {
 //            do {
 //                Contact contact = new Contact();
 //                contact.setID(Integer.parseInt(cursor.getString(0)));
 //                contact.setName(cursor.getString(1));
 //                contact.setPhoneNumber(cursor.getString(2));
 //                // Adding contact to list
 //                contactList.add(contact);
 //            } while (cursor.moveToNext());
 //        }
 // 
 //        // return contact list
 //        return contactList;
 //    }
 
  
 }
