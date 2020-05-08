 package edu.csumb.ideasofmarch.codecruncher;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class CrunchConstants {
 	public static final int NUM_GAME_MODES = 6;
 	public static final String LOCAL_HIGH_SCORES = "localHighScores";
 	public static final String backendURL = "http://glacial-scrubland-4587.herokuapp.com";
 	
 	public static final int BINARY_TO_DECIMAL = 1;
 	public static final int HEX_TO_DECIMAL = 2;
 	public static final int DECIMAL_TO_HEX = 3;
 	public static final int DECIMAL_TO_BINARY = 4;
 	public static final int BINARY_TO_HEX = 5;
 	public static final int HEX_TO_BINARY = 6;
 	public static final int BINARY_TO_DECIMAL_HARD = 7;
 	public static final int BINARY_TO_HEX_HARD = 8;
	public static final int HEX_TO_DECIMAL_HARD = 9;
	public static final int DECIMAL_TO_HEX_HARD = 10;
	public static final int DECIMAL_TO_BINARY_HARD = 11;
	public static final int HEX_TO_BINARY_HARD = 12;
 
 	public static final String JSON_NAME = "savedName";
 	
 	public static final String PREFERENCES_FILENAME = "preferences.json";
 	public static final String SCORES_FILENAME = "scores.json";
 	
 	public static Map<String, String> myPreferencesMap;
 	public static Map<Integer, Integer> myScoresMap;
 
 
 }
