 package com.github.Holyvirus.Blacksmith.core.Tools.Sign;
 
 import org.bukkit.block.Sign;
 
 public class SignValidator {
 	
	public static boolean isBlackSmithSign(Sign s) {
		return s.getLine(0).equalsIgnoreCase("[BlackSmith]");
 	}
 	
	public static SignType getType(Sign s) {
 		if(isBlackSmithSign(s)) {
			if(s.getLine(1).equalsIgnoreCase("VALUE")) {
 				return SignType.VALUE;
			}else if(s.getLine(1).equalsIgnoreCase("REPAIR")) {
 				return SignType.REPAIR;
			}else if(s.getLine(1).equalsIgnoreCase("KILL")) {
 				return SignType.KILL;
 			}
 		}
 		
 		return SignType.INVALID;
 	}
 }
