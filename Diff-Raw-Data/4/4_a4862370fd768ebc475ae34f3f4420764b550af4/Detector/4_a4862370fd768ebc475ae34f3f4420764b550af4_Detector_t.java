 package no.kaedeno.enonic.detector;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.enonic.cms.api.plugin.PluginConfig;
 import com.enonic.cms.api.plugin.PluginEnvironment;
 import com.enonic.cms.api.plugin.ext.http.HttpInterceptor;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 import ua_parser.Parser;
 import ua_parser.Client;
 
 public class Detector extends HttpInterceptor {
 
 	private PluginEnvironment pluginEnvironment = null;
 	private PluginConfig pluginConfig = null;
 
 	private MongoClient mongoClient = null;
 	
 	private String cookieID = "Modernizr";
 
 	private Logger log = Logger.getLogger("Detector");
 
 	/**
 	 * Handles the request received by the server before it is executed by Enonic CMS.
 	 * It is responsible for checking the user-agent string in the request against the database
 	 * to find the features and capabilities of the user-agent making the request.
 	 * If the string is not present in the database it sends a Modernizr test suite to the client
 	 * to check for its user-agent features, and parses the user-agent string for any useful 
 	 * information.
 	 * <p>
 	 * The features and capabilities are written to the Enonic device class resvolver script XML.
 	 * 
 	 * @param httpServletRequest	the http request
 	 * @param httpServletResponse	the http response
 	 * @return						true if the request should be passed to execution in Enonic CMS
 	 * 								or false if not.
 	 */
 	@Override
 	public boolean preHandle(HttpServletRequest httpServletRequest,
 			HttpServletResponse httpServletResponse) throws Exception {
 
 		// Database connection
 		String mongoURI = (String) pluginConfig.get("mongouri");
 		int mongoPort = Integer.parseInt(pluginConfig.get("mongoport"));
 		String mongoName = (String) pluginConfig.get("mongoname");
 		String mongoCollection = (String) pluginConfig.get("mongocollection");
 
 		setMongoClient(new MongoClient(mongoURI, mongoPort));
 		DB db = mongoClient.getDB(mongoName);
 		DBCollection coll = getMongoCollection(db, mongoCollection);
 
 		// 1. Look up UA string in database
 		String userAgent = httpServletRequest.getHeader("User-Agent");
 		DBObject result = coll.findOne(new BasicDBObject("user-agent", userAgent));
 
 		// 2. If found:
 		if (result != null) {
 
 			log.info("=== DATABASE ===");
 			log.info("Result from DB: " + result);
 
 			// TODO: 2.1 Set UA features in context XML
 
 			return true;
 		}
 		// 3. If not found:
 		else {
 			// 3.1 Send Modernizr tests to client
 			Cookie cookie = getCookie(httpServletRequest.getCookies(), this.cookieID);
 			BasicDBObject parsedCookie = null;
 			if (cookie == null) {
 				sendClientTests(httpServletResponse);
 				return false;
 			} else {
 				parsedCookie = parseCookie(cookie.getValue());
 				cookie.setMaxAge(0);
 				cookie.setValue("");
 				httpServletResponse.addCookie(cookie);
 			}
 
 			// 3.2 Check UA string for useful information
 			Parser uaParser = new Parser();
 			Client c = uaParser.parse(userAgent);
 
 			// 3.3. Store UA Parser and Modernizr results in database
 			BasicDBObject userAgentData = new BasicDBObject("user-agent", userAgent)
 					.append("ua",
 							new BasicDBObject("family", c.userAgent.family).append("major",
 									c.userAgent.major).append("minor", c.userAgent.minor))
 					.append("os",
 							new BasicDBObject("family", c.os.family).append("major", c.os.major)
 									.append("minor", c.os.minor))
 					.append("device",
 							new BasicDBObject("family", c.device.family).append("isMobile",
 									c.device.isMobile).append("isSpider", c.device.isSpider))
 					.append("features", parsedCookie);
 
 			coll.insert(userAgentData);
 			log.info("=== DATABASE ===");
 			log.info("Inserted into DB: " + userAgentData);
 
 			// TODO: 3.4. Set UA features in context XML
 
 			return true;
 		}
 	}
 	
 	/**
 	 * Parses the value of a cookie with the detector cookie format and builds a 
 	 * MongoDB object out of it.
 	 * 
 	 * Adapted from modernizr-server
 	 * 
 	 * @param cookie	the value of a cookie as a string
 	 * @return 			the BasicDBObject containing the information from the cookie or
 	 * 					null if the value is null or has a length of 0
 	 */
 	private BasicDBObject parseCookie(String cookie) {
 		if (cookie == null || cookie.length() > 0) {
 			BasicDBObject uaFeatures = new BasicDBObject();
 			for (String feature : cookie.split("\\|")) {
 				String[] nameValue = feature.split("--", 2);
 				String name = nameValue[0];
 				String value = nameValue[1];				
 				if (value.charAt(0) == '/') {
 					BasicDBObject valueObject = new BasicDBObject();
 					for (String subFeature : value.substring(1).split("/")) {
 						nameValue = subFeature.split("--", 2);
 						String subName = nameValue[0];
 						String subValue = nameValue[1];				
 						valueObject.append(subName, trueFalse(subValue));
 					}
 					uaFeatures.append(name, valueObject);
 				} else {
 					uaFeatures.append(name, trueFalse(value));
 				}
 			}
 			return uaFeatures;
 		} 
 		return null;
 	}
 	
 	/**
 	 * Decides whether a string represents the boolean values true or false
 	 * 
 	 * @param value 	the value as a string that is to be checked
 	 * @return			true if the value equals "1", false if not
 	 */
 	private boolean trueFalse(String value) {
 		return value.equals("1") ? true : false;
 	}
 	
 	/**
 	 * Gets a specific cookie from an array of cookies
 	 * 
 	 * @param cookies		the array of cookie
 	 * @param cookieName	the name of the specific cookie
 	 * @return				the cookie object if the name is present in the array, null if not
 	 */
 	private Cookie getCookie(Cookie[] cookies, String cookieName) {
 		for (Cookie c : cookies) {
 			if (cookieName.equals(c.getName())) {
 				return c;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Generates a string with HTML markup and the appropriate JavaScript code to run
 	 * Modernizr tests on the client.
 	 * 
 	 * @return the generated markup
 	 */
 	private String generateMarkup() {
 		String modernizrFile = (String) pluginConfig.get("modernizr");
 		String modernizrScript = null;
 
 		InputStream is = getClass().getClassLoader().getResourceAsStream("/" + modernizrFile);
 		Scanner sc = new Scanner(is);
 		modernizrScript = sc.useDelimiter("\\Z").next();
 		sc.close();
 
 		return "<!DOCTYPE html><html><head><meta charset='utf-8'><script type='text/javascript'>" 
 				+ modernizrScript + generateCookieJS(true)
 				+ "</script></head><body></body></html>";
 	}
 	
 	/**
 	 * Generates the JavaScript code for reading the Modernizr test result object and writes
 	 * the results to a cookie formatted in key-value pairs.
 	 * 
 	 * Adapted from modernizr-server
 	 * 
 	 * @param reload	true if the script should reload the page after creating the cookie,
 	 * 					false if not
 	 * @return			the generated code
 	 */
 	private String generateCookieJS(boolean reload) {
 	
 		String output = "var m=Modernizr,c='';"+
 	      "for(var f in m){"+
 	        "if(f[0]=='_'){continue;}"+
 	        "var t=typeof m[f];"+
 	        "if(t=='function'){continue;}"+
 	        "c+=(c?'|':'" + this.cookieID + "=')+f+'--';"+
 	        "if(t=='object'){"+
 	          "for(var s in m[f]){"+
 	            "c+='/'+s+'--'+(m[f][s]?'1':'0');"+
 	          "}"+
 	        "}else{"+
 	          "c+=m[f]?'1':'0';"+
 	        "}"+
 	      "}"+
 	      "c+=';path=/';"+
 	      "try{"+
 	        "document.cookie=c;";
 	      if(reload) {
 	        output += "document.location.reload();";
 	      }
 	      output += "}catch(e){}";
 		
 		return output;
 	}
 	
 	/**
 	 * Sends markup containing the necessary JavaScript code to run Modernizr tests on the client
 	 * as well as generating a cookie with the test results that can be sent to the server.
 	 * 
 	 * @param httpServletResponse the http response
 	 */
 	private void sendClientTests(HttpServletResponse httpServletResponse) {
 		String markup = generateMarkup();
 		try {
 			PrintWriter w = httpServletResponse.getWriter();
 			w.write(markup);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Prints various debug information to the logger
 	 * 
 	 * @param c the UA parser Client object
 	 */
 	@SuppressWarnings("unused")
 	private void printDebugInfo(Client c) {
 		log.info("=== UA PARSER RESULT ===");
 		log.info("UA Family: " + c.userAgent.family);
 		log.info("UA Major version: " + c.userAgent.major);
 		log.info("UA Minor version: " + c.userAgent.minor);
 
 		log.info("OS Family: " + c.os.family);
 		log.info("OS Major version: " + c.os.major);
 		log.info("OS Minor version: " + c.os.minor);
 
 		log.info("Device Family: " + c.device.family);
 		log.info("Device is mobile: " + new Boolean(c.device.isMobile).toString());
 		log.info("Device is a spider: " + new Boolean(c.device.isSpider).toString());
 
 		log.info("=== SESSION ===");
 		if (pluginEnvironment != null) {
 			Enumeration<?> attNames = pluginEnvironment.getCurrentSession().getAttributeNames();
 			while (attNames.hasMoreElements()) {
 				log.info("Session attribute name: " + attNames.nextElement());
 			}
 		} else {
 			log.info("SESSION NOT FOUND");
 		}
 	}
 
 	/**
 	 * Gets a MongoDB collection, or creates one if it does not exist.
 	 * 
 	 * @param db 				the MongoDB database object
 	 * @param collectionName	the name of the collection
 	 * @return					the MongoDB collection of the given name if it exists, or a new
 	 * 							MongoDB collection of the given name if it does not exist
 	 */
 	private DBCollection getMongoCollection(DB db, String collectionName) {
		return db.collectionExists(collectionName) ? db.getCollection(collectionName) : 
			db.createCollection(collectionName, null);
 	}
 
 	/**
 	 * Sets the Enonic Plugin Environment object
 	 * 
 	 * @param pluginEnvironment the Enonic Plugin Environment object
 	 */
 	public void setPluginEnvironment(PluginEnvironment pluginEnvironment) {
 		this.pluginEnvironment = pluginEnvironment;
 	}
 
 	/**
 	 * Sets the Enonic Plugin Config object
 	 * 
 	 * @param pluginConfig the Enonic Plugin Config object
 	 */
 	public void setPluginConfig(PluginConfig pluginConfig) {
 		this.pluginConfig = pluginConfig;
 	}
 
 	/**
 	 * Sets the MongoDB client object
 	 * 
 	 * @param mongoClient the MongoDb client object
 	 */
 	public void setMongoClient(MongoClient mongoClient) {
 		this.mongoClient = mongoClient;
 	}
 	
 	/**
 	 * Handles the request after it has been executed by Enonic CMS.
 	 * Should do nothing in this plugin.
 	 * 
 	 * @param httpServletRequest	the http request
 	 * @param httpServletResponse	the http response
 	 */
 	@Override
 	public void postHandle(HttpServletRequest httpServletRequest,
 			HttpServletResponse httpServletResponse) throws Exception {
 		// Do nothing
 	}
 	
 
 }
