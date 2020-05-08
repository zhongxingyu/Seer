 package com.turt2live.antishare.debug;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 public class BugCheck {
 
 	public static void verifyEqual(Object object1, Object object2, String message, Class<?> offendingClass){
 		if(object1 == null || object2 == null || message == null || offendingClass == null){
 			Bug bug = new Bug(new NullPointerException("Null: EQUAL TO"), "Values: " + object1 + " " + object2 + " " + message + " " + offendingClass, BugCheck.class, null);
 			Debugger.sendBug(bug);
 			return;
 		}
 		try{
 			if(!object1.equals(object2)){
 				Bug bug = new Bug(new BugException("Failed to make " + object1 + " equal to " + object2), message, offendingClass, null);
 				Debugger.sendBug(bug);
 			}
 		}catch(Exception e){
 			throwInternalBug(e, "Failed: " + message);
 		}
 	}
 
 	public static void verifyLessThan(Float object1, Float object2, String message, Class<?> offendingClass){
 		if(object1 == null || object2 == null || message == null || offendingClass == null){
 			Bug bug = new Bug(new NullPointerException("Null: LESS THAN"), "Values: " + object1 + " " + object2 + " " + message + " " + offendingClass, BugCheck.class, null);
 			Debugger.sendBug(bug);
 			return;
 		}
 		try{
 			if(!(object1 < object2)){
 				Bug bug = new Bug(new BugException("Failed to make " + object1 + " less than " + object2), message, offendingClass, null);
 				Debugger.sendBug(bug);
 			}
 		}catch(Exception e){
 			throwInternalBug(e, "Failed: " + message);
 		}
 	}
 
 	public static void verifyGreaterThan(Float object1, Float object2, String message, Class<?> offendingClass){
 		if(object1 == null || object2 == null || message == null || offendingClass == null){
 			Bug bug = new Bug(new NullPointerException("Null: GREATER THAN"), "Values: " + object1 + " " + object2 + " " + message + " " + offendingClass, BugCheck.class, null);
 			Debugger.sendBug(bug);
 			return;
 		}
 		try{
 			if(!(object1 > object2)){
 				Bug bug = new Bug(new BugException("Failed to make " + object1 + " greater than " + object2), message, offendingClass, null);
 				Debugger.sendBug(bug);
 			}
 		}catch(Exception e){
 			throwInternalBug(e, "Failed: " + message);
 		}
 	}
 
	public static void verifyWritten(File file, String line, String message, Class<?> offendingClass, boolean caseSensitive, boolean startsWith){
 		if(file == null || message == null || offendingClass == null){
 			Bug bug = new Bug(new NullPointerException("Null: WRITTEN"), "Values: " + file + " " + message + " " + offendingClass, BugCheck.class, null);
 			Debugger.sendBug(bug);
 			return;
 		}
 		try{
			line = line.trim().replace("\t", "");
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			boolean found = false;
 			String rline;
 			while ((rline = in.readLine()) != null){
				rline = rline.trim().replace("\t", "");
				if(startsWith){
					if(caseSensitive){
						if(rline.startsWith(line)){
							found = true;
							break;
						}
					}else{
						if(rline.toLowerCase().startsWith(line.toLowerCase())){
							found = true;
							break;
						}
 					}
 				}else{
					if(caseSensitive){
						if(rline.equals(line)){
							found = true;
							break;
						}
					}else{
						if(rline.equalsIgnoreCase(line)){
							found = true;
							break;
						}
 					}
 				}
 			}
 			in.close();
 			if(!found){
 				Bug bug = new Bug(new BugException("Failed to find " + line + " in file " + file.getName()), message, offendingClass, null);
 				Debugger.sendBug(bug);
 			}
 		}catch(Exception e){
 			throwInternalBug(e, "Failed: " + message);
 		}
 	}
 
 	private static void throwInternalBug(Exception e, String message){
 		Bug bug = new Bug(e, "INTERNAL: " + message, BugCheck.class, null);
 		Debugger.sendBug(bug);
 	}
 }
