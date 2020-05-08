 package com.secondhand.twirl;
 
 import java.util.List;
 
 import android.R;
 import android.content.Context;
 import android.content.res.Resources;
 
 /**
  * Singelton class for accessing the localization strings. 
  */
 public class LocalizationStrings {
 
 	private static LocalizationStrings instance = null;
 
 	private Context context;
 
 	private LocalizationStrings() {}
 
 	public static LocalizationStrings getInstance() {
 		if(instance == null) {
 			instance = new LocalizationStrings();
 		}
 		return instance;
 	}
 
	
 
 	public void initialize(Context context) {
 		this.context = context;
 	}
 
 	public String getLocalizedString(String stringName) {
 		String packageName = context.getPackageName();
 		int resId = context.getResources().getIdentifier(stringName, "string", packageName);
 		return context.getString(resId);
 	}
 	
 	// Don't know if this is correct
 	public String[] getLocalizedStringArray(String stringName){
 		String packageName = context.getPackageName();
 		int resId = context.getResources().getIdentifier(stringName, "string", packageName);
 		return context.getResources().getStringArray(resId);
 	}
 	
 	// Don't know if this is correct
 	public int[] getLocalizedIntArray(String stringName){
 		String packageName = context.getPackageName();
 		int resId = context.getResources().getIdentifier(stringName, "string", packageName);
 		return context.getResources().getIntArray(resId);
 	}
 }
