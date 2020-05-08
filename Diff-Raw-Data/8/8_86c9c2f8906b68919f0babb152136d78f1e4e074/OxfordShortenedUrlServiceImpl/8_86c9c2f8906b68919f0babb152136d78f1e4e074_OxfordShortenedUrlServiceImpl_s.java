 package org.sakaiproject.oxford.shortenedurl.impl;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.oxford.shortenedurl.api.OxfordShortenedUrlService;
 
 /**
  * Implementation of {@link org.sakaiproject.oxford.shortenedurl.api.OxfordShortenedUrlService} for Oxford University's Weblearn system. 
  * 
  * <p>Maps weblearn URLs to m.ox urls.</p>
  *
  * @author Steve Swinsburg (steve.swinsburg@gmail.com)
  *
  */
 public class OxfordShortenedUrlServiceImpl implements OxfordShortenedUrlService {
 
 	private static Log log = LogFactory.getLog(OxfordShortenedUrlServiceImpl.class);
 	private final String BUNDLE_NAME = "org.sakaiproject.oxford.shortenedurl.impl.mappings";
 	private final String MOX_BASE_URL = "https://m.ox.ac.uk/weblearn";
 	
 	private List<String> replacementValues;
 
 	
 	/**
 	 * This must be called with a relative path to https://weblearn.ox.ac.uk/direct, e.g. /poll/123.json or /poll/123.json?siteId=abc
 	 */
 	public String shorten(String path) {
 		log.debug("Path: " + path);
 		
 		//split to check if query string is present
 		//path will be 0, query string will be 1 if given.
 		String[] urlParts = StringUtils.split(path, '?');
 		
 		//split path
 		String[] pathParts = StringUtils.split(urlParts[0], '/');
 		if(pathParts.length == 0) {
 			log.error("Invalid path: " + path);
 			return null;
 		}
 				
 		//get prefix, ignore any possible extension.
 		String prefix = StringUtils.substringBefore(pathParts[0], ".");
 		log.debug("Prefix: " + prefix);
 		
 		//get pattern count
 		String patternCountProperty = prefix + ".pattern.count";
 		log.debug("patternCountProperty: " + patternCountProperty);
 		
 		int patternCount = NumberUtils.toInt(getString(patternCountProperty), 0);
 		if(patternCount == 0){
 			log.error("Missing configuration for: " + patternCountProperty);
 			return null;
 		}
 		
 		Pattern p = null;
 		Matcher m = null;
 		replacementValues = new ArrayList<String>();
 		
 		//check each pattern to find a matching one
 		for(int i=1; i<= patternCount; i++){
 			String patternProperty = prefix + ".pattern." + i;
 			log.debug("patternProperty: " + patternProperty);
 			
 			String pattern = getString(patternProperty);
 			if(StringUtils.isBlank(pattern)){
 				log.warn("No pattern for: " + patternProperty);
 				continue;
 			}
 			log.debug("pattern: " + pattern);
 
             p = Pattern.compile(pattern);
             m = p.matcher(path);
             if(!m.matches()) {
             	continue;
             }
            
             //check for path values we need to transfer
             String pathValuesProperty = prefix + ".path.values." + i;
             log.debug("pathValuesProperty: " + pathValuesProperty);
             String pathValuesExpression = getString(pathValuesProperty);
             log.debug("pathValuesExpression: " + pathValuesExpression);
 
             //if we have path values to substitute
             if(StringUtils.isNotBlank(pathValuesExpression)) {
             	loadPathValues(pathParts, pathValuesExpression);
             }
             
            log.debug("urlParts.length:" + urlParts.length);
             
             //check for query values we need to transfer
             if(urlParts.length > 1) {
             
 	            String queryValuesProperty = prefix + ".query.values." + i;
 	            log.debug("queryValuesProperty: " + queryValuesProperty);
 	            String queryValuesExpression = getString(queryValuesProperty);
 	            log.debug("queryValuesExpression: " + queryValuesExpression);
 	            
 	            if(StringUtils.isNotBlank(queryValuesExpression)) {
 	            	loadQueryValues(urlParts[1], queryValuesExpression);
 	            }
             }
             
             //now get the final URL string we need and interpolate any values we have
             String urlProperty = prefix + ".url." + i;
             log.debug("urlProperty: " + urlProperty);
 
             String url = getString(urlProperty, replacementValues.toArray());
             log.debug("url: " + url);
             
             return url;
 			
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Not implemented
 	 */
 	public String shorten(String url, boolean secure) {
 		log.info("Not implemented.");
 		return url;
 	}
 
 	/**
 	 * Not implemented
 	 */
 	public String resolve(String key) {
 		log.info("Not implemented.");
 		return null;
 	}
 	
 	public void init() {
   		log.debug("WeblearnUrlService init().");
   	}
 	
 	
 	/**
 	 * Get a simple message from the bundle
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private String getString(String key) {
 		return getMessage(key);
 	}
 	
 	/**
 	 * Get a parameterised message from the bundle and perform the parameter substitution on it
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private String getString(String key, Object[] arguments) {
         return MessageFormat.format(getMessage(key), arguments);
     }
 	
 	/**
 	 * Get a standard message from the bundle
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private String getMessage(String key) {
 		try {
 			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
 		} catch (MissingResourceException e) {
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * Get the values from the path based on the given expression, and load into the replacementValues list at the given positions
 	 * @param pathParts		split array of path parts
 	 * @param expression	expression property
 	 */
 	private void loadPathValues(String[] pathParts, String expression) {
 		
 		//first, split the expression up into pairs
 		String[] pairs = StringUtils.split(expression, ',');
 		
 		//foreach pair, the first value maps to the given path, the second maps to the position in the replacementValues list.
 		//so get the value from the path and load it into the list at the specified position
 		for(int i=0; i< pairs.length; i++){
 			String[] parts = StringUtils.split(pairs[i], '=');
 			
 			//get the value of each index
         	int firstIndex = Integer.parseInt(parts[0]);
         	int secondIndex = Integer.parseInt(parts[1]);
 
         	//get the corresponding path value
         	String pathValue = pathParts[firstIndex];
         	
         	//if there is a . in this parameter (ie because an extension may be present), just return the part before it
         	pathValue = StringUtils.substringBefore(pathValue, ".");
         	
         	//add this value to the list at the specified position
         	replacementValues.add(secondIndex, pathValue);
 			
 		}
 	}
 	
 	/**
 	 * Get the parameters from the query based on the given expression, and load into the replacementValues list at the given positions
 	 * @param query		query string
 	 * @param expression	expression property
 	 */
 	private void loadQueryValues(String query, String expression) {
 	
 		//first, convert the string t a map of properties
 		Map<String,String> params = splitQueryStringToMap(query);
 		
 		//now, split the expression into pairs
		String[] pairs = StringUtils.split(query, ',');
 		
 		//foreach pair, the first value maps to a key in the map, the second maps to the position in the replacementValues list.
 		//so get the value map and load it into the list at the specified position
 		log.debug("pairs.length:" + pairs.length);
 		
 		for(int i=0; i< pairs.length; i++){
 			String[] parts = StringUtils.split(pairs[i], '=');
 			
 			//get the value of each part
         	String key = parts[0];
         	int index = Integer.parseInt(parts[1]);
 
         	//add the value from the map to the list at the specified position
 			replacementValues.add(index, params.get(key));
 		}
 		
 	}
 	
 	/**
 	 * Helper to split a query string to a Map.
 	 * @param query		the given query string, eg ?siteId=abc&userId=123
 	 * @return
 	 */
 	private Map<String,String> splitQueryStringToMap(String query) {
 		
 		Map<String,String> params = new HashMap<String,String>();
 		
 		//remove leading ? if present.
 		query = StringUtils.stripStart(query, "?");
 		
 		//split into pairs
 		String[] pairs = StringUtils.split(query, '&');
 
 		//foreach pair, split again and load into map
 		for(int i=0; i< pairs.length; i++){
 			String[] p = StringUtils.split(pairs[i], '=');
 			params.put(p[0], p[1]);
 		}
 		
 		log.debug("params map: " + params.toString());
 		
 		return params;
 		
 	}
 	
 
 }
