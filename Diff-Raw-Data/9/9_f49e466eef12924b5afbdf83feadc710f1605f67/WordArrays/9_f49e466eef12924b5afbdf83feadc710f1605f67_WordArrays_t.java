 package com.utilis;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class WordArrays {
 	
 	public static String seperateWord(String s, int i){
 		String word = "";
 		String wordHolder = "";
 		int wordCount = 0;
 		StringTokenizer token = new StringTokenizer(s);
 		while (token.hasMoreTokens()){
 			wordCount++;
 			wordHolder = token.nextToken();
 			if (wordCount==i){
 				word = wordHolder;
 			}
 		}
 		return word;
 	}
 	public static int numOfWords(String s){
 		int wordCount = 0;
 		StringTokenizer token = new StringTokenizer(s);
 		while (token.hasMoreTokens()){
 			wordCount++;
 			token.nextToken();
 		}
 		return wordCount;
 			
 	}
 	public static String[] arrayOfWords(String s){
 		String wordHolder = "";
 		ArrayList<String> al = new ArrayList<String>();
 		StringTokenizer token = new StringTokenizer(s);
 		while (token.hasMoreTokens()){
 			wordHolder = token.nextToken();
 			al.add(wordHolder);
 		}
 		
 		return  objectArrayToStringArray(al.toArray());
 	}
 	
 	public static String[] objectArrayToStringArray(Object[] o){
 		String[] array = new String[o.length];
 		for (int i=0;i<o.length;i++){
 			array[i] = (String)o[i];
 		}
 		return array;
 	}
 	public static int[] objectArrayToIntArray(Object[] o){
 		int[] array = new int[o.length];
 		for (int i=0;i<o.length;i++){
 			array[i] = (Integer)o[i];
 		}
 		return array;
 	}
 	public static float[] objectArrayToFloatArray(Object[] o){
 		float[] array = new float[o.length];
 		for (int i=0;i<o.length;i++){
 			array[i] = (Float)o[i];
 		}
 		return array;
 	}
 	public static char[] objectArrayToCharArray(Object[] o){
 		char[] array = new char[o.length];
 		for (int i=0;i<o.length;i++){
 			array[i] = (Character)o[i];
 		}
 		return array;
 	}
	/*
	public static <T> T[] objectArrayToTypeArray(Object[] o){
		T[] array = new T[o.length];
		for (int i=0;i<o.length;i++){
			array[i] = (T)o[i];
		}
		return array;
	}
	*/
 	
 }
