 package edu.unsw.comp9321.assign2;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
  * validates input from forms
  */
 public class validator {
 	private static Pattern pattern;
 	private static Matcher matcher;
 	
 	public static boolean checkText(String text) {
 		System.out.println("validation string: " + text);
 		String regex = "[^=*][a-zA-Z0-9]*"; // work for user 1
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(text);
 		return matcher.matches();
 	}
 	
 	public static boolean checkPassword(String password) {
 		System.out.println("validation string: " + password);
 		String regex = "[^=*][a-zA-Z0-9]*"; // work for user 1
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(password);
 		return matcher.matches();
 	}
 	
	public static boolean checkNumSequence(String sequence) { // credit card [0-9]{16}
		String regex = "[0-9]";
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(sequence);
 		return matcher.matches();
 	}
 	
	public static boolean checkURL(String url) {
		String regex = "^[a-z]*@[a-z.]*";
 		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(url);
 		return matcher.matches();
 	}
 }
