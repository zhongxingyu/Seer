 package com.uiproject.meetingplanner;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class Logout {
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	private static final int MODE_PRIVATE = 0;
 
 	public static void logout(Context context){
 
 		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
 		SharedPreferences.Editor editor = settings.edit();
     	editor.remove("uid");
     	editor.remove("userPhoneNumber");
     	editor.remove("userFirstName");
     	editor.remove("userLastName");
     	editor.remove("userEmail");
     	editor.remove("remember");
     	editor.commit();
		
 	}
 }
