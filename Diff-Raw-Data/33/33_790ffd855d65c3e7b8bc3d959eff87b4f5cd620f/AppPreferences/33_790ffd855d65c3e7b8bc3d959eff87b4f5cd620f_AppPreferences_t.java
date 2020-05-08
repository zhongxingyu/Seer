 package com.android.mcameron.singletrack;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class AppPreferences {
 	private static final String APP_PREFERENCES = "SINGLETRACK_PREFERENCES";
 
 	private SharedPreferences appSharedPrefs;
 	private SharedPreferences.Editor prefsEditor;
 	
 	static Globals globals = new Globals();
 	 
 	public AppPreferences(Context context){
 		this.appSharedPrefs = context.getSharedPreferences(APP_PREFERENCES, Activity.MODE_PRIVATE);
 		this.prefsEditor = appSharedPrefs.edit();
 	}
 	 
 	public int getLevelState(String levelPack, String levelNumber) {
 		return appSharedPrefs.getInt(levelPack + levelNumber, Globals.LEVEL_DISABLED);
 	}
 	 
 	public void setLevelState(String levelPack, String levelNumber, int levelState) {
 		prefsEditor.putInt(levelPack + levelNumber, levelState).commit();
 	}
 	 
 	public void clear() {
 		prefsEditor.clear().commit();
 	}
 	 
 	public int getHighestUnlockedLevel() {
 		Levels levels = new Levels();
 		int i;
		int unsolvedCount = 0;
		for (i = 2; i < levels.size() ; i++) {
 			int levelState = getLevelState(globals.getCurrentPack(), String.format("%02d", i));
 			Log.d("Counting", "Level "+ i +" has LevelState: "+ levelState);
			if (levelState == Globals.LEVEL_ENABLED) {
				unsolvedCount++;
				if (unsolvedCount == 2) {
					break;
				}
			}
 			if (levelState == Globals.LEVEL_DISABLED) {
 				Log.d("Counting", "break you cunt");
 				break;
 			}
 		}
 		
 		return i;
 	}
 }
