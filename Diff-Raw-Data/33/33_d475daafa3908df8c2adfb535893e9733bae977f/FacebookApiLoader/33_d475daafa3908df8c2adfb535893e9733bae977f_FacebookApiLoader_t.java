 package com.sk.impl2;
 
import java.net.MalformedURLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.sk.api.NameComparison;
 import com.sk.parse.Extractor;
 
 public class FacebookApiLoader extends GoogleSearchLoader {
 
 	private static final Pattern URL_PATTERN = Pattern.compile("^https?://www\\.facebook\\.com/([^/]+)$");
 	private static final Pattern TITLE_PATTERN = Pattern.compile("(.*?) [|] Facebook");
 
 	static final String SITE_KEY = "facebook";
 
 	public FacebookApiLoader(final String first, final String last) {
 		super("site:facebook.com " + first + " " + last, new SearchAcceptor() {
 			private final String[] names = new String[] { first, last };
 
 			@Override
 			public Extractor getExtractor(SearchResult result) {
 				String id = getId(result);
 				if (id == null)
 					return null;
 				String[] currentNames = getNames(result);
 				if (currentNames == null || !NameComparison.get().isSameName(names, currentNames))
 					return null;
				try {
					return new FacebookPersonLoader(id);
				} catch (MalformedURLException e) {
					return null;
				}
 			}
 		});
 	}
 
 	private static String getId(SearchResult result) {
 		Matcher urlMatcher = URL_PATTERN.matcher(result.url);
 		if (urlMatcher.find())
 			return urlMatcher.group(1);
 		else
 			return null;
 	}
 
 	private static String[] getNames(SearchResult result) {
 		Matcher titleMatcher = TITLE_PATTERN.matcher(result.title);
 		if (titleMatcher.find())
 			return parseName(titleMatcher.group(1));
 		return null;
 	}
 
 	private static String[] parseName(String group) {
 		return NameComparison.get().parseName(group);
 	}
 
 }
