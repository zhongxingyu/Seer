 package net.eledge.android.eu.europeana.tools;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import net.eledge.android.eu.europeana.Config;
 
 public class UriHelper {
 
 	public static URL getSearchURL(String[] terms, int page) {
 		try {
 			return new URL(createSearchUrl(terms, page));
 		} catch (MalformedURLException e) {
 			return null;
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 
 	public static URI getSearchURI(String[] terms, int page) {
 		try {
 			return new URI(createSearchUrl(terms, page));
 		} catch (URISyntaxException e) {
 			return null;
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 	
 	private static String createSearchUrl(String[] terms, int page) throws UnsupportedEncodingException {
 		StringBuilder sb = new StringBuilder();
 		for (int i=0; i<terms.length; i++) {
 			String termEncoded = URLEncoder.encode(terms[i], Config.JSON_CHARSET);
 			sb.append(i==0?"":"&qf=").append(termEncoded);
 		}
		return String.format(Config.URL_API_SEARCH, Config.API_KEY, Integer.valueOf(page), sb.toString());
 	}
 
 	public static URI getSuggestionURI(String term) {
 		try {
 			String termEncoded = URLEncoder.encode(term, Config.JSON_CHARSET);
 			return new URI(String.format(Config.URL_API_SUGGESTIONS, termEncoded));
 		} catch (URISyntaxException e) {
 			return null;
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 
 }
