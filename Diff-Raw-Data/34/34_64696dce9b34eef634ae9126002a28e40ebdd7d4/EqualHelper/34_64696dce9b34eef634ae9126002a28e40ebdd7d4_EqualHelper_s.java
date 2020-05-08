 package net.sparktank.nemain.helpers;
 
 public class EqualHelper {
 	
	public static boolean areEqual(Object aThis, Object aThat){
 		return aThis == null ? aThat == null : aThis.equals(aThat);
 	}
 	
 }
