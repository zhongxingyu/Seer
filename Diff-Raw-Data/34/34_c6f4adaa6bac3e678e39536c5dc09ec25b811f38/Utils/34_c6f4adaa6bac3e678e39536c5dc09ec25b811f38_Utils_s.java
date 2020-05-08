 package fr.warlog.util;
 
 import java.io.File;
 import java.lang.reflect.Array;
 
 /**
  * All utils
  * @author ABC-OBJECTIF\philippe.demanget
  */
 public class Utils {
 	
 	public static Object[] newArray(Class clazz){
 		return (Object[]) Array.newInstance(clazz,0);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T[] trapNull(T[] p){
 		return (T[])( p==null? newArray(Object.class):p);
 	}
 	@SuppressWarnings("unchecked")
 	public static  File[] trapNull(File[] p){
 		return (File[])( p==null? newArray(File.class):p);
 	}
 
 }
