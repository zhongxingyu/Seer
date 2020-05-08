 /**
  * 
  */
 package com.asksven.mytrack.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.asksven.andoid.common.contrib.Util;
 import com.asksven.android.common.utils.DataStorage;
 import com.asksven.android.common.utils.DateUtils;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * @author sven
  *
  */
 public class LocationWriter
 {
 	private static final String TAG = "LocationWriter";
 	private static final String FILENAME = "MyTrack";
 	
 	public static void LogLocationToFile(Context context, Location myLoc)
 	{
 		Uri fileUri = null;
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(context);
 
 		if (!DataStorage.isExternalStorageWritable())
 		{
 			Log.e(TAG, "External storage can not be written");
 			Toast.makeText(context, "External Storage can not be written",
 					Toast.LENGTH_SHORT).show();
 		}
 		try
 		{
 			// open file for writing
 			File root;
 
 			try
 			{
 				root = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
 			}
 			catch (Exception e)
 			{
 				root = Environment.getExternalStorageDirectory();
 			}
 
 			String path = root.getAbsolutePath();
 			// check if file can be written
 			if (root.canWrite())
 			{
 				String timestamp = DateUtils.now("yyyy-MM-dd_HHmmssSSS");
 				File textFile = new File(root, FILENAME + ".txt");
 
 				FileWriter fw = new FileWriter(textFile);
 				BufferedWriter out = new BufferedWriter(fw);
 				out.append(timestamp + ": " 
 						+ "LAT=" + myLoc.getLatitude()
 						+ "LONG=" + myLoc.getLongitude() + "\n");
 				out.close();
 				
 				File jsonFile = new File(root, FILENAME + ".json");
 
 				fw = new FileWriter(jsonFile);
 				out = new BufferedWriter(fw);
				out.append("TrackEntry\n");
 				Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
 				out.append(gson.toJson(new TrackEntry(myLoc)));
				out.append("\n");
 				out.close();
 
 			}
 			else
 			{
 				Log.i(TAG,
 						"Write error. "
 								+ Environment.getExternalStorageDirectory()
 								+ " couldn't be written");
 			}
 		} catch (Exception e)
 		{
 			Log.e(TAG, "Exception: " + e.getMessage());
 		}
 	}
 
 }
