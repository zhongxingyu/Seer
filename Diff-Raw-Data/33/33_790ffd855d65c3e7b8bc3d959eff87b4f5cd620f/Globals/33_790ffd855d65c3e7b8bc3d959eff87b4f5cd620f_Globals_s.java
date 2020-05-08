 package com.android.mcameron.singletrack;
 
 import android.app.Application;
 
 public class Globals extends Application {
 	  private static String currentLevel;
 	  private static String currentPack;
 
 	  public static final byte LEVEL_UNSET = -1;
 	  public static final byte LEVEL_DISABLED = 0;
 	  public static final byte LEVEL_ENABLED = 1;
	  public static final byte LEVEL_SOLVED = 1;
 
 	  public static final String PACK_SQUARES = "01";
 	  public static final String PACK_RECTANGLES = "02";
 	  
 	  public void setCurrentLevel(String levelID){
 		  currentLevel = levelID;
 	  }
 	  
 	  public String getCurrentLevel(){
 		  return currentLevel;
 	  }
 	  
 	  public void setCurrentPack(String levelPack){
 		  currentPack = levelPack;
 	  }
 	  
 	  public String getCurrentPack(){
 		  return currentPack;
 	  }
 	  
 	  public String getNextLevel() {
 		  setNextLevel();
 		  return getCurrentLevel();
 	  }
 	  
 	  public void setNextLevel() {
 		  int currentLevel = Integer.parseInt(Globals.currentLevel);
 		  setCurrentLevel(Integer.toString(++currentLevel));
 	  }
 }
