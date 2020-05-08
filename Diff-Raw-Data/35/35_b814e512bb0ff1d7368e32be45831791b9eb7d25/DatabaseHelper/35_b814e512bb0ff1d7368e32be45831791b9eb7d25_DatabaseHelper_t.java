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
 
 import java.util.ArrayList;
 
 import com.mobileobservinglog.R;
 import com.mobileobservinglog.objectSearch.ObjectFilter;
 import com.mobileobservinglog.objectSearch.ObjectIndexFilter;
 import com.mobileobservinglog.objectSearch.StringSearchFilter;
 import com.mobileobservinglog.support.SettingsContainer;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteStatement;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Helper class to handle all database interaction for the Mobile Observing Log app
  * 
  * @author Joe
  *
  */
 public class DatabaseHelper extends SQLiteOpenHelper
 {
 	private static final String DATABASE_NAME = "mobileObservingLogDB";
 	private static final int VERSION = 1;
 	Context mContext;
 	private final int objectTableColumnCount = 26;
 	private final int settingsTableColumnCount = 3;
 	private final int availableCatalogsTableColumnCount = 5;
 	  
 	public DatabaseHelper(Context context) 
 	{
 		super(context, DATABASE_NAME, null, VERSION);
 		mContext = context;
 		Log.d("JoeDebug", "DatabaseHelper constructor finish");
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) 
 	{
 		Log.d("JoeDebug", "DatabaseHelper onCreate called from" + mContext.toString());
 		
 		//create tables
 		db.beginTransaction();
 		try {
 			createTables(db);
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
             Log.e("Error creating tables and debug data", e.toString());
         } finally {
         	db.endTransaction();
         }
 		
 		//populate tables
 		db.beginTransaction();
 		try
 		{
 			populatePersistentSettings(db);
 			populateAvailableCatalogs(db);
 			populatePersonalInfo(db);
 			db.setTransactionSuccessful();
			populateObjects(db);
 		}
 		catch (SQLException e)
 		{
 			Log.e("Error populating tables", e.toString());
 		}
 		catch (Exception e)
 		{
 			Log.e("Error populating tables", e.toString());
 		}
 		finally
 		{
 			db.endTransaction();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
 	{
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Method used to initially create the tables for the database
 	 */
 	private void createTables(SQLiteDatabase db)
 	{
 		db.execSQL(mContext.getString(R.string.create_settings_table));
 		db.execSQL(mContext.getString(R.string.create_personal_info_table));
 		db.execSQL(mContext.getString(R.string.create_objects_table));
 		db.execSQL(mContext.getString(R.string.create_locations_table));
 		db.execSQL(mContext.getString(R.string.create_telescope_table));
 		db.execSQL(mContext.getString(R.string.create_eyepiece_table));
 		db.execSQL(mContext.getString(R.string.create_target_lists_table));
 		db.execSQL(mContext.getString(R.string.create_target_list_items_table));
 		db.execSQL(mContext.getString(R.string.create_available_catalogs_table));
 	}
 
 	/**
 	 * Method used to seed values in the available catalogs table
 	 * @throws Exception If the number of values don't match the number of columns in the table, we will throw
 	 */
 	private void populateAvailableCatalogs(SQLiteDatabase db) throws Exception
 	{
 		Log.d("JoeDebug", "Starting populateAvailableCatalogs");
 		SQLiteStatement sqlStatement;
 		
 		//First, get the values and parse them into individual lines, then parse the values, 
 		//create the sql statement, then execute it
 		String[] settingsLines = parseResourceByLine(R.string.available_catalogs_default_values);
 		
 		for (int i = 0; i < settingsLines.length; i++)
 		{
 			String[] rowData = parseResourceByDelimiter(settingsLines[i], availableCatalogsTableColumnCount);
 			
 			//There are four columns to fill in this table
 			if (rowData.length == availableCatalogsTableColumnCount)
 			{
 				sqlStatement = db.compileStatement("INSERT INTO availableCatalogs (catalogName, installed, numberOfObjects, description, size) " + 
 						"VALUES (?, ?, ?, ?, ?)");
 				for (int j = 0; j < rowData.length; j++)
 				{
 					sqlStatement.bindString(j + 1, rowData[j]);
 				}
 				sqlStatement.execute();
 				sqlStatement.clearBindings();
				sqlStatement.close();
 			}
 			else
 			{
 				Log.d("JoeDebug", "Error in populateAvailableCatalogs");
 				throw new Exception("There were not 5 values to populate the catalogs table with. Values were" + rowData.toString());
 			}
 		}
 	}
 
 	/**
 	 * Method used to seed values in the object table
 	 * @throws Exception If the number of values don't match the number of columns in the table, we will throw
 	 */
 	private void populateObjects(SQLiteDatabase db) throws Exception
 	{
 		Log.d("JoeDebug", "Starting populateObjects");
 		SQLiteStatement sqlStatement;
 		
 		//I think for mitigation of file size, we should maintain each catalog in an independent resource file. 
 		//So we need to create a string array to hold each of the catalogs that we want to populate and itterate
 		//through that.
		int[] availableCatalogs = {R.string.messier_catalog_default_values, R.string.ngc_1_catalog_default_values, R.string.ngc_2_catalog_default_values,
				R.string.ngc_3_catalog_default_values, R.string.ngc_4_catalog_default_values, R.string.ngc_5_catalog_default_values, R.string.ngc_6_catalog_default_values,
				R.string.ngc_7_catalog_default_values, R.string.ngc_8_catalog_default_values, R.string.ic_1_catalog_default_values, R.string.ic_2_catalog_default_values,
				R.string.ic_3_catalog_default_values, R.string.ic_4_catalog_default_values, R.string.ic_5_catalog_default_values, R.string.ic_6_catalog_default_values};
 		for (int i = 0; i < availableCatalogs.length; i++)
 		{
			Log.d("JoeDebug", "Starting population of objects for catalog # " + i);
 			//First, get the values and parse them into individual lines, then parse the values, 
 			//create the sql statement, then execute it
 			String[] objectsLines = parseResourceByLine(availableCatalogs[i]);
 			
 			for (int j = 0; j < objectsLines.length; j++)
 			{
 				String[] rowData = parseResourceByDelimiter(objectsLines[j], objectTableColumnCount);
 				
 				if (rowData.length == objectTableColumnCount)
 				{
					//Log.d("JoeDebug", "In Loop. rowData.length passed the test");
 					
 					sqlStatement = db.compileStatement("INSERT INTO objects (designation, commonName, type, magnitude, size," +
 							" distance, constellation, season, rightAscension, declination, objectDescription, catalog, otherCatalogs," +
 							" imageResource, nightImageResource) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 					
 					//We start this array with 1 to make it simple when using the index value. We start the count on our
 					//bound arguments with 1 (it's 1-based), and we want to start with index 1 on the array because the 
 					//first slot [0] is null for the autoincremented _id. We stop at 15 (<=15) because that is the number
 					//of binding args we have
 					for (int k = 1; k <= 15; k++)
 					{
 						sqlStatement.bindString(k, rowData[k]);
 					}
 					sqlStatement.execute();
 					sqlStatement.clearBindings();
					sqlStatement.close();
 				}
 				else
 				{
					Log.w("JoeDebug", "Error in populateObjects");
					throw new Exception("There were not " + objectTableColumnCount + " values to populate the Objects table with. Object was " + rowData[1]);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method used to seed values in the object table
 	 * @throws Exception If the number of values don't match the number of columns in the table, we will throw
 	 */
 	private void populatePersonalInfo(SQLiteDatabase db) throws Exception
 	{
 		Log.d("JoeDebug", "Starting populatePersonalInfo");
 		SQLiteStatement sqlStatement;
 		
 		sqlStatement = db.compileStatement("INSERT INTO personalInfo (_id, fullName, address, phone, eMail, localClub) VALUES (?, ?, ?, ?, ?, ?)");
 		sqlStatement.bindString(1, "1");
 		sqlStatement.bindString(2, "");
 		sqlStatement.bindString(3, "");
 		sqlStatement.bindString(4, "");
 		sqlStatement.bindString(5, "");
 		sqlStatement.bindString(6, "");
 		
 		sqlStatement.execute();
 		sqlStatement.clearBindings();
		sqlStatement.close();
 	}
 
 	/**
 	 * Method used to seed data in the settings table
 	 * @throws Exception If the number of values don't match the number of columns in the table, we will throw
 	 */
 	private void populatePersistentSettings(SQLiteDatabase db) throws Exception
 	{
 		Log.d("JoeDebug", "Starting populatePersistentSettings");
 		SQLiteStatement sqlStatement;
 		
 		//First, get the values and parse them into individual lines, then parse the values, 
 		//create the sql statement, then execute it
 		String[] settingsLines = parseResourceByLine(R.string.settings_default_values);
 		
 		for (int i = 0; i < settingsLines.length; i++)
 		{
 			String[] rowData = parseResourceByDelimiter(settingsLines[i], settingsTableColumnCount);
 			
 			//There are three columns to fill in this table
 			if (rowData.length == settingsTableColumnCount)
 			{
 				sqlStatement = db.compileStatement("INSERT INTO settings (settingName, settingValue, visible) VALUES (?, ?, ?)");
 				for (int j = 0; j < rowData.length; j++)
 				{
 					sqlStatement.bindString(j + 1, rowData[j]);
 				}
 				sqlStatement.execute();
 				sqlStatement.clearBindings();
				sqlStatement.close();
 			}
 			else
 			{
 				Log.d("JoeDebug", "Error in populatePersistentSettings");
 				throw new Exception("There were not 3 values to populate the settings table with. Values were" + rowData.toString());
 			}
 		}
 	}
 	
 	/**
 	 * We have initial database values saved as string resources with one line representing one row in the database.
 	 * We have put in a \n character at the end of each line, and the system has given us a space to represent the 
 	 * line break. We need to parse out to individual lines so we can establish default values row by row.
 	 * 
 	 * @param resource The resource that we want to parse
 	 * @return String[] with each line from the resource in each array index
 	 */
 	public String[] parseResourceByLine(int resource)
 	{
 		String[] retVal;
 		
 		String fullText = mContext.getString(resource);
 		retVal = fullText.split("\n");
 		
 		return retVal;
 	}
 	
 	/**
 	 * Once we have an array of parsed lines returned by parseResourceByLine, we need to split it into individual
 	 * values to populate the database with. We have used semicolons as our delimiter.
 	 * 
 	 * @param resource The String[] that was returned to us from parseResourceByLine
 	 * @return String[][]: Each index represents one line from the resource. Each index then contains a String[] that contains the individual values from that line
 	 */
 	public String[] parseResourceByDelimiter(String resource, int limit)
 	{
 		String[] retVal = resource.split(";", limit); //The limit parameter will ensure that any semicolons included in the log notes will not force unwanted splits
 		
 		return retVal;
 	}
 }
