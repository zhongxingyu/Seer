 package com.sgs.hotelguru;
 
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 public class firstUseSetup extends Activity{
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);	
 		setContentView(R.layout.firstuse);
 	}
 	public static final String PREFS_NAME = "HotelGuru_Prefs_File";
 	public void firstuse(){
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
 		
 		if(settings.getBoolean("firstBoot", true))
 				{
 					//FIRST BOOT DETECTED, IMPORT DATABASE AND QUERY FOR LOGIN INFORMATION
 					myDatabase db = new myDatabase(this);
 					db.getWritableDatabase();
 					db.close();
 					//DONE WITH FIRST RUN, SET PREF FLAG SO IT DOESNT RUN AGAIN
 					settings.edit().putBoolean("firstBoot", false).commit();
 			
 				}
 		else
 		{
 			//NORMAL MODE CONTINUE TO MAIN ACTIVITY
 		}
 	}
 	
 		
 }
