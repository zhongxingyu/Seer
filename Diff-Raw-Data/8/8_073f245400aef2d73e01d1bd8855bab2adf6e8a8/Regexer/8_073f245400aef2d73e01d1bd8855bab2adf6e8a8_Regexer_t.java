 package com.felixware.gw2w.utilities;
 
 import jregex.Matcher;
 import jregex.Pattern;
 import jregex.Replacer;
 
 public class Regexer {
 	private static String mInput;
 
 	// Wooo! Take it off!
 	public static String strip(String input) {
 		mInput = input;
 
 		removeRedLinks();
 
 		removeEditSections();
 
 		removeFileLinks();
 
 		// Log.i("Regex", mInput);
 
 		return mInput;
 	}
 
 	private static void removeRedLinks() {
		Pattern pattern = new Pattern("<a href=\"[^\"]+?\" class=\"new\" title=\"[^\"]+?\">(.+?)<\\/a>");
 		Replacer replacer = pattern.replacer("$1");
 		String result = replacer.replace(mInput);
 		mInput = result;
 
 	}
 
 	private static void removeEditSections() {
 		Pattern pattern = new Pattern("<span class=\"editsection\">.+?<\\/span>");
 		Replacer replacer = pattern.replacer("");
 		String result = replacer.replace(mInput);
 		mInput = result;
 	}
 
 	private static void removeFileLinks() {
		Pattern pattern = new Pattern("<a href=\"[^\"]+?\" class=\"image\">(.+?)<\\/a>");
 		Replacer replacer = pattern.replacer("$1");
 		String result = replacer.replace(mInput);
 
		Pattern pattern2 = new Pattern("(<img alt=\"[^\"]+?\" src=\"([^\"]+?)\".+?/>)");
 		Replacer replacer2 = pattern2.replacer("<a href=\"$2\">$1</a>");
 		String result2 = replacer2.replace(result);
 		mInput = result2;
 
 	}
 
 	public static String getImageUrl(String url) {
 		Pattern pattern = new Pattern("thumb/");
 		Matcher matcher = pattern.matcher(url);
 		if (matcher.find()) {
 			Pattern pattern2 = new Pattern("(.+?)thumb/(.+?)\\..+?\\.(.+?)");
 			Replacer replacer = pattern2.replacer("$1$2.$3");
 			return replacer.replace(url);
 		} else {
 			return url;
 		}
 	}
 
 }
