 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 
 public class ExtrasDbOpenHelper extends SQLiteOpenHelper {
 	
 	
 	
 	public static final String TABLE_COMMENTS = "comments";
 	public static final String COLUMN_COMMENTS_APKID = "apkid";
 	public static final String COLUMN_COMMENTS_COMMENT = "comment";
 	
 	private static final String CREATE_TABLE_COMMENTS = "create table "
 			+ TABLE_COMMENTS + "(" 
 			+ COLUMN_COMMENTS_APKID + " text , "
 			+ COLUMN_COMMENTS_COMMENT + " text , UNIQUE ("+COLUMN_COMMENTS_APKID+") ON CONFLICT REPLACE); ";
 
 	public ExtrasDbOpenHelper(Context context) {
		super(context, "extras.db", null, 23);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE_COMMENTS);
 		db.execSQL("create index idx on comments(apkid)");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS comments");
		onCreate(db);
 	}
 
 }
