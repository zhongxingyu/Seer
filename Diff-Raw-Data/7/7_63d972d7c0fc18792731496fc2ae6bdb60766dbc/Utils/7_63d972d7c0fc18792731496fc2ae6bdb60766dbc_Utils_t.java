 package de.wak_sh.client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Utils {
 	public static String match(String pattern, String subject) {
		Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(
				subject);
 		matcher.find();
 		return matcher.group(1);
 	}
 
 	public static List<String[]> matchAll(String pattern, String subject) {
 		List<String[]> matches = new ArrayList<String[]>();
		Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(
				subject);
 		while (matcher.find()) {
 			int count = matcher.groupCount();
 			String[] match = new String[count];
 			for (int i = 0; i < count; i++) {
 				match[i] = matcher.group(i + 1);
 			}
 			matches.add(match);
 		}
 		return matches;
 	}
 }
