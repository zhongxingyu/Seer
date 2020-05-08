 package com.bugfullabs.qube.game;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 
 import com.bugfullabs.qube.level.Level;
 
 
 
 
 public class ScoreReader{
 
 	private static String mFilename;
 	
 	private static SharedPreferences score;
 	
 	private static SharedPreferences.Editor scoreEditor;
 	
 	public static void setFilename(Activity a, String pName){
 		mFilename = pName;
 		
 		score = a.getSharedPreferences(pName, 0);
 		scoreEditor = score.edit();
 	}
 	
 	public static void setScore(Level level, int score){
 		
 		scoreEditor.putInt(Integer.toString(level.getLevelpackId()) + Integer.toString(level.getLevelId()) + "score", score);
 		scoreEditor.commit();
 	}
 	
 	public static void setStars(Level level, int stars){
 		
 		scoreEditor.putInt(Integer.toString(level.getLevelpackId()) + Integer.toString(level.getLevelId()) + "stars", stars);
 		scoreEditor.commit();
 	}
 	
 	public static int getScore(Level level){
 		
 		return score.getInt(Integer.toString(level.getLevelpackId()) + Integer.toString(level.getLevelId()) + "score", 0);
 		
 	}
 	
 	public static String getFile(){
 		return mFilename;
 	}
 	
 	public static int getStars(Level level){
 		
 		return score.getInt(Integer.toString(level.getLevelpackId()) + Integer.toString(level.getLevelId()) + "stars", 0);
 		
 	}
 	
 	public static void addTotalCubes(int value){
 		
 		scoreEditor.putInt("TotalCubes", (score.getInt("TotalCubes", 0) + value));
 		scoreEditor.commit();
 	}
 
 	public static int getTotalCubes() {
 
 		return score.getInt("TotalCubes", 0);
 	}
 	
	
 	public static void setAllScores(Level level, int numberOfLevels){
 		
 		int all = 0;
 		
 		for(int i = 0; i < numberOfLevels; i++)
 		{
 			all += score.getInt(level.getLevelpackId() + Integer.toString(i) + "score", 0);
 		}	
 		scoreEditor.putInt("levelpack"+Integer.toString(level.getLevelpackId()), all);
 		scoreEditor.commit();
 	}
 	
 	public static int getLevelpackScore(Level level){
 		return score.getInt("levelpack"+Integer.toString(level.getLevelpackId()), 0);
 	}
 
 	public static int getLevelpackScore(int number){
 		return score.getInt("levelpack"+Integer.toString(number), 0);
 	}
 	
 	
 }
