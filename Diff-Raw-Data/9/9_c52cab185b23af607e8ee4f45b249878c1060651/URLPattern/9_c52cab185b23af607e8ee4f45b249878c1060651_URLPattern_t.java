 package layr.engine.expressions;
 
 import static layr.commons.Reflection.stripAttribute;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class URLPattern {
 
     private static final String RE_IS_VALID_EXPRESSION = "\\{[\\w:]+?[\\w\\.]+?\\}";
 
 	/**
      * Parse Method URL
      * @param pattern
      * @return a Regular Expression that matches the URL pattern
      */
     public String parseMethodUrlPatternToRegExp ( String pattern ) {
     	Matcher matcher = getMatcher(RE_IS_VALID_EXPRESSION, pattern);
    	return matcher.replaceAll("([^\\/]+)");
     }
 
     /**
      * Extract the place holders value from URL based on URL pattern
      * @param pattern
      * @param url
      * @return
      */
     public Map<String, String> extractMethodPlaceHoldersValueFromURL( String pattern, String url ) {
 		String regex = parseMethodUrlPatternToRegExp(pattern);
 		Matcher urlpatternMatcher = getMatcher( RE_IS_VALID_EXPRESSION, pattern);
 		Matcher urlMatcher = getMatcher(regex, url);
 		return createPlaceHolderMapExtractingDataFromURL(
 					urlpatternMatcher, urlMatcher);
     }
 
 	/**
 	 * @param urlpatternMatcher
 	 * @param urlMatcher
 	 * @return
 	 */
 	public Map<String, String> createPlaceHolderMapExtractingDataFromURL(Matcher urlpatternMatcher,
 			Matcher urlMatcher) {
 		Map<String, String> placeHolders = new HashMap<String, String>();
 
 		int cursor = 1;
 		if (urlMatcher.find())
 			while ( cursor <= urlMatcher.groupCount() && urlpatternMatcher.find() ) {
 				populateMapWithPlaceHoldersExtractedFromURL(placeHolders, urlpatternMatcher, urlMatcher, cursor);
 				cursor++;
 			}
 
 		return placeHolders;
 	}
 
 	/**
 	 * @param placeHolders
 	 * @param urlpatternMatcher
 	 * @param urlMatcher
 	 * @param cursor
 	 */
 	public void populateMapWithPlaceHoldersExtractedFromURL(Map<String, String> placeHolders, Matcher urlpatternMatcher,
 			Matcher urlMatcher, int cursor) {
 		String value = urlMatcher.group(cursor);
 		String group = urlpatternMatcher.group();
 		String expression = group.substring( 1, group.length() - 1 );
 		String placeHolder = stripAttribute( expression )[0];
 		placeHolders.put(placeHolder, value);
 	}
 	
 	/**
 	 * @param expression
 	 * @param string
 	 * @return
 	 */
 	public Matcher getMatcher(String expression, String string) {
 		Pattern pattern = Pattern.compile(expression);
 		return pattern.matcher(string);
 	}
 }
