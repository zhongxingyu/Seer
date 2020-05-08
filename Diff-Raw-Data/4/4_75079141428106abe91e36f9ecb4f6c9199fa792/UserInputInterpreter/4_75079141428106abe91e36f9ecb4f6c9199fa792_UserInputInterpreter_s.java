 package com.example.multimodal2;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import multimodal.FuzzyTime;
 import multimodal.schedule.Room;
 import android.annotation.SuppressLint;
 import android.util.Log;
 
 @SuppressLint("DefaultLocale")
 public class UserInputInterpreter {
 	public class UserInput{
 	   // public CommandType commandType;
 	    public String time;
 	    FuzzyTime exactTime;	    
 	}
 	public enum CommandType{
 	    DISPLAY, CANCEL, MOVE, BOOK, WHEN, WHERE, WHO
 	};
 	Room exactLocation;
 	FuzzyTime time;
 	CommandType command;
 	
 	@SuppressLint("DefaultLocale")
 	public UserInputInterpreter(String text) {
 		text = text.toLowerCase();		
 		try {
 			this.time = this.interpreteTime(text);
 		} catch (UserInputNotUnderstoodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(text.contains("when")) {
 			this.command = CommandType.WHEN;
 		} else if(text.matches("where")) {
 			this.command = CommandType.WHERE;
 		} else if(text.matches("show")) {
 			this.command = CommandType.DISPLAY;
 		} else if(text.matches("move")) {
 			this.command = CommandType.MOVE;
 		} else if(text.matches("cancel")) {
 			this.command = CommandType.CANCEL;
 		}
 	}
 
 	private static Map<String, Integer> timeUnitMultiplier;
     static {
     	timeUnitMultiplier = new HashMap<String, Integer>();
     	timeUnitMultiplier.put("a.m.", 0);
     	timeUnitMultiplier.put("p.m", 60*60*12);
     	timeUnitMultiplier.put("o'clock", 0);
     	timeUnitMultiplier.put("hours", 60*60);
     	timeUnitMultiplier.put("minutes", 60);
     	timeUnitMultiplier.put("days", 60*60*24);
     	timeUnitMultiplier.put("month", 60*60*24*30);
     	timeUnitMultiplier = Collections.unmodifiableMap(timeUnitMultiplier);
     }
 	
 	private FuzzyTime interpreteTime(String time) throws UserInputNotUnderstoodException {
 		FuzzyTime fuztime = interpreteTimeInFuture(time);
 		if(fuztime != null){
 			return fuztime;
 		}
 		fuztime = interpreteExactTime(time);
 		if(fuztime != null){
 			return fuztime;
 		}
 	
 		if(time.contains("yesterday")){
 			return FuzzyTime.nowPlusSeconds(-24*60*60);
 		} else if(time.contains("day after tomorrow")){
 			int timeAdd = timeUnitMultiplier.get("days")*2;
 			return FuzzyTime.nowPlusSeconds(timeAdd);
 		} else if(time.contains("tomorrow")){
			int timeAdd = timeUnitMultiplier.get("days")*2;
			return FuzzyTime.nowPlusSeconds(24*60*60);
 		} else if(time.equals("next")){
 			return FuzzyTime.now();
 		} else {			
 			throw new UserInputNotUnderstoodException("Time: "+time);
 		}
 	}
 	
 	private FuzzyTime interpreteExactTime(String time) {
 		time = time.toLowerCase(Locale.getDefault());
 		Pattern datePatt = Pattern.compile("at (\\d+) (p\\.m\\.|a\\.m\\.|o'clock)");
 		Matcher m = datePatt.matcher(time);
 		if (m.matches()){
 			if( m.groupCount() == 2 && timeUnitMultiplier.containsKey(m.group(1))){
 				try{
 					int hours = Integer.parseInt(m.group(0));
 					return FuzzyTime.nowPlusSeconds(timeUnitMultiplier.get("hours")*hours*timeUnitMultiplier.get(m.group(1)));
 				} catch(NumberFormatException e){
 					Log.e(this.getClass().getSimpleName(), "error parsing time number "+m.group(0));
 				} catch(NullPointerException e){
 					Log.e(this.getClass().getSimpleName(), "no such time unit "+m.group(1));
 				}
 			} else {
 				Log.e(this.getClass().getSimpleName(),"cannot match exact time, no such timeMultiplier!");
 			}
 		}
 		return null;
 	}
 
 	private FuzzyTime interpreteTimeInFuture(String time){
 		time = time.toLowerCase(Locale.getDefault());
 		Pattern datePatt = Pattern.compile("in (\\d+) (hours|minutes|days|months)");
 		Matcher m = datePatt.matcher(time);
 		if (m.matches()) {
 			if(m.groupCount()<3){
 				Log.e(this.getClass().getSimpleName(), "Error, not enough regex groups found in string: "+time);
 			}
 			Log.i(this.getClass().getSimpleName(), "Failed parsing number "+m.group(1));
 			try{
 				Integer baseNumber = Integer.parseInt(m.group(1));
 				if(timeUnitMultiplier.containsKey(m.group(2))){
 					return FuzzyTime.nowPlusSeconds(baseNumber*timeUnitMultiplier.get(m.group(2)));
 				} else {
 					Log.e(this.getClass().getSimpleName(), "There is no time Unit called "+m.group(2));
 				}
 			} catch(NumberFormatException e){
 				Log.i(this.getClass().getSimpleName(), "Failed parsing number "+m.group(1));
 			}
 		}
 		return null;
 	}
 	
 
 	
 	class UserInputNotUnderstoodException extends Exception{
 		UserInputNotUnderstoodException(String msg){
 			super(msg);
 		}
 	}
 	public static void userSaidCancel() {
 		Log.d("SpeechRepeatActivity", "matched cancel!");
 	}
 
 	public static void userSaidMove() {
 		Log.d("SpeechRepeatActivity", "matched move!");	
 	}
 
 	public static void userSaidShow() {
 		Log.d("SpeechRepeatActivity", "matched show!");
 	}
 
 	public static void userSaidWhere() {
 		
 		Log.d("SpeechRepeatActivity", "matched where!");
 	}
 
 	public static void userSaidWhen() {
 		Log.d("SpeechRepeatActivity", "matched when!");
 	}
 	
 }
