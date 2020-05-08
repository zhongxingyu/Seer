 /*
  * Copyright (c) 2012 Joe Rowley
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.mobileobservinglog.support.database;
 
 import com.mobileobservinglog.support.SettingsContainer;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 public class SettingsDAO extends DatabaseHelper {
 
 	public SettingsDAO(Context context) {
 		super(context);
 	}
 
 	/**
 	 * This method is used to return all persistent settings in a single cursor. It's primary use is for creating
 	 * and populating the listItem in the settings screen. If you want to get a single setting, use the
 	 * getPersistentSettings(String setting) override.
 	 * 
 	 * @return A cursor containing the full contents of the settings table. This is not limited at this time because we don't expect the settings table to be very large
 	 */
 	public Cursor getPersistentSettings()
 	{
 		Cursor retVal = null;
 		SQLiteDatabase db = getReadableDatabase();
 		String sqlStatement = "SELECT * FROM settings WHERE settingName IS NOT NULL";
 		
 		retVal = db.rawQuery(sqlStatement, null);
 		retVal.moveToFirst();
 		
 		db.close();
 				
 		return retVal;
 	}
 	
 	/**
 	 * This method is used to return the value of a single persistent setting. It's primary purpose is to be used on the initial screen to 
 	 * set the night-mode backlight intensity, for example.
 	 * 
 	 * @param setting A string representing the setting to look for in the table
 	 * @return A string representing the value of the desired setting 
 	 */
 	public String getPersistentSettings(String setting)
 	{
 		String retVal = null;
 		
 		SQLiteDatabase db = getReadableDatabase();
 		String sqlStatement = "SELECT settingValue FROM settings where settingName = ?";
 		
 		Cursor dbCursor = db.rawQuery(sqlStatement, new String[] {setting});
 		dbCursor.moveToFirst();
 		retVal = dbCursor.getString(0);
 		
 		dbCursor.close();
 		db.close();
 		
 		return retVal;
 	}
 	
 	/**
 	 * This method is used to populate the settings database with a new value for a given setting, and to simultaneously
 	 * set that value in the settings container so it can become immediately available to other parts of the application
 	 * 
 	 * @param setting String representing the setting you wish to set. Preferably use the String Constants provided in the SettingsContainer
 	 * @param value String representing the value to insert.
 	 * @return Was the operation successful?
 	 */
 	public boolean setPersistentSetting(String setting, String value)
 	{
 		SQLiteDatabase db = getWritableDatabase();
 		SettingsContainer settingsRef = SettingsContainer.getSettingsContainer();
 		
 		SQLiteStatement sqlStatement = db.compileStatement("UPDATE settings SET settingValue = ? WHERE settingName = ?");
 		sqlStatement.bindString(1, value);
 		sqlStatement.bindString(2, setting);
 		
 		boolean success = false;
 		db.beginTransaction();
 		try
 		{
 			Log.d("JoeTest", "SqlStatement is " + sqlStatement.toString());
 			sqlStatement.execute();
 			db.setTransactionSuccessful();
 			settingsRef.setPersistentSetting(setting, value);
 			success = true;
 		}
 		catch (SQLException ex)
 		{
 			ex.printStackTrace();
 		}
 		finally
 		{
 			db.endTransaction();
			sqlStatement.close();
 			db.close();
 		}
 		
 		return success;
 	}
 
 }
